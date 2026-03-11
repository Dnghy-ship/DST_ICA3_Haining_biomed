package cn.edu.zju.servlet;

import cn.edu.zju.bean.Page;
import cn.edu.zju.bean.Sample;
import cn.edu.zju.dao.SampleDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "SamplesServlet", urlPatterns = "/samples")
public class SamplesServlet extends HttpServlet {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String q     = RequestParamUtils.trimOrNull(request.getParameter("q"));
        int pageSize = RequestParamUtils.parsePositiveInt(request.getParameter("pageSize"), DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
        int page     = RequestParamUtils.parsePositiveInt(request.getParameter("page"), 1, Integer.MAX_VALUE);

        SampleDao sampleDao = new SampleDao();
        int totalCount = sampleDao.count(q);
        int totalPages = (totalCount == 0) ? 1 : (int) Math.ceil((double) totalCount / pageSize);
        page = Math.min(page, totalPages);

        int offset = (page - 1) * pageSize;
        List<Sample> items = sampleDao.findPage(q, offset, pageSize);

        Page<Sample> pageObj = new Page<>(items, page, pageSize, totalCount);

        request.setAttribute("page", pageObj);
        request.setAttribute("q", q == null ? "" : q);
        request.getRequestDispatcher("/views/samples.jsp").forward(request, response);
    }

}

