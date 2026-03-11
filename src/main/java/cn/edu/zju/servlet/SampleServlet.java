package cn.edu.zju.servlet;

import cn.edu.zju.bean.Sample;
import cn.edu.zju.dao.AnnovarDao;
import cn.edu.zju.dao.SampleDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet(name = "SampleServlet", urlPatterns = "/samples")
public class SampleServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        SampleDao sampleDao = new SampleDao();
        AnnovarDao annovarDao = new AnnovarDao();

        List<Sample> samples = sampleDao.findAll();
        // 每个样本的变异数量
        Map<Integer, Integer> variantCounts = annovarDao.countVariantsBySample();

        request.setAttribute("samples", samples);
        request.setAttribute("variantCounts", variantCounts);
        request.getRequestDispatcher("/views/samples.jsp").forward(request, response);
    }
}
