package us.deans.raven.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

// import org.mariadb.jdbc.Connection;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.PreparedStatement;

import java.util.ArrayList;
import java.util.List;


public class OppProcessor_02 implements Processor {

    private final RvnJob jobDetails;
    private final List<RvnPost> postList;

    Logger logger = LoggerFactory.getLogger(this.getClass());

/*
    public OppProcessor_02(List<RvnPost> postList) {
        this.postList = postList;
        logger.info("instantiating processor...");
    }
*/
    public OppProcessor_02(RvnImport rvnImport) {
        this.jobDetails = new RvnJob();
        this.jobDetails.setJob_id(1);
        this.jobDetails.setCreated_at("add date");
        this.jobDetails.setTopic_id(rvnImport.getTopic_id());
        this.jobDetails.setTitle(rvnImport.getTopic_title());
        this.jobDetails.setReport_type(rvnImport.getReport_type());
        this.postList = rvnImport.getPostList();
        logger.info("processor initialized.");
    }

    @Override
    public void log() {

        for (RvnPost post: this.postList) {
            logger.info("record: " + post.printRecord() + "\n");
        }
    }

    @Override
    public void persist() throws Exception {

        logger.info("processor.persist()...");

        String local_data_db = "jdbc:mariadb://vortex:3306/raven_1";
        Connection maria_connection = DriverManager.getConnection(local_data_db,"bambam","bambam");

        String sql_insert_job_data = "insert into uploads(topic_id, topic_title, report_type) VALUES (?,?,?)";

        try (PreparedStatement statement = maria_connection.prepareStatement(sql_insert_job_data) ) {
            statement.setInt(1, jobDetails.getTopic_id());
            statement.setString(2,jobDetails.getTitle());
            statement.setInt(3, jobDetails.getReport_type());
            int rowsInserted = statement.executeUpdate();
            logger.info(rowsInserted + " rows added to metadata.");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // private final String cloud_content_db = "mongodb+srv://ncdeans:Qelar9E8DfXgZrrs@cluster0.ueelqzu.mongodb.net/?retryWrites=true&w=majority&appName=Cluster0";
        String local_content_db = "mongodb://localhost:27017";

        try (MongoClient mongoClient = MongoClients.create(local_content_db)) {

            MongoDatabase database = mongoClient.getDatabase("Raven-1");
            MongoCollection<Document> collection = database.getCollection("posts");

            List<Document> postData = new ArrayList<>();

            for (RvnPost post : this.postList) {
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
