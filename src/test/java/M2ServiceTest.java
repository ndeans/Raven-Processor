import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import us.deans.raven.processor.M2Result;
import us.deans.raven.processor.M2Service;
import us.deans.raven.processor.Maria_DAO;
import us.deans.raven.processor.MongoDao;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class M2ServiceTest {

    @Mock
    MongoDao mongoDao;

    @Mock
    Maria_DAO mariaDao;

    M2Service service;

    @BeforeEach
    void setUp() {
        service = new M2Service(mongoDao, mariaDao);
    }

    @Test
    void singleUpload_success() throws Exception {
        when(mongoDao.deletePosts(301L)).thenReturn(482L);

        List<M2Result> results = service.execute(List.of(301L));

        assertEquals(1, results.size());
        M2Result r = results.get(0);
        assertEquals(301L, r.getUploadId());
        assertEquals(482L, r.getPostsDeleted());
        assertTrue(r.isSuccess());
        assertNull(r.getMessage());

        verify(mongoDao).deletePosts(301L);
        verify(mariaDao).deleteUpload(301L);
    }

    @Test
    void multipleUploads_allSucceed() throws Exception {
        when(mongoDao.deletePosts(301L)).thenReturn(10L);
        when(mongoDao.deletePosts(302L)).thenReturn(20L);

        List<M2Result> results = service.execute(List.of(301L, 302L));

        assertEquals(2, results.size());
        assertTrue(results.get(0).isSuccess());
        assertTrue(results.get(1).isSuccess());
        assertEquals(10L, results.get(0).getPostsDeleted());
        assertEquals(20L, results.get(1).getPostsDeleted());
    }

    @Test
    void mongoFailure_reportedAndRemainingUploadsStillProcessed() throws Exception {
        when(mongoDao.deletePosts(301L)).thenThrow(new RuntimeException("connection timeout"));
        when(mongoDao.deletePosts(302L)).thenReturn(5L);

        List<M2Result> results = service.execute(List.of(301L, 302L));

        assertEquals(2, results.size());

        M2Result failed = results.get(0);
        assertEquals(301L, failed.getUploadId());
        assertFalse(failed.isSuccess());
        assertEquals(0L, failed.getPostsDeleted());
        assertTrue(failed.getMessage().contains("connection timeout"));

        M2Result succeeded = results.get(1);
        assertEquals(302L, succeeded.getUploadId());
        assertTrue(succeeded.isSuccess());
        assertEquals(5L, succeeded.getPostsDeleted());

        verify(mariaDao, never()).deleteUpload(301L);
        verify(mariaDao).deleteUpload(302L);
    }

    @Test
    void mariaFailure_reportedAndRemainingUploadsStillProcessed() throws Exception {
        when(mongoDao.deletePosts(301L)).thenReturn(10L);
        doThrow(new RuntimeException("DB error")).when(mariaDao).deleteUpload(301L);
        when(mongoDao.deletePosts(302L)).thenReturn(5L);

        List<M2Result> results = service.execute(List.of(301L, 302L));

        assertFalse(results.get(0).isSuccess());
        assertTrue(results.get(1).isSuccess());
    }
}
