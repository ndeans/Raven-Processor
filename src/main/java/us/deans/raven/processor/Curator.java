package us.deans.raven.processor;

import java.util.List;

public interface Curator {
    List<RvnPost> getPostList(long upload_id) throws Exception;
}
