package cn.edu.zju.service;

import cn.edu.zju.bean.DashboardStats;
import cn.edu.zju.bean.Sample;
import cn.edu.zju.dao.AnnovarDao;
import cn.edu.zju.dao.DosingGuidelineDao;
import cn.edu.zju.dao.DrugDao;
import cn.edu.zju.dao.SampleDao;

import java.util.List;

public class DashboardService {

    private final SampleDao sampleDao = new SampleDao();
    private final AnnovarDao annovarDao = new AnnovarDao();
    private final DrugDao drugDao = new DrugDao();
    private final DosingGuidelineDao dosingGuidelineDao = new DosingGuidelineDao();

    public DashboardStats getDashboardStats() {
        int totalSamples = sampleDao.count();
        long totalVariants = annovarDao.countAll();
        int totalDrugs = drugDao.count();
        int totalGuidelines = dosingGuidelineDao.count();
        List<Sample> recentActivities = sampleDao.findRecent(5);
        return new DashboardStats(totalSamples, totalVariants, totalDrugs, totalGuidelines, recentActivities);
    }
}
