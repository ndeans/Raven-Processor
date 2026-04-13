package us.deans.raven.processor;

/**
 * Operational data object for Operation R7 (Conversation Linking).
 * One instance per post returned from MongoDB for a given upload.
 */
public class R7Post {

    private String postId; // post_id from MongoDB — primary identifier
    private String author; // author of this post
    private String head; // post heading/subject
    private String html; // full HTML content of the post
    private String link; // URL to original post on OPP
    private int width; // count of direct responses to this post (computed)
    private String uplinkPostId; // post_id of the post this post replies to (computed, null if root)

    public R7Post() {
        this.width = 0;
        this.uplinkPostId = null;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getHead() {
        return head;
    }

    public void setHead(String head) {
        this.head = head;
    }

    public String getHtml() {
        return html;
    }

    public void setHtml(String html) {
        this.html = html;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void incrementWidth() {
        this.width++;
    }

    public String getUplinkPostId() {
        return uplinkPostId;
    }

    public void setUplinkPostId(String uplinkPostId) {
        this.uplinkPostId = uplinkPostId;
    }

    @Override
    public String toString() {
        return "[post_id: " + postId + "] " + author + " — " + head;
    }
}
