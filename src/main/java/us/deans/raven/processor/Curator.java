package us.deans.raven.processor;

import java.util.List;

public interface Curator {
    List<RvnPost> getPostList(long upload_id) throws Exception;
    String getTopicTitle(long upload_id) throws Exception;
    List<RvnJob> getFilteredUploads(String author, String keyword) throws Exception;
    List<RvnPost> getFilteredPosts(long uploadId, String author, String keyword) throws Exception;
}
