package us.deans.raven.processor;

public class M2Result {

    private final long uploadId;
    private final long postsDeleted;
    private final boolean success;
    private final String message;

    public M2Result(long uploadId, long postsDeleted, boolean success, String message) {
        this.uploadId = uploadId;
        this.postsDeleted = postsDeleted;
        this.success = success;
        this.message = message;
    }

    public long getUploadId()     { return uploadId; }
    public long getPostsDeleted() { return postsDeleted; }
    public boolean isSuccess()    { return success; }
    public String getMessage()    { return message; }
}
