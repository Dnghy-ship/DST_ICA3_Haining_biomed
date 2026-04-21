package cn.edu.zju.bean;

import java.util.List;

public class DashboardStats {

    private int totalSamples;
    private long totalVariants;
    private int totalDrugs;
    private int totalGuidelines;
    private List<Sample> recentActivities;

    public DashboardStats() {
    }

    public DashboardStats(int totalSamples, long totalVariants, int totalDrugs, int totalGuidelines, List<Sample> recentActivities) {
        this.totalSamples = totalSamples;
        this.totalVariants = totalVariants;
        this.totalDrugs = totalDrugs;
        this.totalGuidelines = totalGuidelines;
        this.recentActivities = recentActivities;
    }

    public int getTotalSamples() {
        return totalSamples;
    }

    public void setTotalSamples(int totalSamples) {
        this.totalSamples = totalSamples;
    }

    public long getTotalVariants() {
        return totalVariants;
    }

    public void setTotalVariants(long totalVariants) {
        this.totalVariants = totalVariants;
    }

    public List<Sample> getRecentActivities() {
        return recentActivities;
    }

    public void setRecentActivities(List<Sample> recentActivities) {
        this.recentActivities = recentActivities;
    }

    public int getTotalDrugs() {
        return totalDrugs;
    }

    public void setTotalDrugs(int totalDrugs) {
        this.totalDrugs = totalDrugs;
    }

    public int getTotalGuidelines() {
        return totalGuidelines;
    }

    public void setTotalGuidelines(int totalGuidelines) {
        this.totalGuidelines = totalGuidelines;
    }
}
