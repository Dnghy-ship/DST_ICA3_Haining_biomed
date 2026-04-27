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
    <title>Matching Result · Precision Medicine Matching System</title>

    <!-- Bootstrap core CSS -->
    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
    <script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/html2pdf.js/0.10.1/html2pdf.bundle.min.js"
            integrity="sha384-Yv5O+t3uE3hunW8uyrbpPW3iw6/5/Y7HitWJBLgqfMoA36NogMmy+8wWZMpn3HWc"
            crossorigin="anonymous"></script>
    <!-- Custom styles for this template -->
    <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">
    <style>
        .accordion .card-header { cursor: pointer; }
        .gene-tag { font-size: .75rem; }
        .score-badge { font-size: .8rem; }
        .pdf-render-template {
            position: absolute;
            left: -10000px;
            top: 0;
            width: 190mm;
            background: #fff;
            color: #212529;
            padding: 10mm;
            font-size: .85rem;
            line-height: 1.45;
        }
        .pdf-render-template h3,
        .pdf-render-template h5 {
            margin-bottom: .5rem;
        }
        .pdf-report-table th,
        .pdf-report-table td {
            vertical-align: top;
            word-break: break-word;
            overflow-wrap: anywhere;
        }
        .pdf-report-table {
            width: 100%;
            table-layout: fixed;
        }
        .pdf-summary-block {
            white-space: pre-wrap;
            border: 1px solid #dee2e6;
            border-radius: .25rem;
            padding: .75rem;
            margin-bottom: .75rem;
            background: #f8f9fa;
        }
    </style>
</head>
<body>
<nav class="navbar navbar-dark fixed-top bg-dark flex-md-nowrap p-0 shadow">
    <a class="navbar-brand col-sm-3 col-md-2 mr-0" href="#">Precision Medicine Matching System</a>
</nav>

<div class="container-fluid">
    <div class="row">
        <jsp:include page="nav.jsp">
            <jsp:param name="active" value="matching_index"/>
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>Matching Result</h2>
                <a href="<%=request.getContextPath()%>/samples" class="btn btn-sm btn-outline-secondary">← Back to Samples</a>
            </div>

            <!-- Sample Info -->
            <div class="alert alert-info" role="alert">
                <h4 class="alert-heading">Sample Info #${sample.id}</h4>
                <div><strong>Uploaded at:</strong> ${sample.createdAt}</div>
                <div><strong>Uploaded by:</strong> ${sample.uploadedBy}</div>
            </div>

            <!-- Matched Drug Labels -->
            <div class="d-flex justify-content-between align-items-center mb-3">
                <h4 class="mb-0">Matched Drug Labels
                    <span class="badge badge-secondary ml-2">${matched.size()} result(s)</span>
                </h4>
                <button type="button"
                        id="exportClinicalReportBtn"
                        class="btn btn-primary btn-sm"
                        data-sample-id="<c:out value='${sample.id}'/>"
                        ${empty matched ? 'disabled="disabled"' : ''}>
                    <span class="mr-1">&#11015;</span>Export Clinical Report (PDF)
                </button>
            </div>

            <c:choose>
                <c:when test="${not empty matched}">
                    <div class="accordion" id="matchingAccordion">
                        <c:forEach items="${matched}" var="item" varStatus="loop">
                            <div class="card mb-1 ${item.score >= 8 ? 'border-success' : (item.score >= 4 ? 'border-warning' : 'border-secondary')}">
                                <div class="card-header p-0" id="heading${loop.index}">
                                    <button class="btn btn-link btn-block text-left px-3 py-2 d-flex align-items-center justify-content-between"
                                            type="button"
                                            data-toggle="collapse"
                                            data-target="#collapse${loop.index}"
                                            aria-expanded="${loop.index == 0 || item.score >= 8 ? 'true' : 'false'}"
                                            aria-controls="collapse${loop.index}">
                                        <span>
                                            <strong>${item.name}</strong>
                                            <c:forEach items="${item.matchedGenes}" var="gene">
                                                <span class="badge badge-danger gene-tag ml-1">${gene}</span>
                                            </c:forEach>
                                        </span>
                                        <span>
                                            <span class="badge ${item.score >= 8 ? 'badge-success' : (item.score >= 4 ? 'badge-warning' : 'badge-secondary')} score-badge mr-1">Score: ${item.score}</span>
                                            <c:choose>
                                                <c:when test="${empty item.recommendationLevel}">
                                                    <span class="badge badge-light score-badge">Unrated</span>
                                                </c:when>
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
                                <div id="collapse${loop.index}"
                                     class="collapse ${loop.index == 0 || item.score >= 8 ? 'show' : ''}"
                                     aria-labelledby="heading${loop.index}"
                                     data-parent="#matchingAccordion">
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
                    <div class="alert alert-warning">No drug labels matched for this sample's variants.</div>
                </c:otherwise>
            </c:choose>

            <div id="clinicalReportPdfTemplate" class="pdf-render-template">
                <div class="border-bottom pb-2 mb-3">
                    <h3 class="mb-1">Clinical Pharmacogenomic Matching Report</h3>
                    <div class="text-muted">Precision Medicine Matching System</div>
                </div>

                <div class="mb-3">
                    <h5>Patient Sample Information</h5>
                    <table class="table table-sm table-bordered mb-0">
                        <tbody>
                        <tr>
                            <th style="width: 30%;">Sample ID</th>
                            <td><c:out value="${sample.id}"/></td>
                        </tr>
                        <tr>
                            <th>Uploaded At</th>
                            <td><c:out value="${sample.createdAt}"/></td>
                        </tr>
                        <tr>
                            <th>Uploaded By</th>
                            <td><c:out value="${sample.uploadedBy}"/></td>
                        </tr>
                        <tr>
                            <th>Total Matched Labels</th>
                            <td>${matched.size()}</td>
                        </tr>
                        </tbody>
                    </table>
                </div>

                <h5 class="mb-2">Matched Drug Label Summary</h5>
                <c:choose>
                    <c:when test="${not empty matched}">
                        <table class="table table-sm table-bordered pdf-report-table mb-3">
                            <thead class="thead-light">
                            <tr>
                                <th>Drug Label</th>
                                <th>Score</th>
                                <th>Recommendation</th>
                                <th>Matched Genes</th>
                                <th>Source</th>
                            </tr>
                            </thead>
                            <tbody>
                            <c:forEach items="${matched}" var="item">
                                <tr>
                                    <td><c:out value="${item.name}"/></td>
                                    <td><c:out value="${item.score}"/></td>
                                    <td><c:out value="${empty item.recommendationLevel ? 'Unrated' : item.recommendationLevel}"/></td>
                                    <td><c:forEach items="${item.matchedGenes}" var="gene" varStatus="gs"><c:out value="${gene}"/><c:if test="${!gs.last}">, </c:if></c:forEach></td>
                                    <td><c:out value="${item.source}"/></td>
                                </tr>
                            </c:forEach>
                            </tbody>
                        </table>

                        <h5 class="mb-2">Clinical Detail Notes</h5>
                        <c:forEach items="${matched}" var="item" varStatus="loop">
                            <div class="border rounded p-2 mb-2">
                                <div><strong><c:out value="${loop.count}"/>. <c:out value="${item.name}"/></strong></div>
                                <div><strong>Dosing Info:</strong> <c:out value="${item.dosingInformation}"/></div>
                                <div><strong>Alternate Drug Available:</strong> <c:out value="${item.alternateDrugAvailable}"/></div>
                                <div class="mt-2"><strong>Summary</strong></div>
                                <div class="pdf-summary-block"><c:out value="${item.summaryMarkdown}"/></div>
                            </div>
                        </c:forEach>
                    </c:when>
                    <c:otherwise>
                        <div class="alert alert-warning mb-0">No drug labels matched for this sample's variants.</div>
                    </c:otherwise>
                </c:choose>
            </div>
        </main>
    </div>
</div>
<script>
    (function () {
        var exportButton = document.getElementById("exportClinicalReportBtn");
        var reportTemplate = document.getElementById("clinicalReportPdfTemplate");
        if (!exportButton || !reportTemplate) {
            return;
        }

        exportButton.addEventListener("click", function () {
            if (typeof html2pdf === "undefined") {
                alert("PDF export library failed to load. Please refresh and try again.");
                return;
            }

            var originalHtml = exportButton.innerHTML;
            var minExportDimension = 1;
            exportButton.disabled = true;
            exportButton.innerHTML = '<span class="spinner-border spinner-border-sm mr-1" role="status" aria-hidden="true"></span>Generating PDF...';

            var options = {
                margin: [10, 10, 10, 10],
                filename: "clinical_report_sample_" + (exportButton.getAttribute("data-sample-id") || "unknown") + ".pdf",
                image: {type: "jpeg", quality: 0.98},
                html2canvas: {scale: 1, useCORS: true, backgroundColor: "#ffffff", scrollX: 0, scrollY: 0},
                jsPDF: {unit: "mm", format: "a4", orientation: "portrait"},
                pagebreak: {mode: ["css", "legacy"]}
            };

            var exportRoot = document.createElement("div");
            exportRoot.id = "clinicalReportPdfExportRoot";
            exportRoot.style.position = "fixed";
            exportRoot.style.left = "0";
            exportRoot.style.top = "0";
            exportRoot.style.width = "auto";
            exportRoot.style.background = "#ffffff";
            exportRoot.style.pointerEvents = "none";
            exportRoot.style.opacity = "1";
            exportRoot.style.zIndex = "-1";
            exportRoot.style.overflow = "visible";
            document.body.appendChild(exportRoot);

            var reportClone = reportTemplate.cloneNode(true);
            reportClone.id = "clinicalReportPdfTemplateClone";
            reportClone.classList.remove("pdf-render-template");
            reportClone.style.position = "relative";
            reportClone.style.left = "0";
            reportClone.style.top = "0";
            reportClone.style.width = "190mm";
            reportClone.style.maxWidth = "none";
            reportClone.style.minWidth = "190mm";
            reportClone.style.background = "#ffffff";
            reportClone.style.padding = "10mm";
            reportClone.style.margin = "0";
            reportClone.style.pointerEvents = "none";
            reportClone.style.boxSizing = "border-box";
            reportClone.style.color = "#212529";
            reportClone.style.fontSize = ".85rem";
            reportClone.style.lineHeight = "1.45";
            reportClone.style.transform = "none";
            reportClone.style.overflow = "visible";
            exportRoot.appendChild(reportClone);

            var doExport = function () {
                var cloneRect = reportClone.getBoundingClientRect();
                var exportWidth = Math.max(cloneRect.width, minExportDimension);
                var exportHeight = Math.max(reportClone.scrollHeight, cloneRect.height, minExportDimension);
                options.html2canvas.width = exportWidth;
                options.html2canvas.height = exportHeight;
                options.html2canvas.x = 0;
                options.html2canvas.y = 0;

                html2pdf()
                    .set(options)
                    .from(reportClone)
                    .save()
                    .catch(function (error) {
                        console.error("Clinical report PDF export failed:", error);
                        alert("Unable to export clinical report right now. Please try again.");
                    })
                    .finally(function () {
                        if (exportRoot && exportRoot.parentNode) {
                            exportRoot.parentNode.removeChild(exportRoot);
                        }
                        exportButton.disabled = false;
                        exportButton.innerHTML = originalHtml;
                    });
            };

            window.requestAnimationFrame(function () {
                window.requestAnimationFrame(doExport);
            });
        });
    })();
</script>
</body>
</html>
