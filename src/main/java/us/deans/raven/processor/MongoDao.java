package us.deans.raven.processor;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class MongoDao {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public void processPostData(long upload_id, List<RvnPost> postList) throws Exception {
        // private final String cloud_content_db = "mongodb+srv://ncdeans:Qelar9E8DfXgZrrs@cluster0.ueelqzu.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
        String local_content_db = "mongodb://localhost:27017";

        try (MongoClient mongoClient = MongoClients.create(local_content_db)) {

            MongoDatabase database = mongoClient.getDatabase("Raven-1");
            MongoCollection<Document> collection = database.getCollection("posts");

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
    }
}
