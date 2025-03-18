package us.deans.raven.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

/**
 *  This class is the processor for ingesting uploads from the web services
 */
public class OppProcessor implements Processor {

    Logger logger = LoggerFactory.getLogger(this.getClass());
    Maria_DAO maria_dao = new Maria_DAO();
    MongoDao mongo_dao = new MongoDao(); //MongoDao.getInstance();
    private final RvnJob jobDetails;
    private final List<RvnPost> postList;

    public OppProcessor(RvnImport rvnImport) {
        this.postList = rvnImport.getPost_data();
        this.jobDetails = new RvnJob();
        this.jobDetails.setTopic_id(rvnImport.getTopic_id());
        this.jobDetails.setTitle(rvnImport.getTopic_title());
        this.jobDetails.setReport_type(rvnImport.getReport_type());
        this.jobDetails.setPost_count(rvnImport.getPost_data().size());
        logger.info("processor initialized for {}.", rvnImport.getTopic_id());
        logger.info("postList contains : " + jobDetails.getPost_count() + " posts.");
    }

    // This is where I want to add a transaction structure
    @Override
    public void persist() throws Exception {
        long upload_id = maria_dao.processMetaData(jobDetails);
        mongo_dao.processPostData(upload_id, postList);
    }



}
