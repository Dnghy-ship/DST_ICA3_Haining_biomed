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
        th a { color: inherit; text-decoration: none; }
        th a:hover { text-decoration: underline; }
    </style>
</head>
<body>
<jsp:include page="head.jsp" />

<div class="container-fluid">
    <div class="row">
        <jsp:include page="nav.jsp">
            <jsp:param name="active" value="drugs" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>Drugs
                    <small class="text-muted" style="font-size:0.6em;">&nbsp;(${page.totalCount} total)</small>
                </h2>
            </div>

            <p class="text-muted">Browse all pharmacogenomic drugs imported from PharmGKB. Use the search box to filter by name, and click column headers to sort.</p>

            <%-- Search bar --%>
            <form method="get" action="drugs" class="form-inline mb-3">
                <input type="hidden" name="sort" value="${sort}" />
                <input type="hidden" name="dir"  value="${dir}"  />
                <input type="hidden" name="pageSize" value="${page.pageSize}" />
                <div class="input-group">
                    <input type="text" name="q" class="form-control" placeholder="Search drug name…" value="${q}">
                    <div class="input-group-append">
                        <button class="btn btn-outline-secondary" type="submit">Search</button>
                        <c:if test="${not empty q}">
                            <a class="btn btn-outline-danger" href="drugs?sort=${sort}&dir=${dir}&pageSize=${page.pageSize}">Clear</a>
                        </c:if>
                    </div>
                </div>
            </form>

            <div class="table-responsive">
                <table class="table table-striped table-sm table-hover">
                    <thead class="thead-dark">
                    <tr>
                        <th>#</th>
                        <th>
                            <%-- Name sort link --%>
                            <a href="drugs?q=${q}&sort=name&dir=${sort == 'name' && dir == 'asc' ? 'desc' : 'asc'}&page=1&pageSize=${page.pageSize}">
                                Name
                                <c:if test="${sort == 'name'}">
                                    <c:choose>
                                        <c:when test="${dir == 'asc'}">&#9650;</c:when>
                                        <c:otherwise>&#9660;</c:otherwise>
                                    </c:choose>
                                </c:if>
                            </a>
                        </th>
                        <th>Drug URL</th>
                        <th>
                            <a href="drugs?q=${q}&sort=biomarker&dir=${sort == 'biomarker' && dir == 'asc' ? 'desc' : 'asc'}&page=1&pageSize=${page.pageSize}">
                                Biomarker
                                <c:if test="${sort == 'biomarker'}">
                                    <c:choose>
                                        <c:when test="${dir == 'asc'}">&#9650;</c:when>
                                        <c:otherwise>&#9660;</c:otherwise>
                                    </c:choose>
                                </c:if>
                            </a>
                        </th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${page.items}" var="item">
                        <tr>
                            <td>${item.id}</td>
                            <td>
                                <c:choose>
                                    <c:when test="${not empty item.drugUrl}">
                                        <a href="${item.drugUrl}" target="_blank" rel="noopener noreferrer">${item.name}</a>
                                    </c:when>
                                    <c:otherwise>${item.name}</c:otherwise>
                                </c:choose>
                            </td>
                            <td>
                                <c:if test="${not empty item.drugUrl}">
                                    <a href="${item.drugUrl}" target="_blank" rel="noopener noreferrer">${item.drugUrl}</a>
                                </c:if>
                            </td>
                            <td>
                                <c:choose>
                                    <c:when test="${item.biomarker}">
                                        <span class="badge badge-success">Yes</span>
                                    </c:when>
                                    <c:otherwise>
                                        <span class="badge badge-secondary">No</span>
                                    </c:otherwise>
                                </c:choose>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty page.items}">
                        <tr><td colspan="4" class="text-center text-muted">No drugs found.</td></tr>
                    </c:if>
                    </tbody>
                </table>
            </div>

            <%-- Pagination UI --%>
            <c:if test="${page.totalPages > 1}">
            <nav aria-label="Drug list pagination">
                <ul class="pagination flex-wrap">
                    <%-- Prev --%>
                    <li class="page-item ${page.hasPrev ? '' : 'disabled'}">
                        <a class="page-link" href="drugs?q=${q}&sort=${sort}&dir=${dir}&pageSize=${page.pageSize}&page=${page.page - 1}">Prev</a>
                    </li>

                    <%-- Page numbers (show up to 9 around current) --%>
                    <c:set var="startP" value="${page.page - 4 < 1 ? 1 : page.page - 4}" />
                    <c:set var="endP"   value="${page.page + 4 > page.totalPages ? page.totalPages : page.page + 4}" />
                    <c:forEach var="p" begin="${startP}" end="${endP}">
                        <li class="page-item ${p == page.page ? 'active' : ''}">
                            <a class="page-link" href="drugs?q=${q}&sort=${sort}&dir=${dir}&pageSize=${page.pageSize}&page=${p}">${p}</a>
                        </li>
                    </c:forEach>

                    <%-- Next --%>
                    <li class="page-item ${page.hasNext ? '' : 'disabled'}">
                        <a class="page-link" href="drugs?q=${q}&sort=${sort}&dir=${dir}&pageSize=${page.pageSize}&page=${page.page + 1}">Next</a>
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

