package cn.edu.zju.bean;

import java.util.Date;
import java.text.SimpleDateFormat;

public class Sample {
    private int id;
    private Date createdAt;
    private String uploadedBy;

    public Sample() {
    }

    public Sample(int id, Date createdAt, String uploadedBy) {
        this.id = id;
        this.createdAt = createdAt;
        this.uploadedBy = uploadedBy;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedAtFormatted() {
        if (createdAt == null) {
            return "";
        }
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(createdAt);
    }

    public String getUploadedBy() {
        return uploadedBy;
    }

    public void setUploadedBy(String uploadedBy) {
        this.uploadedBy = uploadedBy;
    }
}
