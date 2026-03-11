<%--
  Created by IntelliJ IDEA.
  User: hello
  Date: 2019-12-3
  Time: 15:37
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <meta name="description" content="">
    <meta name="author" content="">
    <meta name="generator" content="">
    <title>Dashboard Template · Bootstrap</title>

    <!-- Bootstrap core CSS -->
    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
    <script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
    <!-- Custom styles for this template -->
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
            <jsp:param name="active" value="drug_labels" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>Drug Labels
                    <small class="text-muted" style="font-size:0.6em;">&nbsp;(${page.totalCount} total)</small>
                </h2>
            </div>

            <p class="text-muted">FDA and international drug labels with pharmacogenomic dosing information sourced from PharmGKB.</p>

            <%-- Search bar --%>
            <form method="get" action="drugLabels" class="form-inline mb-3">
                <input type="hidden" name="pageSize" value="${page.pageSize}" />
                <div class="input-group">
                    <input type="text" name="q" class="form-control" placeholder="Search source or summary…" value="${q}">
                    <div class="input-group-append">
                        <button class="btn btn-outline-secondary" type="submit">Search</button>
                        <c:if test="${not empty q}">
                            <a class="btn btn-outline-danger" href="drugLabels?pageSize=${page.pageSize}">Clear</a>
                        </c:if>
                    </div>
                </div>
            </form>

            <div class="table-responsive">
                <table class="table table-striped table-sm">
                    <thead class="thead-dark">
                    <tr>
                        <th>#</th>
                        <th>Source</th>
                        <th>Dosing Information</th>
                        <th>Summary</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${page.items}" var="item">
                        <tr>
                            <td>${item.id}</td>
                            <td>${item.source}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${item.dosingInformation}">
                                        <span class="badge badge-success">Yes</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-secondary">No</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                            <td>${item.summaryMarkdown}</td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty page.items}">
                        <tr><td colspan="4" class="text-center text-muted">No drug labels found.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>

            <%-- Pagination UI --%>
            <c:if test="${page.totalPages > 1}">
            <nav aria-label="Drug labels pagination">
                <ul class="pagination flex-wrap">
                    <li class="page-item ${page.hasPrev ? '' : 'disabled'}">
                        <a class="page-link" href="drugLabels?q=${q}&pageSize=${page.pageSize}&page=${page.page - 1}">Prev</a>
                    </li>
                    <c:set var="startP" value="${page.page - 4 < 1 ? 1 : page.page - 4}" />
                    <c:set var="endP"   value="${page.page + 4 > page.totalPages ? page.totalPages : page.page + 4}" />
                    <c:forEach var="p" begin="${startP}" end="${endP}">
                        <li class="page-item ${p == page.page ? 'active' : ''}">
                            <a class="page-link" href="drugLabels?q=${q}&pageSize=${page.pageSize}&page=${p}">${p}</a>
                        </li>
                    </c:forEach>
                    <li class="page-item ${page.hasNext ? '' : 'disabled'}">
                        <a class="page-link" href="drugLabels?q=${q}&pageSize=${page.pageSize}&page=${page.page + 1}">Next</a>
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

