<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>
<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Dashboard – Precision Medicine Matching System</title>
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
            <jsp:param name="active" value="dashboard" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>Dashboard</h2>
            </div>

            <p class="text-muted">Welcome to the Precision Medicine Matching System.</p>

            <!-- Statistics cards -->
            <div class="row mt-3">
                <div class="col-sm-6 col-lg-3 mb-3">
                    <div class="card text-white bg-primary">
                        <div class="card-body">
                            <h5 class="card-title">Drugs</h5>
                            <p class="card-text display-4">${drugCount}</p>
                        </div>
                        <div class="card-footer">
                            <a href="<%=request.getContextPath()%>/drugs" class="text-white small">View all &raquo;</a>
                        </div>
                    </div>
                </div>
                <div class="col-sm-6 col-lg-3 mb-3">
                    <div class="card text-white bg-success">
                        <div class="card-body">
                            <h5 class="card-title">Drug Labels</h5>
                            <p class="card-text display-4">${drugLabelCount}</p>
                        </div>
                        <div class="card-footer">
                            <a href="<%=request.getContextPath()%>/drugLabels" class="text-white small">View all &raquo;</a>
                        </div>
                    </div>
                </div>
                <div class="col-sm-6 col-lg-3 mb-3">
                    <div class="card text-white bg-info">
                        <div class="card-body">
                            <h5 class="card-title">Dosing Guidelines</h5>
                            <p class="card-text display-4">${dosingGuidelineCount}</p>
                        </div>
                        <div class="card-footer">
                            <a href="<%=request.getContextPath()%>/dosingGuideline" class="text-white small">View all &raquo;</a>
                        </div>
                    </div>
                </div>
                <div class="col-sm-6 col-lg-3 mb-3">
                    <div class="card text-white bg-secondary">
                        <div class="card-body">
                            <h5 class="card-title">Samples</h5>
                            <p class="card-text display-4">${sampleCount}</p>
                        </div>
                        <div class="card-footer">
                            <a href="<%=request.getContextPath()%>/samples" class="text-white small">View all &raquo;</a>
                        </div>
                    </div>
                </div>
            </div>

            <div class="mt-3">
                <a href="<%=request.getContextPath()%>/counter" class="btn btn-outline-primary">
                    &#128202; Visitor Counter Demo (Week 3 Practical)
                </a>
            </div>
        </main>
    </div>
</div>
</body>
</html>