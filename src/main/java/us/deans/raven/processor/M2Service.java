package us.deans.raven.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Operation M2 — Selective Removal.
 *
 * Given a list of upload_id values, cascades deletion through both databases:
 * MongoDB posts are deleted first, then the MariaDB upload record.
 * A failure on one upload is logged and reported but does not abort the rest.
 */
public class M2Service {

    private static final Logger log = LoggerFactory.getLogger(M2Service.class);

    private final MongoDao mongoDao;
    private final Maria_DAO mariaDao;

    public M2Service(MongoDao mongoDao, Maria_DAO mariaDao) {
        this.mongoDao = mongoDao;
        this.mariaDao = mariaDao;
    }

    public List<M2Result> execute(List<Long> uploadIds) {
        List<M2Result> results = new ArrayList<>();

        for (long uploadId : uploadIds) {
            try {
                long postsDeleted = mongoDao.deletePosts(uploadId);
                mariaDao.deleteUpload(uploadId);
                log.info("M2: removed upload_id={} ({} posts deleted)", uploadId, postsDeleted);
                results.add(new M2Result(uploadId, postsDeleted, true, null));
            } catch (Exception e) {
                log.error("M2: failed for upload_id={} — {}", uploadId, e.getMessage(), e);
                results.add(new M2Result(uploadId, 0, false, e.getMessage()));
            }
        }

        return results;
    }
}
