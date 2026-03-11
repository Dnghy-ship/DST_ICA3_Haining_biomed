<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Samples – Precision Medicine Matching System</title>
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
            <div class="table-responsive">
                <table class="table table-striped table-sm">
                    <thead>
                    <tr>
                        <th>#</th>
                        <th>Uploaded By</th>
                        <th>Uploaded At</th>
                        <th>Variant Count</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${samples}" var="sample">
                        <tr>
                            <td>${sample.id}</td>
                            <td>${sample.uploadedBy}</td>
                            <td><fmt:formatDate value="${sample.createdAt}" pattern="yyyy-MM-dd HH:mm:ss" /></td>
                            <td>${variantCounts[sample.id]}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
                <c:if test="${empty samples}">
                    <p class="text-muted">No samples uploaded yet.</p>
                </c:if>
            </div>
        </main>
    </div>
</div>
</body>
</html>
