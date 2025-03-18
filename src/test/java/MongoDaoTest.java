import org.junit.jupiter.api.Test;
import us.deans.raven.processor.MongoDao;
import us.deans.raven.processor.RvnPost;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MongoDaoTest {


    @Test
    public void test_processPostData() {

        List<RvnPost> postList = new ArrayList<>();

        RvnPost post = new RvnPost();
        post.setId("5102456");
        post.setAuthor("Bruce123");
        post.setHead("Oct 10, 2024 09:35:00");
        post.setHtml("<br>Money printing Tom, money printing.<br>");
        post.setLink("https://www.onepoliticalplaza.com/tpr?p=5102456&t=335085");
        post.setText("Money printing Tom, money printing.");
        post.setUpload_id(11);
        post.setSelected(false);
        postList.add(post);

        post = new RvnPost();
        post.setId("5102459");
        post.setAuthor("Liberty Tree");
        post.setHead("Oct 10, 2024 09:39:45");
        post.setHtml("<br>Money printing Tom, money printing.<br>");
        post.setLink("https://www.onepoliticalplaza.com/tpr?p=5102459&t=335085");
        post.setText("Look it up");
        post.setUpload_id(11);
        post.setSelected(false);
        postList.add(post);

        MongoDao mongo_dao = new MongoDao(); //MongoDao.getInstance();
        try {
           mongo_dao.processPostData(7, postList);
        } catch (Exception e) {
            fail(e.getMessage());
        }
        assert true;
    }

    @Test
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
