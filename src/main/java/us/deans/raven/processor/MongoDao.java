package us.deans.raven.processor;

import com.mongodb.client.*;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import org.bson.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MongoDao {

    private static MongoDao instance;

    private final String local_content_db = "mongodb://localhost:27017";
    private String databaseName = "Raven-1";
    private String collectionName = "posts";
    MongoClient mongoClient;
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public MongoDao() {
        mongoClient = MongoClients.create(local_content_db);
        this.database = mongoClient.getDatabase(databaseName);
        this.collection = database.getCollection(collectionName);
    }

    public void close() {
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    public void processPostData(long upload_id, List<RvnPost> postList) throws Exception {
        List<Document> postData = new ArrayList<>();
        for (RvnPost post : postList) {
            String topic_id = "";
            if (post.getLink() != null && post.getLink().contains("t=")) {
                int idx = post.getLink().lastIndexOf("t=");
                topic_id = post.getLink().substring(idx + 2);
            } else if (post.getLink() != null && post.getLink().contains("=")) {
                int idx = post.getLink().lastIndexOf("=");
                topic_id = post.getLink().substring(idx + 1);
            }

            String uploadID = String.valueOf(upload_id);
            Document record = new Document()
                    .append("post_id", post.getId())
                    .append("author", post.getAuthor())
                    .append("head", post.getHead())
                    .append("link", post.getLink())
                    .append("text", post.getText())
                    .append("html", post.getHtml())
                    .append("topic_id", topic_id)
                    .append("upload_id", uploadID);
            postData.add(record);
        }
        if (!postData.isEmpty()) {
            collection.insertMany(postData);
            logger.info(">>> " + postData.size() + " post records inserted...");
        }
    }

    public List<RvnPost> getPostList(long upload_id) throws Exception {
        logger.info("Getting list for upload_id = {}", upload_id);
        List<RvnPost> postList = new ArrayList<>();
        String strUploadId = String.valueOf(upload_id);

        try (MongoCursor<Document> cursor = collection.find(Filters.eq("upload_id", strUploadId)).iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                RvnPost post = new RvnPost();
                post.setId(doc.getString("post_id"));
                post.setAuthor(doc.getString("author"));
                post.setHead(doc.getString("head"));
                post.setLink(doc.getString("link"));
                post.setText(doc.getString("text"));
                post.setHtml(doc.getString("html"));
                post.setTopic_id(doc.getString("topic_id"));
                post.setUpload_id(upload_id);
                postList.add(post);
            }
        }
        return postList;
    }

    public long deletePosts(long upload_id) throws Exception {
        String strUploadId = String.valueOf(upload_id);
        logger.info("Deleting posts for upload_id = {}", strUploadId);
        com.mongodb.client.result.DeleteResult result = collection.deleteMany(Filters.eq("upload_id", strUploadId));
        long deletedCount = result.getDeletedCount();
        logger.info("Deleted {} post records", deletedCount);
        return deletedCount;
    }

    public long getPostDocumentCount() {
        try {
            return collection.countDocuments(new Document());
        } catch (Exception e) {
            logger.error("Error counting documents in MongoDB posts collection", e);
            throw e;
        }
    }

    public Map<String, Long> findOrphanUploadIds(Set<String> knownIds) {
        Map<String, Long> orphans = new LinkedHashMap<>();
        try {
            collection.aggregate(Arrays.asList(
                Aggregates.group("$upload_id", Accumulators.sum("count", 1)),
                Aggregates.match(Filters.nin("_id", knownIds))
            )).forEach(doc -> orphans.put(doc.getString("_id"), ((Number) doc.get("count")).longValue()));
        } catch (Exception e) {
            logger.error("Error finding orphan upload_ids in MongoDB", e);
            throw e;
        }
        return orphans;
    }

    /**
     * R7: Returns all posts for a given upload_id as R7Post objects.
     * Fields returned: post_id, author, head, html, link.
     * width and uplinkPostId are initialised to defaults (0 / null) by the R7Post
     * constructor.
     */
    public List<R7Post> getPostsByUploadId(String uploadId) {
        logger.info("R7: Loading posts for upload_id = {}", uploadId);
        List<R7Post> posts = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection
                .find(Filters.eq("upload_id", uploadId))
                .projection(Projections.fields(
                        Projections.include("post_id", "author", "head", "html", "link"),
                        Projections.excludeId()))
                .iterator()) {
            while (cursor.hasNext()) {
                Document doc = cursor.next();
                R7Post post = new R7Post();
                post.setPostId(doc.getString("post_id"));
                post.setAuthor(doc.getString("author"));
                post.setHead(doc.getString("head"));
                post.setHtml(doc.getString("html"));
                post.setLink(doc.getString("link"));
                posts.add(post);
            }
        }
        logger.info("R7: Loaded {} posts", posts.size());
        return posts;
    }
}
