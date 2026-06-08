package us.deans.raven.processor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Output data object for Operation R7 (Conversation Linking).
 * Represents one root-to-leaf path through the reply tree — a single
 * conversation thread.
 */
public class R7Conversation {

    private final List<R7Post> posts; // ordered: root post first, leaf post last
    private R7Post contextPost;       // post quoted by the root, if resolvable

    public R7Conversation(List<R7Post> posts) {
        this.posts = List.copyOf(posts);
    }

    public List<R7Post> getPosts() {
        return posts;
    }

    public int length() {
        return posts.size();
    }

    public R7Post getContextPost() {
        return contextPost;
    }

    public void setContextPost(R7Post contextPost) {
        this.contextPost = contextPost;
    }

    private static final int MAX_AUTHORS = 10; // move to config when framework exists

    public String getAllAuthors() {
        LinkedHashMap<String, Integer> authorMap = new LinkedHashMap<>();
        for (R7Post post : posts) {
            authorMap.merge(post.getAuthor(), 1, Integer::sum);
        }
        return authorMap.entrySet().stream()
                .limit(MAX_AUTHORS)
                .map(e -> e.getKey() + "-" + e.getValue())
                .collect(Collectors.joining(", "));
    }
}
