package cn.edu.zju.servlet;

import cn.edu.zju.bean.Drug;
import cn.edu.zju.bean.Page;
import cn.edu.zju.dao.DrugDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "DrugServlet", urlPatterns = "/drugs")
public class DrugServlet extends HttpServlet {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        // --- parse query params ---
        String q    = RequestParamUtils.trimOrNull(request.getParameter("q"));
        String sort = DrugDao.validateSortColumn(request.getParameter("sort"));
        String dir  = DrugDao.validateSortDir(request.getParameter("dir"));

        int pageSize = RequestParamUtils.parsePositiveInt(request.getParameter("pageSize"), DEFAULT_PAGE_SIZE, MAX_PAGE_SIZE);
        int page     = RequestParamUtils.parsePositiveInt(request.getParameter("page"), 1, Integer.MAX_VALUE);

        // --- query ---
        DrugDao drugDao = new DrugDao();
        int totalCount = drugDao.count(q);
        int totalPages = (totalCount == 0) ? 1 : (int) Math.ceil((double) totalCount / pageSize);
        page = Math.min(page, totalPages);  // clamp to valid range

        int offset = (page - 1) * pageSize;
        List<Drug> items = drugDao.findPage(q, sort, dir, offset, pageSize);

        Page<Drug> pageObj = new Page<>(items, page, pageSize, totalCount);

        // --- pass to JSP ---
        request.setAttribute("page", pageObj);
        request.setAttribute("q", q == null ? "" : q);
        request.setAttribute("sort", sort);
        request.setAttribute("dir", dir);
        request.getRequestDispatcher("/views/drugs.jsp").forward(request, response);
    }

}


