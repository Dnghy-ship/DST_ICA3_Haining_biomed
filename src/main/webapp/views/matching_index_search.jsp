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
    <title>Matching Search · Precision Medicine Matching System</title>

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
            <jsp:param name="active" value="matching" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>基因-药物匹配查询</h2>
            </div>

            <div class="alert alert-info" role="alert">
                <strong>精准医学匹配</strong> &mdash;
                输入患者样本 ID，系统将提取样本中的致病性基因变异，
                并在药物基因组学知识库中匹配相关药物及用药建议。
                匹配算法基于基因名称（Gene.refGene）与药物名称的关联，排除同义突变（synonymous SNV）。
            </div>

            <!-- 搜索表单 -->
            <div class="card mb-4">
                <div class="card-header">
                    <strong>样本查询</strong>
                </div>
                <div class="card-body">
                    <form action="<%=request.getContextPath()%>/matchingIndex" method="get" class="form-inline">
                        <label for="sampleId" class="mr-2">样本 ID：</label>
                        <input type="number" id="sampleId" name="sampleId" class="form-control mr-2"
                               placeholder="输入样本编号" min="1"
                               value="${param.sampleId}" />
                        <button type="submit" class="btn btn-primary">
                            &#128269; 开始匹配
                        </button>
                        <a href="<%=request.getContextPath()%>/samples" class="btn btn-outline-secondary ml-2">
                            查看所有样本
                        </a>
                    </form>
                </div>
            </div>

            <!-- 错误提示 -->
            <c:if test="${not empty errorMessage}">
                <div class="alert alert-warning" role="alert">
                    &#9888; ${errorMessage}
                </div>
            </c:if>

            <!-- 样本信息卡片 -->
            <c:if test="${sample != null}">
                <div class="card mb-4 border-primary">
                    <div class="card-header bg-primary text-white">
                        <strong>样本信息</strong>
                    </div>
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-4">
                                <dl class="row mb-0">
                                    <dt class="col-sm-5">样本 ID</dt>
                                    <dd class="col-sm-7">${sample.id}</dd>
                                    <dt class="col-sm-5">上传者</dt>
                                    <dd class="col-sm-7">${sample.uploadedBy}</dd>
                                    <dt class="col-sm-5">上传时间</dt>
                                    <dd class="col-sm-7">
                                        <fmt:formatDate value="${sample.createdAt}" pattern="yyyy-MM-dd HH:mm:ss" />
                                    </dd>
                                </dl>
                            </div>
                            <div class="col-md-4">
                                <dl class="row mb-0">
                                    <dt class="col-sm-6">非同义变异基因数</dt>
                                    <dd class="col-sm-6">
                                        <span class="badge badge-info" style="font-size:1rem;">${refGenes.size()}</span>
                                    </dd>
                                </dl>
                            </div>
                            <div class="col-md-4">
                                <dl class="row mb-0">
                                    <dt class="col-sm-6">匹配药物数</dt>
                                    <dd class="col-sm-6">
                                        <span class="badge badge-success" style="font-size:1rem;">${matchedDrugs.size()}</span>
                                    </dd>
                                </dl>
                            </div>
                        </div>
                    </div>
                </div>

                <!-- 检测到的基因变异 -->
                <c:if test="${not empty refGenes}">
                    <div class="card mb-4">
                        <div class="card-header">
                            <strong>检测到的非同义突变基因 (${refGenes.size()} 个)</strong>
                        </div>
                        <div class="card-body">
                            <c:forEach items="${refGenes}" var="gene">
                                <span class="badge badge-warning mr-1 mb-1" style="font-size:0.85rem;">${gene}</span>
                            </c:forEach>
                        </div>
                    </div>
                </c:if>

                <!-- 匹配药物结果 -->
                <div class="card mb-4">
                    <div class="card-header">
                        <strong>匹配药物结果</strong>
                    </div>
                    <div class="card-body">
                        <c:choose>
                            <c:when test="${empty matchedDrugs}">
                                <p class="text-muted">未在知识库中找到与该样本基因变异相关的药物。</p>
                            </c:when>
                            <c:otherwise>
                                <div class="table-responsive">
                                    <table class="table table-striped table-sm">
                                        <thead>
                                        <tr>
                                            <th>药物 ID</th>
                                            <th>药物名称</th>
                                            <th>生物标记物</th>
                                            <th>PharmGKB 链接</th>
                                        </tr>
                                        </thead>
                                        <tbody>
                                        <c:forEach items="${matchedDrugs}" var="drug">
                                            <tr>
                                                <td>${drug.id}</td>
                                                <td><strong>${drug.name}</strong></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${drug.biomarker}">
                                                            <span class="badge badge-success">有FDA指南</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge badge-secondary">仅研究证据</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty drug.drugUrl}">
                                                            <a href="${drug.drugUrl}" target="_blank" rel="noopener noreferrer">查看详情</a>
                                                        </c:when>
                                                        <c:otherwise>—</c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </c:if>

        </main>
    </div>
</div>
</body>
</html>
