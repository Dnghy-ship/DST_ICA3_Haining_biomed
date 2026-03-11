package cn.edu.zju.servlet;

import cn.edu.zju.bean.Drug;
import cn.edu.zju.dao.DrugDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(name = "DrugServlet",  urlPatterns = "/drugs")
public class DrugServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        DrugDao drugDao = new DrugDao();
        List<Drug> all = drugDao.findAll();

        // Normalise drugUrl: promote http:// to https:// (PharmGKB supports HTTPS).
        // Any URL that does not start with http:// or https:// is blanked out to
        // prevent javascript: / data: URIs from reaching the JSP.
        for (Drug drug : all) {
            drug.setDrugUrl(sanitiseUrl(drug.getDrugUrl()));
        }

        request.setAttribute("drugs", all);
        request.getRequestDispatcher("/views/drugs.jsp").forward(request, response);
    }

    /**
     * Returns a safe HTTPS URL, or null if the input is blank or uses a
     * non-http(s) scheme.
     */
    static String sanitiseUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return null;
        }
        String trimmed = url.trim();
        if (trimmed.startsWith("https://")) {
            return trimmed;
        }
        if (trimmed.startsWith("http://")) {
            return "https://" + trimmed.substring("http://".length());
        }
        // Reject any other scheme (javascript:, data:, etc.)
        return null;
    }
}
