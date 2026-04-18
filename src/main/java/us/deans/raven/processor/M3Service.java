package us.deans.raven.processor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Operation M3 — Daily Verification.
 *
 * Compares the total post count between MariaDB (SUM(post_count) from uploads)
 * and MongoDB (countDocuments from posts collection). A mismatch indicates an
 * incomplete previous maintenance operation or an uncontrolled data path.
 * On mismatch, runs a secondary diagnostic to identify orphan upload_ids.
 */
public class M3Service {

    private static final Logger log = LoggerFactory.getLogger(M3Service.class);

    private final Maria_DAO mariaDao;
    private final MongoDao mongoDao;

    public M3Service(Maria_DAO mariaDao, MongoDao mongoDao) {
        this.mariaDao = mariaDao;
        this.mongoDao = mongoDao;
    }

    public M3Result execute() throws Exception {
        long mariaCount = mariaDao.getPostCountSum();
        long mongoCount = mongoDao.getPostDocumentCount();
        boolean match = (mariaCount == mongoCount);
        String message = String.format("M3: mariaDb=%d mongoDb=%d match=%b", mariaCount, mongoCount, match);
        Map<String, Long> orphans = Collections.emptyMap();

        if (match) {
            log.info(message);
        } else {
            log.error(message);
            Set<String> knownIds = mariaDao.getAllUploadIds();
            orphans = mongoDao.findOrphanUploadIds(knownIds);
            if (orphans.isEmpty()) {
                log.error("M3: no orphan upload_ids found — delta may be within a known upload_id");
            } else {
                orphans.forEach((id, count) ->
                    log.error("M3: orphan upload_id={} postCount={}", id, count));
            }
        }

        return new M3Result(mariaCount, mongoCount, match, message, orphans);
    }
}
