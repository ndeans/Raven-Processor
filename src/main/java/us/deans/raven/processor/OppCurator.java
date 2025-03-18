package us.deans.raven.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 *  This class is the curator for managing the uploaded data and supports the JSF application
 */
public class OppCurator implements Curator {

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public OppCurator() {
        logger.debug("OppCurator called.");
    }

    @Override
    public List<RvnPost> getPostList(long upload_id) throws Exception {
        MongoDao mongoDao = new MongoDao(); //MongoDao.getInstance();
        List<RvnPost> postList = null;
        try {
            postList = mongoDao.getPostList(upload_id);
        } catch (Exception e) {
            logger.error("Trouble with getting PostList: {}", e.getMessage());
        } finally {
            mongoDao.close();
        }

        return postList;
    }


    public String getTopicId(long upload_id) throws Exception {
        String returnVal = "";
        Maria_DAO mariaDao = new Maria_DAO();
        return returnVal;
    }

    public String getTopicTitle(long upload_id) throws Exception {
        String returnVal = "";
        return returnVal;
    }



}
