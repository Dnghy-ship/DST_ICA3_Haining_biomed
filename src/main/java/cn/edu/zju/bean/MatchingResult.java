package cn.edu.zju.bean;

import java.util.Date;

public class MatchingResult {

    private int id;
    private int sampleId;
    private String drugLabelId;
    private int score;
    private String recommendationLevel;
    private String matchedGenes;
    private Date createdAt;

    public MatchingResult() {
    }

    public MatchingResult(int id, int sampleId, String drugLabelId, int score,
                          String recommendationLevel, String matchedGenes, Date createdAt) {
        this.id = id;
        this.sampleId = sampleId;
        this.drugLabelId = drugLabelId;
        this.score = score;
        this.recommendationLevel = recommendationLevel;
        this.matchedGenes = matchedGenes;
        this.createdAt = createdAt;
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

    public String getDrugLabelId() {
        return drugLabelId;
    }

    public void setDrugLabelId(String drugLabelId) {
        this.drugLabelId = drugLabelId;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getRecommendationLevel() {
        return recommendationLevel;
    }

    public void setRecommendationLevel(String recommendationLevel) {
        this.recommendationLevel = recommendationLevel;
    }

    public String getMatchedGenes() {
        return matchedGenes;
    }

    public void setMatchedGenes(String matchedGenes) {
        this.matchedGenes = matchedGenes;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }
}
