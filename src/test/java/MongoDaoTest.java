import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import us.deans.raven.processor.MongoDao;
import us.deans.raven.processor.RvnPost;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MongoDaoTest {

    /**
     * Sentinel upload_id for tests that write to the live MongoDB instance.
     * MariaDB upload_ids are auto-increment and always positive, so a negative
     * value can never collide with (or masquerade as) a real upload.
     * Previously this test wrote under upload_id 7 and never cleaned up,
     * which was the cause of PI-31's recurring orphans (see NI-19).
     */
    private static final long TEST_UPLOAD_ID = -1;

    private MongoDao mongoDao;

    @BeforeEach
    void setUp() throws Exception {
        mongoDao = new MongoDao();
        // Clear anything left behind by a previously interrupted run.
        mongoDao.deletePosts(TEST_UPLOAD_ID);
    }

    @AfterEach
    void tearDown() throws Exception {
        try {
            mongoDao.deletePosts(TEST_UPLOAD_ID);
        } finally {
            mongoDao.close();
        }
    }

    @Test
    public void test_processPostData() throws Exception {

        List<RvnPost> postList = new ArrayList<>();

        RvnPost post = new RvnPost();
        post.setId("5102456");
        post.setAuthor("Bruce123");
        post.setHead("Oct 10, 2024 09:35:00");
        post.setHtml("<br>Money printing Tom, money printing.<br>");
        post.setLink("https://www.onepoliticalplaza.com/tpr?p=5102456&t=335085");
        post.setText("Money printing Tom, money printing.");
        post.setUpload_id(TEST_UPLOAD_ID);
        post.setSelected(false);
        postList.add(post);

        post = new RvnPost();
        post.setId("5102459");
        post.setAuthor("Liberty Tree");
        post.setHead("Oct 10, 2024 09:39:45");
        post.setHtml("<br>Money printing Tom, money printing.<br>");
        post.setLink("https://www.onepoliticalplaza.com/tpr?p=5102459&t=335085");
        post.setText("Look it up");
        post.setUpload_id(TEST_UPLOAD_ID);
        post.setSelected(false);
        postList.add(post);

        mongoDao.processPostData(TEST_UPLOAD_ID, postList);

        List<RvnPost> stored = mongoDao.getPostList(TEST_UPLOAD_ID);
        assertEquals(2, stored.size());
        for (RvnPost p : stored) {
            assertEquals("335085", p.getTopic_id(), "topic_id should be parsed from the link");
        }
    }

    @Test
    @Disabled
    public void test_getPostList() {

        List<RvnPost> postList = new ArrayList<>();
        long upload_id = 83;
        MongoDao mongoDao = new MongoDao();
        try {
            postList = mongoDao.getPostList(upload_id);
            assertFalse(postList.isEmpty());
        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            mongoDao.close();
        }

    }

}
