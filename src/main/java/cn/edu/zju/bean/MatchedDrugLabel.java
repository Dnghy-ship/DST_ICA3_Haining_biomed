package cn.edu.zju.bean;

import java.util.List;

public class MatchedDrugLabel extends DrugLabel {

    private int score;
    private String recommendationLevel;
    private List<String> matchedGenes;
    private Double calculatedDose;

    public MatchedDrugLabel(DrugLabel label, int score, String recommendationLevel, List<String> matchedGenes) {
        super(label.getId(), label.getName(), label.getObjCls(),
                label.isAlternateDrugAvailable(), label.isDosingInformation(),
                label.getPrescribingMarkdown(), label.getSource(),
                label.getTextMarkdown(), label.getSummaryMarkdown(),
                label.getRaw(), label.getDrugId());
        this.score = score;
        this.recommendationLevel = recommendationLevel;
        this.matchedGenes = matchedGenes;
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

    public List<String> getMatchedGenes() {
        return matchedGenes;
    }

    public void setMatchedGenes(List<String> matchedGenes) {
        this.matchedGenes = matchedGenes;
    }

    public Double getCalculatedDose() {
        return calculatedDose;
    }

    public void setCalculatedDose(Double calculatedDose) {
        this.calculatedDose = calculatedDose;
    }
}
