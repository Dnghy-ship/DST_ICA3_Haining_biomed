package cn.edu.zju.service;

import cn.edu.zju.bean.DashboardStats;
import cn.edu.zju.bean.Sample;
import cn.edu.zju.dao.AnnovarDao;
import cn.edu.zju.dao.SampleDao;

import java.util.List;

public class DashboardService {

    private final SampleDao sampleDao = new SampleDao();
    private final AnnovarDao annovarDao = new AnnovarDao();

    public DashboardStats getDashboardStats() {
        int totalSamples = sampleDao.count();
        long totalVariants = annovarDao.countAll();
        List<Sample> recentActivities = sampleDao.findRecent(5);
        return new DashboardStats(totalSamples, totalVariants, recentActivities);
    }
}
