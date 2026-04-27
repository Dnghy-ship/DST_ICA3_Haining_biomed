package cn.edu.zju.bean;

import java.math.BigDecimal;

public class PatientProfile {

    private int id;
    private int sampleId;
    private Integer age;
    private BigDecimal height;
    private BigDecimal weight;
    private String gender;

    public PatientProfile() {
    }

    public PatientProfile(int id, int sampleId, Integer age, BigDecimal height, BigDecimal weight, String gender) {
        this.id = id;
        this.sampleId = sampleId;
        this.age = age;
        this.height = height;
        this.weight = weight;
        this.gender = gender;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSampleId() {
        return sampleId;
    }

    public void setSampleId(int sampleId) {
        this.sampleId = sampleId;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public BigDecimal getHeight() {
        return height;
    }

    public void setHeight(BigDecimal height) {
        this.height = height;
    }

    public BigDecimal getWeight() {
        return weight;
    }

    public void setWeight(BigDecimal weight) {
        this.weight = weight;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
