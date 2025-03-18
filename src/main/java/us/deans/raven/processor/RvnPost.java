package us.deans.raven.processor;

public class RvnPost {

    private String id;
    private String author;
    private String head;
    private String html;
    private String link;
    private String text;
    private String topic_id;
    private long upload_id;
    private Boolean selected;

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

    public String getTopic_id() { return topic_id; }
    public void setTopic_id(String topic_id) { this.topic_id = topic_id; }

    public long getUpload_id() { return upload_id; }
    public void setUpload_id(long upload_id) { this.upload_id = upload_id; }

    public Boolean getSelected() { return selected; }
    public void setSelected(Boolean selected) { this.selected = selected; }

    public String printRecord() {
        return "id: " + this.id + ", author: " + this.author + ", time: " + this.head + ", text: " + this.text + ", link: " + this.link + "upload_id: " + upload_id;
    }

}
