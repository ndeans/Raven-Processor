package us.deans.raven.processor;

public class RvnPost {

    private String id;
    private String author;
    private String head;
    private String html;
    private String link;
    private String text;
    private long upload_id;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getUpload_id() { return upload_id; }

    public void setUpload_id(long upload_id) { this.upload_id = upload_id; }

    public String printRecord() {
        return "id: " + this.id + ", author: " + this.author + ", time: " + this.head + ", text: " + this.text + ", link: " + this.link + "upload_id: " + upload_id;
    }

}
