package us.deans.raven.processor;

import com.mongodb.client.*;
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

    public List<Document> getPostList(int upload_id) throws Exception {

        logger.info("getting list for upload_id = " + upload_id);
        logger.info("MongoDB connection state: " + mongoClient);

        List<Document> postList = new ArrayList<>();
        try (MongoCursor<Document> cursor = collection.find(new Document("upload_id", upload_id)).iterator() ) {
            while (cursor.hasNext()) {
                postList.add(cursor.next());
            }
        }
        return postList;
    }

}
