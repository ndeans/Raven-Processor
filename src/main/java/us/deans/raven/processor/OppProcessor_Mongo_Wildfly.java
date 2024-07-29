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


public class OppProcessor_Mongo_Wildfly implements Procesor{

    private final List<OppPost> postList;
    Logger logger = LoggerFactory.getLogger(this.getClass());
    // private final String cloud_db = "mongodb+srv://ncdeans:Qelar9E8DfXgZrrs@cluster0.ueelqzu.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
    private final String local_db = "mongodb://localhost:27017";

    public OppProcessor_Mongo_Wildfly(List<OppPost> postList) {
        this.postList = postList;
        logger.info("instantiating processor...");
    }

    @Override
    public void log() {
        for (OppPost post: this.postList) {
            logger.info("record: " + post.printRecord() + "\n");
        }
    }

    @Override
    public void persist() {

        try (MongoClient mongoClient = MongoClients.create(local_db)) {
            MongoDatabase database = mongoClient.getDatabase("Raven-1");
            MongoCollection<Document> collection = database.getCollection("posts");

            List<Document> postData = new ArrayList<>();

            for (OppPost post : this.postList) {
                int idx = post.getLink().lastIndexOf("=");
                String topic_id = post.getLink().substring(idx + 1);
                Document record = new Document()
                        .append("post_id", post.getId())
                        .append("author", post.getAuthor())
                        .append("head", post.getHead())
                        .append("link", post.getLink())
                        .append("text", post.getText())
                        .append("topic_id", topic_id);
                postData.add(record);
            }
            collection.insertMany(postData);
            logger.info(">>> " + postData.size() + "post records inserted...");
        }

    }
}
