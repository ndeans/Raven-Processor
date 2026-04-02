package us.deans.raven.processor;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Operation R7 — Conversation Linking.
 *
 * Given an upload_id, reconstructs threaded conversations by parsing HTML
 * quote-reply
 * relationships and building a parent-child reply tree. Each root-to-leaf path
 * through
 * the tree is returned as one R7Conversation.
 *
 * Four phases:
 * Phase 1 — Load posts from MongoDB
 * Phase 2 — Parse HTML and link each post to its parent via uplink_post_id
 * Phase 3 — Find root posts (no uplink, has at least one reply)
 * Phase 4 — Recursively build conversations from each root
 */
public class R7Service {

    private static final Logger log = LoggerFactory.getLogger(R7Service.class);
    private static final int FINGERPRINT_WORDS = 20;

    private final MongoDao mongoDao;

    public R7Service(MongoDao mongoDao) {
        this.mongoDao = mongoDao;
    }

    /**
     * Main entry point. Returns the list of all reconstructed conversations for the
     * given upload.
     */
    public List<R7Conversation> execute(String uploadId) {
        log.info("R7: Starting for upload_id={}", uploadId);

        // Phase 1 — Load
        List<R7Post> posts = mongoDao.getPostsByUploadId(uploadId);
        if (posts.isEmpty()) {
            log.warn("R7: No posts found for upload_id={}", uploadId);
            return List.of();
        }
        log.info("R7 Phase 1 complete — {} posts loaded", posts.size());

        // Detect obsolete uploads (pre-html collection) — html field will be null on
        // all posts
        if (posts.get(0).getHtml() == null) {
            log.warn("R7: upload_id={} is an obsolete upload (html field is null). " +
                    "These records predate html collection. Use Operation M2 to remove them.", uploadId);
            System.out.println("\n[R7] Sorry — upload_id " + uploadId +
                    " is an obsolete upload that predates HTML collection. " +
                    "Conversation linking is not available for this upload.\n");
            return List.of();
        }

        // Phase 2 — Parse & Link
        parseAndLink(posts, uploadId);
        log.info("R7 Phase 2 complete — reply graph built");

        // Phase 3 — Find Roots
        List<R7Post> roots = findRoots(posts);
        log.info("R7 Phase 3 complete — {} root(s) identified", roots.size());

        // Phase 4 — Recursive Conversation Builder
        List<R7Conversation> results = new ArrayList<>();
        for (R7Post root : roots) {
            List<R7Post> path = new ArrayList<>();
            path.add(root);
            buildConversations(root, path, results, posts);
        }
        log.info("R7 Phase 4 complete — {} conversation(s) built", results.size());

        return results;
    }

    // -------------------------------------------------------------------------
    // Phase 2 — Parse & Link
    // -------------------------------------------------------------------------

    private void parseAndLink(List<R7Post> posts, String uploadId) {
        for (R7Post post : posts) {
            if (post.getHtml() == null || post.getHtml().isBlank()) {
                continue;
            }

            Document doc = Jsoup.parse(post.getHtml());
            Element outerDiv = doc.selectFirst("div.smalltext");
            Element innerDiv = doc.selectFirst("div.quote_colors");

            // No quote-reply divs — standalone post or plain reply
            if (outerDiv == null || innerDiv == null) {
                continue;
            }

            // Extract the quoted author from the outer div's own text
            String outerText = outerDiv.ownText().trim();
            if (!outerText.endsWith("wrote:")) {
                continue; // unexpected format — skip
            }
            String quotedAuthor = outerText.substring(0, outerText.length() - "wrote:".length()).trim();
            if (quotedAuthor.isBlank()) {
                log.warn("R7 Phase 2: blank quoted author in post_id={} upload_id={}", post.getPostId(), uploadId);
                continue;
            }

            // Extract the fingerprint from the quoted text (first N words)
            String quotedFingerprint = fingerprint(innerDiv.text());

            // Search for parent: same author, own-content fingerprint matches quoted
            // fingerprint
            List<R7Post> candidates = posts.stream()
                    .filter(p -> quotedAuthor.equals(p.getAuthor()))
                    .filter(p -> quotedFingerprint.equals(ownContentFingerprint(p)))
                    .collect(Collectors.toList());

            if (candidates.size() == 1) {
                R7Post parent = candidates.get(0);
                post.setUplinkPostId(parent.getPostId());
                parent.incrementWidth();
            } else if (candidates.isEmpty()) {
                log.warn("R7 Phase 2: zero matches — post_id={}, upload_id={}, quotedAuthor={}",
                        post.getPostId(), uploadId, quotedAuthor);
            } else {
                log.warn("R7 Phase 2: ambiguous match ({}) — post_id={}, upload_id={}, quotedAuthor={}",
                        candidates.size(), post.getPostId(), uploadId, quotedAuthor);
            }
        }
    }

    /**
     * Extracts own content from a post (excluding any nested quote div) and
     * returns a fingerprint of the first N words.
     */
    private String ownContentFingerprint(R7Post post) {
        if (post.getHtml() == null || post.getHtml().isBlank()) {
            return "";
        }
        Document doc = Jsoup.parse(post.getHtml());
        Element quoteDiv = doc.selectFirst("div.smalltext");
        if (quoteDiv != null) {
            quoteDiv.remove();
        }
        return fingerprint(doc.text());
    }

    /**
     * Returns the first N words of the given text as a lowercase,
     * whitespace-normalised string.
     */
    private String fingerprint(String text) {
        if (text == null || text.isBlank())
            return "";
        String[] words = text.trim().split("\\s+");
        return Arrays.stream(words)
                .limit(FINGERPRINT_WORDS)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));
    }

    // -------------------------------------------------------------------------
    // Phase 3 — Find Roots
    // -------------------------------------------------------------------------

    private List<R7Post> findRoots(List<R7Post> posts) {
        return posts.stream()
                .filter(p -> p.getUplinkPostId() == null && p.getWidth() > 0)
                .collect(Collectors.toList());
    }

    // -------------------------------------------------------------------------
    // Phase 4 — Recursive Conversation Builder
    // -------------------------------------------------------------------------

    private void buildConversations(R7Post current, List<R7Post> pathSoFar,
            List<R7Conversation> results, List<R7Post> allPosts) {
        List<R7Post> children = allPosts.stream()
                .filter(p -> current.getPostId().equals(p.getUplinkPostId()))
                .collect(Collectors.toList());

        if (children.isEmpty()) {
            // Base case — path is complete
            results.add(new R7Conversation(new ArrayList<>(pathSoFar)));
        } else {
            // Recursive case — branch for each child
            for (R7Post child : children) {
                pathSoFar.add(child);
                buildConversations(child, pathSoFar, results, allPosts);
                pathSoFar.remove(pathSoFar.size() - 1);
            }
        }
    }
}
