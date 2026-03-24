<%--
  Matching Result (Saved) - Displays previously saved matching results for a sample.
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
    <title>Saved Matching Result · Precision Medicine Matching System</title>

    <!-- Bootstrap core CSS -->
    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
    <script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
    <!-- Custom styles for this template -->
    <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">
    <style>
        .accordion .card-header { cursor: pointer; }
        .gene-tag { font-size: .75rem; }
        .score-badge { font-size: .8rem; }
    </style>
</head>
<body>
<nav class="navbar navbar-dark fixed-top bg-dark flex-md-nowrap p-0 shadow">
    <a class="navbar-brand col-sm-3 col-md-2 mr-0" href="#">Precision Medicine Matching System</a>
</nav>

<div class="container-fluid">
    <div class="row">
        <jsp:include page="nav.jsp">
            <jsp:param name="active" value="samples"/>
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>Saved Matching Report</h2>
                <div>
                    <a href="<%=request.getContextPath()%>/matching?sampleId=${sample.id}"
                       class="btn btn-sm btn-outline-primary mr-2">Re-run Matching</a>
                    <a href="<%=request.getContextPath()%>/samples" class="btn btn-sm btn-outline-secondary">← Back to Samples</a>
                </div>
            </div>

            <!-- Sample Info -->
            <div class="alert alert-secondary" role="alert">
                <h4 class="alert-heading">Sample Info #${sample.id}</h4>
                <div><strong>Uploaded at:</strong> ${sample.createdAt}</div>
                <div><strong>Uploaded by:</strong> ${sample.uploadedBy}</div>
            </div>

            <!-- Matched Drug Labels (Saved) -->
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h4 class="mb-0">Matched Drug Labels
                    <span class="badge badge-secondary ml-2">${matched.size()} result(s)</span>
                </h4>
            </div>

            <c:choose>
                <c:when test="${not empty matched}">
                    <div class="accordion" id="savedMatchingAccordion">
                        <c:forEach items="${matched}" var="item" varStatus="loop">
                            <div class="card mb-1">
                                <div class="card-header p-0" id="savedHeading${loop.index}">
                                    <button class="btn btn-link btn-block text-left px-3 py-2 d-flex align-items-center justify-content-between"
                                            type="button"
                                            data-toggle="collapse"
                                            data-target="#savedCollapse${loop.index}"
                                            aria-expanded="${loop.index == 0 ? 'true' : 'false'}"
                                            aria-controls="savedCollapse${loop.index}">
                                        <span>
                                            <strong>${item.name}</strong>
                                            <c:forEach items="${item.matchedGenes}" var="gene">
                                                <span class="badge badge-danger gene-tag ml-1">${gene}</span>
                                            </c:forEach>
                                        </span>
                                        <span>
                                            <span class="badge badge-primary score-badge mr-1">Score: ${item.score}</span>
                                            <c:choose>
                                                <c:when test="${item.recommendationLevel == 'Strong'}">
                                                    <span class="badge badge-success score-badge">Strong</span>
                                                </c:when>
                                                <c:when test="${item.recommendationLevel == 'Moderate'}">
                                                    <span class="badge badge-warning score-badge">Moderate</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-secondary score-badge">Optional</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </span>
                                    </button>
                                </div>
                                <div id="savedCollapse${loop.index}"
                                     class="collapse ${loop.index == 0 ? 'show' : ''}"
                                     aria-labelledby="savedHeading${loop.index}"
                                     data-parent="#savedMatchingAccordion">
                                    <div class="card-body">
                                        <dl class="row mb-0">
                                            <dt class="col-sm-2">Source</dt>
                                            <dd class="col-sm-10">${item.source}</dd>
                                            <dt class="col-sm-2">Dosing Info</dt>
                                            <dd class="col-sm-10">${item.dosingInformation}</dd>
                                            <dt class="col-sm-2">Alternate Drug</dt>
                                            <dd class="col-sm-10">${item.alternateDrugAvailable}</dd>
                                            <dt class="col-sm-2">Summary</dt>
                                            <dd class="col-sm-10">
                                                <div style="max-height: 200px; overflow-y: auto; white-space: pre-wrap; font-size: .85rem;">${item.summaryMarkdown}</div>
                                            </dd>
                                        </dl>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="alert alert-warning">No saved results found for this sample.</div>
                </c:otherwise>
            </c:choose>
        </main>
    </div>
</div>
</body>
</html>
