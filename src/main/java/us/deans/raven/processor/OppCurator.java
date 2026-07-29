package us.deans.raven.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

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

    @Override
    public String getTopicTitle(long upload_id) throws Exception {
        Maria_DAO mariaDao = new Maria_DAO();
        return mariaDao.getTopicTitle(upload_id);
    }

    @Override
    public List<RvnJob> getFilteredUploads(String author, String keyword, int offset, int limit) throws Exception {
        Maria_DAO mariaDao = new Maria_DAO();
        return mariaDao.getFilteredUploads(author, keyword, offset, limit);
    }

    @Override
    public List<RvnPost> getFilteredPosts(long uploadId, String author, String keyword) throws Exception {
        MongoDao mongoDao = new MongoDao();
        try {
            return mongoDao.getFilteredPosts(uploadId, author, keyword);
        } finally {
            mongoDao.close();
        }
    }

    @Override
    public Optional<RvnPost> findPostById(String postId) throws Exception {
        MongoDao mongoDao = new MongoDao();
        try {
            return mongoDao.findPostById(postId);
        } finally {
            mongoDao.close();
        }
    }

}
