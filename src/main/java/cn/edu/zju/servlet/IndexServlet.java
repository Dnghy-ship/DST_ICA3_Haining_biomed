package cn.edu.zju.servlet;

import cn.edu.zju.dao.DosingGuidelineDao;
import cn.edu.zju.dao.DrugDao;
import cn.edu.zju.dao.DrugLabelDao;
import cn.edu.zju.dao.SampleDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(name = "IndexServlet", urlPatterns = {"/index"})
public class IndexServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("drugCount", new DrugDao().countAll());
        request.setAttribute("drugLabelCount", new DrugLabelDao().countAll());
        request.setAttribute("dosingGuidelineCount", new DosingGuidelineDao().countAll());
        request.setAttribute("sampleCount", new SampleDao().countAll());
        request.getRequestDispatcher("/views/index.jsp").forward(request, response);
    }
}
