package us.deans.raven.processor;

import java.sql.*;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class OppProcessor implements Processor {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    Maria_DAO maria_dao = new Maria_DAO();
    Mongo_DAO mongo_dao = new Mongo_DAO();
    private final RvnJob jobDetails;
    private final List<RvnPost> postList;


    public OppProcessor(RvnImport rvnImport) {
        this.postList = rvnImport.getPost_data();
        this.jobDetails = new RvnJob();
        this.jobDetails.setTopic_id(rvnImport.getTopic_id());
        this.jobDetails.setTitle(rvnImport.getTopic_title());
        this.jobDetails.setReport_type(rvnImport.getReport_type());
        this.jobDetails.setPost_count(rvnImport.getPost_data().size());
        logger.debug("processor initialized.");
        logger.debug("postList contains : " + jobDetails.getPost_count() + " posts.");
    }

    @Override
    public void log() {
        logger.info(jobDetails.getTitle());
    }

    // This is where I want to add a transaction structure
    @Override
    public void upload() throws Exception {
        logger.info("uploding...");
        long upload_id = maria_dao.processMetaData(jobDetails);
        mongo_dao.processPostData(upload_id, postList);
    }
}
