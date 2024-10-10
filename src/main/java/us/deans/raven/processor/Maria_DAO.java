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

    public Maria_DAO()  {
    }

    private Connection openConnection() throws Exception {
        maria_connection = DriverManager.getConnection(local_data_db,"bambam","bambam");
        return maria_connection;
    }

    public long processMetaData(RvnJob jobDetails) throws Exception {

        long upload_id = 0;
        Connection maria_connection = openConnection();

        try (PreparedStatement statement = maria_connection.prepareStatement(sql_insert_job_data, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, jobDetails.getTopic_id());
            statement.setString(2,jobDetails.getTitle());
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
}
