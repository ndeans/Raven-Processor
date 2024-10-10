package us.deans.raven.processor;

public class RvnJob {
    private Integer job_id;
    private String created_at;
    private Integer topic_id;
    private String title;
    private Integer post_count;
    private Integer report_type;

    public Integer getJob_id() { return job_id; }
    public void setJob_id(Integer job_id) { this.job_id = job_id; }

    public String getCreated_at() { return created_at; }
    public void setCreated_at(String created_at) { this.created_at = created_at; }

    public Integer getTopic_id() {
        return topic_id;
    }
    public void setTopic_id(Integer topic_id) {
        this.topic_id = topic_id;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getReport_type() {
        return report_type;
    }
    public void setReport_type(Integer report_type) {
        this.report_type = report_type;
    }

    public Integer getPost_count() { return post_count; }
    public void setPost_count(Integer post_count) { this.post_count = post_count; }
}
