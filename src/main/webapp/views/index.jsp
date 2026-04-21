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
    <title>Dashboard · Precision Medicine Matching System</title>

    <!-- Bootstrap core CSS -->
    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
    <script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
    <!-- Custom styles for this template -->
    <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">
    <style>
        .stat-card { border-left: 4px solid; }
        .stat-card.primary { border-color: #007bff; }
        .stat-card.success { border-color: #28a745; }
        .stat-card.info    { border-color: #17a2b8; }
        .stat-card.warning { border-color: #ffc107; }
        .stat-card .stat-value { font-size: 2rem; font-weight: 700; }
        .stat-card .stat-label { font-size: .85rem; color: #6c757d; text-transform: uppercase; }
    </style>
</head>
<body>
<nav class="navbar navbar-dark fixed-top bg-dark flex-md-nowrap p-0 shadow">
    <a class="navbar-brand col-sm-3 col-md-2 mr-0" href="#">Precision Medicine Matching System</a>
</nav>

<div class="container-fluid">
    <div class="row">
        <jsp:include page="nav.jsp">
            <jsp:param name="active" value="dashboard"/>
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>Dashboard</h2>
            </div>

            <!-- Stats Overview -->
            <div class="row mb-4">
                <div class="col-md-3 mb-3">
                    <div class="card stat-card primary shadow-sm">
                        <div class="card-body">
                            <div class="stat-label">Total Samples</div>
                            <div class="stat-value text-primary">
                                <c:choose>
                                    <c:when test="${not empty stats}">${stats.totalSamples}</c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </div>
                            <small class="text-muted">Uploaded genomic files</small>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="card stat-card success shadow-sm">
                        <div class="card-body">
                            <div class="stat-label">Total Variants</div>
                            <div class="stat-value text-success">
                                <c:choose>
                                    <c:when test="${not empty stats}">${stats.totalVariants}</c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </div>
                            <small class="text-muted">Annotated genomic variants</small>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="card stat-card info shadow-sm">
                        <div class="card-body">
                            <div class="stat-label">Total Drugs</div>
                            <div class="stat-value text-info">
                                <c:choose>
                                    <c:when test="${not empty stats}">${stats.totalDrugs}</c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </div>
                            <small class="text-muted">Knowledge base drugs</small>
                        </div>
                    </div>
                </div>
                <div class="col-md-3 mb-3">
                    <div class="card stat-card warning shadow-sm">
                        <div class="card-body">
                            <div class="stat-label">Total Guidelines</div>
                            <div class="stat-value text-warning">
                                <c:choose>
                                    <c:when test="${not empty stats}">${stats.totalGuidelines}</c:when>
                                    <c:otherwise>—</c:otherwise>
                                </c:choose>
                            </div>
                            <small class="text-muted">Knowledge base dosing guidelines</small>
                        </div>
                    </div>
                </div>
            </div>

            <div class="mb-4">
                <a href="<%=request.getContextPath()%>/matchingIndex" class="btn btn-sm btn-primary mr-1">New Matching</a>
                <a href="<%=request.getContextPath()%>/samples" class="btn btn-sm btn-outline-secondary mr-1">View Samples</a>
                <a href="<%=request.getContextPath()%>/drugs" class="btn btn-sm btn-outline-info mr-1">View Drugs</a>
                <a href="<%=request.getContextPath()%>/dosingGuideline" class="btn btn-sm btn-outline-warning">View Guidelines</a>
            </div>

            <!-- Recent Matching Activities -->
            <div class="d-flex justify-content-between align-items-center mb-2">
                <h5 class="mb-0">Recent Matching Activities</h5>
                <a href="<%=request.getContextPath()%>/samples" class="btn btn-sm btn-outline-primary">View All</a>
            </div>
            <c:choose>
                <c:when test="${not empty stats and not empty stats.recentActivities}">
                    <div class="table-responsive">
                        <table class="table table-hover table-sm">
                            <thead class="thead-light">
                            <tr>
                                <th>#</th>
                                <th>Uploaded By</th>
                                <th>Uploaded At</th>
                                <th>Action</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${stats.recentActivities}" var="item">
                                <tr>
                                    <td>${item.id}</td>
                                    <td>${item.uploadedBy}</td>
                                    <td>${item.createdAt}</td>
                                    <td>
                                        <a href="<%=request.getContextPath()%>/matching?sampleId=${item.id}"
                                           class="btn btn-sm btn-outline-primary">Run Matching</a>
                                    </td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="alert alert-light text-muted">
                        No matching activities yet. <a href="<%=request.getContextPath()%>/matchingIndex">Upload a sample</a> to get started.
                    </div>
                </c:otherwise>
            </c:choose>
        </main>
    </div>
</div>
</body>
</html>
