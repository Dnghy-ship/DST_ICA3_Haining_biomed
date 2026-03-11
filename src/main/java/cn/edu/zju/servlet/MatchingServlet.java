package cn.edu.zju.servlet;

import cn.edu.zju.bean.Drug;
import cn.edu.zju.bean.Sample;
import cn.edu.zju.dao.AnnovarDao;
import cn.edu.zju.dao.DrugDao;
import cn.edu.zju.dao.SampleDao;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet(name = "MatchingServlet", urlPatterns = "/matchingIndex")
public class MatchingServlet extends HttpServlet {
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String sampleIdParam = request.getParameter("sampleId");

        if (sampleIdParam != null && !sampleIdParam.trim().isEmpty()) {
            try {
                int sampleId = Integer.parseInt(sampleIdParam.trim());
                SampleDao sampleDao = new SampleDao();
                AnnovarDao annovarDao = new AnnovarDao();
                DrugDao drugDao = new DrugDao();

                Sample sample = sampleDao.findById(sampleId);
                if (sample != null) {
                    // 获取样本中的基因列表（排除同义SNV）
                    List<String> refGenes = annovarDao.getRefGenes(sampleId);
                    // 获取所有药物并匹配
                    List<Drug> allDrugs = drugDao.findAll();
                    List<Drug> matchedDrugs = new ArrayList<>();
                    for (Drug drug : allDrugs) {
                        String drugName = drug.getName() == null ? "" : drug.getName().toLowerCase();
                        for (String gene : refGenes) {
                            if (gene != null && drugName.contains(gene.toLowerCase())) {
                                matchedDrugs.add(drug);
                                break;
                            }
                        }
                    }

                    request.setAttribute("sample", sample);
                    request.setAttribute("refGenes", refGenes);
                    request.setAttribute("matchedDrugs", matchedDrugs);
                } else {
                    request.setAttribute("errorMessage", "未找到样本 ID: " + sampleId);
                }
            } catch (NumberFormatException e) {
                request.setAttribute("errorMessage", "无效的样本 ID，请输入数字。");
            }
        }

        request.getRequestDispatcher("/views/matching_index_search.jsp").forward(request, response);
    }
}
