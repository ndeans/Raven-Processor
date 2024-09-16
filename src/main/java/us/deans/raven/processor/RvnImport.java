package us.deans.raven.processor;

import java.util.List;

public class RvnImport {

    private Integer topic_id;
    private Integer report_type;
    private String topic_title;
    private List<RvnPost> postList;

    public Integer getTopic_id() {
        return topic_id;
    }

    public void setTopic_id(Integer topic_id) {
        this.topic_id = topic_id;
    }

    public Integer getReport_type() {
        return report_type;
    }

    public void setReport_type(Integer report_type) {
        this.report_type = report_type;
    }

    public String getTopic_title() {
        return topic_title;
    }

    public void setTopic_title(String topic_title) {
        this.topic_title = topic_title;
    }

    public List<RvnPost> getPostList() { return postList; }

    public void setPosts(List<RvnPost> postList) {
        this.postList = postList;
    }

    public String printJob() {
        return "topic_id: " + this.topic_id + ", topic_title: " + this.topic_title + ", report_type: " + this.report_type + ", + " + this.postList.size() + " post records.";
    }
}

