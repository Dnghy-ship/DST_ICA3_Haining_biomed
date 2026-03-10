package cn.edu.zju.servlet;;

import cn.edu.zju.dao.DosingGuidelineDao;
import cn.edu.zju.dao.DrugDao;
import cn.edu.zju.dao.DrugLabelDao;
import cn.edu.zju.dao.SampleDao;
import cn.edu.zju.filter.AuthenticationFilter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

@WebServlet(name = "IndexServlet", urlPatterns = {"/index"})
public class IndexServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Enumeration<String> attributeNames = request.getSession().getAttributeNames();
        System.out.println("print session");
        System.out.println(request.getSession().getAttribute(AuthenticationFilter.USERNAME));
        while (attributeNames.hasMoreElements()) {
            System.out.println(attributeNames.nextElement());
        }

        // 加载系统统计数据
        try {
            DrugDao drugDao = new DrugDao();
            DrugLabelDao drugLabelDao = new DrugLabelDao();
            DosingGuidelineDao dosingGuidelineDao = new DosingGuidelineDao();
            SampleDao sampleDao = new SampleDao();

            request.setAttribute("totalDrugs", drugDao.countAll("drug"));
            request.setAttribute("totalDrugLabels", drugLabelDao.countAll("drug_label"));
            request.setAttribute("totalDosingGuidelines", dosingGuidelineDao.countAll("dosing_guideline"));
            request.setAttribute("totalSamples", sampleDao.countAll("sample"));
        } catch (Exception e) {
            // 数据库不可用时设置默认值
            request.setAttribute("totalDrugs", 0);
            request.setAttribute("totalDrugLabels", 0);
            request.setAttribute("totalDosingGuidelines", 0);
            request.setAttribute("totalSamples", 0);
        }

        request.getRequestDispatcher("/views/index.jsp").forward(request, response);
    }
}
