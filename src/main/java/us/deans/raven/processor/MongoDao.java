package us.deans.raven.processor;

import com.mongodb.client.*;
import com.mongodb.client.model.Filters;
import org.bson.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
            int idx = post.getLink().lastIndexOf("=");
            String topic_id = post.getLink().substring(idx + 1);
            String uploadID = String.valueOf(upload_id);
            Document record = new Document()
                    .append("post_id", post.getId())
                    .append("author", post.getAuthor())
                    .append("head", post.getHead())
                    .append("link", post.getLink())
                    .append("text", post.getText())
                    .append("topic_id", topic_id)
                    .append("upload_id", uploadID);
            postData.add(record);
        }
        collection.insertMany(postData);
        logger.info(">>> " + postData.size() + " post records inserted...");
    }

    public List<RvnPost> getPostList(long upload_id) throws Exception {
        logger.info("Getting list for upload_id = {}", upload_id);
        // logger.info("MongoDB connection state: {}", mongoClient.toString());
        List<RvnPost> postList = new ArrayList<>();
        String strUploadId = upload_id + "";

        try (MongoCursor<Document> cursor = collection.find(Filters.eq("upload_id", strUploadId)).iterator()) {
            while (cursor.hasNext()) {
                Document doc = (Document) cursor.next();
                RvnPost post = new RvnPost();
                post.setId(doc.getString("post_id"));
                post.setAuthor(doc.getString("author"));
                post.setHead(doc.getString("head"));
                post.setLink(doc.getString("link"));
                post.setText(doc.getString("text"));
                post.setTopic_id(doc.getString("topic_id"));
                post.setUpload_id(upload_id);
                // logger.info("...");
                postList.add(post);
            }
        }
        return postList;
    }
}
