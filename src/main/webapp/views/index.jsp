<%--
  Created by IntelliJ IDEA.
  User: hello
  Date: 2019-12-3
  Time: 15:37
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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
        .bd-placeholder-img {
            font-size: 1.125rem;
            text-anchor: middle;
            -webkit-user-select: none;
            -moz-user-select: none;
            -ms-user-select: none;
            user-select: none;
        }

        @media (min-width: 768px) {
            .bd-placeholder-img-lg {
                font-size: 3.5rem;
            }
        }
    </style>
</head>
<body>
<nav class="navbar navbar-dark fixed-top bg-dark flex-md-nowrap p-0 shadow">
    <a class="navbar-brand col-sm-3 col-md-2 mr-0" href="#">Precision Medicine Matching System</a>
</nav>

<div class="container-fluid">
    <div class="row">
        <jsp:include page="nav.jsp" >
            <jsp:param name="active" value="dashboard" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>Dashboard</h2>
            </div>

            <div class="alert alert-info" role="alert">
                <strong>欢迎使用精准医学匹配系统</strong> &mdash;
                本系统整合了药物基因组学知识库与患者基因变异数据，支持个性化用药决策。
                请从左侧导航栏进入各功能模块。
            </div>

            <!-- 统计卡片 -->
            <div class="row mb-4">
                <div class="col-md-3">
                    <div class="card text-white bg-primary mb-3">
                        <div class="card-body">
                            <h5 class="card-title">药物 (Drugs)</h5>
                            <p class="card-text display-4">${totalDrugs}</p>
                        </div>
                        <div class="card-footer">
                            <a href="<%=request.getContextPath()%>/drugs" class="text-white">查看详情 &rarr;</a>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card text-white bg-success mb-3">
                        <div class="card-body">
                            <h5 class="card-title">药物标签 (Drug Labels)</h5>
                            <p class="card-text display-4">${totalDrugLabels}</p>
                        </div>
                        <div class="card-footer">
                            <a href="<%=request.getContextPath()%>/drugLabels" class="text-white">查看详情 &rarr;</a>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card text-white bg-warning mb-3">
                        <div class="card-body">
                            <h5 class="card-title">用药指南 (Dosing Guidelines)</h5>
                            <p class="card-text display-4">${totalDosingGuidelines}</p>
                        </div>
                        <div class="card-footer">
                            <a href="<%=request.getContextPath()%>/dosingGuideline" class="text-white">查看详情 &rarr;</a>
                        </div>
                    </div>
                </div>
                <div class="col-md-3">
                    <div class="card text-white bg-info mb-3">
                        <div class="card-body">
                            <h5 class="card-title">样本 (Samples)</h5>
                            <p class="card-text display-4">${totalSamples}</p>
                        </div>
                        <div class="card-footer">
                            <a href="<%=request.getContextPath()%>/samples" class="text-white">查看详情 &rarr;</a>
                        </div>
                    </div>
                </div>
            </div>

            <!-- 快速操作 -->
            <h5>快速操作</h5>
            <div class="mt-2 mb-4">
                <a href="<%=request.getContextPath()%>/matchingIndex" class="btn btn-primary mr-2">
                    &#128269; 基因-药物匹配查询
                </a>
                <a href="<%=request.getContextPath()%>/drugs" class="btn btn-outline-primary mr-2">
                    &#128137; 浏览药物库
                </a>
                <a href="<%=request.getContextPath()%>/counter" class="btn btn-outline-secondary">
                    &#128202; Visitor Counter Demo (Week 3 Practical)
                </a>
            </div>
        </main>
    </div>
</div>
</body>
</html>