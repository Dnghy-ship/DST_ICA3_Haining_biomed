package cn.edu.zju.bean;

import java.util.List;

public class DashboardStats {

    private int totalSamples;
    private long totalVariants;
    private List<Sample> recentActivities;

    public DashboardStats() {
    }

    public DashboardStats(int totalSamples, long totalVariants, List<Sample> recentActivities) {
        this.totalSamples = totalSamples;
        this.totalVariants = totalVariants;
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
}
