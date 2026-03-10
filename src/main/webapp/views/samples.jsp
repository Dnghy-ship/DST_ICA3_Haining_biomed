<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false" %>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Samples · Precision Medicine Matching System</title>

    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
    <script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
    <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">
</head>
<body>
<jsp:include page="head.jsp" />

<div class="container-fluid">
    <div class="row">
        <jsp:include page="nav.jsp">
            <jsp:param name="active" value="samples" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>Samples</h2>
            </div>

            <div class="alert alert-info" role="alert">
                <strong>样本列表</strong> &mdash;
                本页面展示已上传的基因检测样本，每个样本包含患者的基因变异注释数据（ANNOVAR格式）。
                "变异数量" 列显示每个样本中检测到的基因变异条目数。
                点击 "变异匹配" 可查看该样本的药物基因组学匹配结果。
            </div>

            <div class="table-responsive">
                <table class="table table-striped table-sm">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>上传者</th>
                        <th>上传时间</th>
                        <th>变异数量</th>
                        <th>操作</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:choose>
                        <c:when test="${empty samples}">
                            <tr>
                                <td colspan="5" class="text-center text-muted">暂无样本数据</td>
                            </tr>
                        </c:when>
                        <c:otherwise>
                            <c:forEach items="${samples}" var="item">
                                <tr>
                                    <td>${item.id}</td>
                                    <td>${item.uploadedBy}</td>
                                    <td><fmt:formatDate value="${item.createdAt}" pattern="yyyy-MM-dd HH:mm:ss" /></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${variantCounts[item.id] != null}">
                                                <span class="badge badge-info">${variantCounts[item.id]}</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge badge-light">0</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <a href="<%=request.getContextPath()%>/matchingIndex?sampleId=${item.id}"
                                           class="btn btn-sm btn-primary">变异匹配</a>
                                    </td>
                                </tr>
                            </c:forEach>
                        </c:otherwise>
                    </c:choose>
                    </tbody>
                </table>
            </div>
        </main>
    </div>
</div>
</body>
</html>
