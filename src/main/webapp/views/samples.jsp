<%--
  Samples page: lists uploaded samples with variant count, search by uploaded_by, pagination.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Samples · Precision Medicine</title>

    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
    <script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
    <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">
    <style>
        .bd-placeholder-img { font-size: 1.125rem; text-anchor: middle; -webkit-user-select: none; -moz-user-select: none; -ms-user-select: none; user-select: none; }
        @media (min-width: 768px) { .bd-placeholder-img-lg { font-size: 3.5rem; } }
    </style>
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
                <h2>Uploaded Samples
                    <small class="text-muted" style="font-size:0.6em;">&nbsp;(${page.totalCount} total)</small>
                </h2>
                <a href="<%=request.getContextPath()%>/matchingIndex" class="btn btn-sm btn-primary">
                    &#x2B06; Upload New Sample
                </a>
            </div>

            <p class="text-muted">Each row represents a patient sample uploaded via ANNOVAR. Click "Upload New Sample" to run a new pharmacogenomic analysis.</p>

            <%-- Search bar --%>
            <form method="get" action="samples" class="form-inline mb-3">
                <input type="hidden" name="pageSize" value="${page.pageSize}" />
                <div class="input-group">
                    <input type="text" name="q" class="form-control" placeholder="Search by uploaded by…" value="${q}">
                    <div class="input-group-append">
                        <button class="btn btn-outline-secondary" type="submit">Search</button>
                        <c:if test="${not empty q}">
                            <a class="btn btn-outline-danger" href="samples?pageSize=${page.pageSize}">Clear</a>
                        </c:if>
                    </div>
                </div>
            </form>

            <div class="table-responsive">
                <table class="table table-striped table-sm">
                    <thead class="thead-dark">
                    <tr>
                        <th>Sample ID</th>
                        <th>Uploaded By</th>
                        <th>Created At</th>
                        <th>Variant Count</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${page.items}" var="item">
                        <tr>
                            <td>${item.id}</td>
                            <td>${item.uploadedBy}</td>
                            <td>${item.createdAt}</td>
                            <td>
                                <span class="badge badge-info">${item.variantCount}</span>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty page.items}">
                        <tr><td colspan="4" class="text-center text-muted">No samples found. <a href="<%=request.getContextPath()%>/matchingIndex">Upload one now.</a></td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>

            <%-- Pagination UI --%>
            <c:if test="${page.totalPages > 1}">
            <nav aria-label="Samples pagination">
                <ul class="pagination flex-wrap">
                    <li class="page-item ${page.hasPrev ? '' : 'disabled'}">
                        <a class="page-link" href="samples?q=${q}&pageSize=${page.pageSize}&page=${page.page - 1}">Prev</a>
                    </li>
                    <c:set var="startP" value="${page.page - 4 < 1 ? 1 : page.page - 4}" />
                    <c:set var="endP"   value="${page.page + 4 > page.totalPages ? page.totalPages : page.page + 4}" />
                    <c:forEach var="p" begin="${startP}" end="${endP}">
                        <li class="page-item ${p == page.page ? 'active' : ''}">
                            <a class="page-link" href="samples?q=${q}&pageSize=${page.pageSize}&page=${p}">${p}</a>
                        </li>
                    </c:forEach>
                    <li class="page-item ${page.hasNext ? '' : 'disabled'}">
                        <a class="page-link" href="samples?q=${q}&pageSize=${page.pageSize}&page=${page.page + 1}">Next</a>
                    </li>
                </ul>
            </nav>
            <small class="text-muted">Page ${page.page} of ${page.totalPages} &mdash; ${page.totalCount} records</small>
            </c:if>

        </main>
    </div>
</div>
</body>
</html>
