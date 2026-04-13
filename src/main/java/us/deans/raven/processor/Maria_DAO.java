package us.deans.raven.processor;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Maria_DAO {

    private long upload_id = 0;
    Connection maria_connection;

    private final String local_data_db = "jdbc:mariadb://vortex:3306/raven_1";
    private String sql_insert_job_data = "insert into uploads(topic_id, topic_title, report_type, post_count) VALUES (?,?,?,?)";
    private String sql_get_job_list = "select * from uploads";

    Logger logger = LoggerFactory.getLogger(this.getClass());

    public Maria_DAO() {
    }

    private Connection openConnection() throws Exception {
        maria_connection = DriverManager.getConnection(local_data_db, "bambam", "bambam");
        return maria_connection;
    }

    public long processMetaData(RvnJob jobDetails) throws Exception {

        long upload_id = 0;
        Connection maria_connection = openConnection();

        try (PreparedStatement statement = maria_connection.prepareStatement(sql_insert_job_data,
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, jobDetails.getTopic_id());
            statement.setString(2, jobDetails.getTitle());
            statement.setInt(3, jobDetails.getReport_type());
            statement.setInt(4, jobDetails.getPost_count());
            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        upload_id = generatedKeys.getLong(1);
                        statement.setString(4, String.valueOf(upload_id));
                        logger.info("added upload record " + upload_id);
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return upload_id;
    }

    public List<RvnJob> getMetaData() throws Exception {

        Connection maria_connection = openConnection();
        ResultSet rs = null;
        List<RvnJob> jobList = new ArrayList<>();

        try (PreparedStatement statement = maria_connection.prepareStatement(sql_get_job_list)) {
            rs = statement.executeQuery();
            while (rs.next()) {
                RvnJob record = new RvnJob();
                record.setJob_id(rs.getInt(1));
                record.setCreated_at(rs.getString(2));
                record.setTopic_id(rs.getInt(3));
                record.setTitle(rs.getString(4));
                record.setReport_type(rs.getInt(5));
                record.setPost_count(rs.getInt(6));
                jobList.add(record);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return jobList;
    }

    public void markForPruning() throws Exception {
        String sql = "UPDATE uploads SET pruned = 1, pruned_at = NOW() WHERE pruned = 0 AND upload_id NOT IN ( " +
                "  SELECT id FROM ( " +
                "    SELECT u1.upload_id as id FROM uploads u1 " +
                "    INNER JOIN (SELECT topic_id, MAX(upload_time) as max_time FROM uploads GROUP BY topic_id) u2 " +
                "    ON u1.topic_id = u2.topic_id AND u1.upload_time = u2.max_time " +
                "  ) as latest " +
                ")";
        try (Connection conn = openConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            int rowsAffected = stmt.executeUpdate();
            logger.info("Marked {} uploads for pruning", rowsAffected);
        } catch (SQLException e) {
            logger.error("Error marking uploads for pruning", e);
            throw e;
        }
    }

    public List<Long> getPrunedUploadIds() throws Exception {
        String sql = "SELECT upload_id FROM uploads WHERE pruned = 1";
        List<Long> ids = new ArrayList<>();
        try (Connection conn = openConnection();
                PreparedStatement stmt = conn.prepareStatement(sql);
                ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                ids.add(rs.getLong("upload_id"));
            }
        } catch (SQLException e) {
            logger.error("Error fetching pruned upload IDs", e);
            throw e;
        }
        return ids;
    }

    public void deleteUpload(long uploadId) throws Exception {
        String sql = "DELETE FROM uploads WHERE upload_id = ?";
        try (Connection conn = openConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, uploadId);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Deleted upload record: {}", uploadId);
            }
        } catch (SQLException e) {
            logger.error("Error deleting upload record: {}", uploadId, e);
            throw e;
        }
    }
}
