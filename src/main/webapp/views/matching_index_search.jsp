<%--
  Created by IntelliJ IDEA.
  User: hello
  Date: 2019-12-3
  Time: 15:37
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
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
        .pdf-report-table tr {
            break-inside: avoid;
            page-break-inside: avoid;
        }
        .pdf-summary-block {
            white-space: pre-wrap;
            border: 1px solid #dee2e6;
            border-radius: .25rem;
            padding: .75rem;
            margin-bottom: .75rem;
            background: #f8f9fa;
            break-inside: avoid;
            page-break-inside: avoid;
        }
        .pdf-avoid-break {
            break-inside: avoid;
            page-break-inside: avoid;
        }
        .clinical-pdf-export-root {
            position: fixed;
            left: -10000px;
            top: 0;
            background: #ffffff;
            z-index: -1;
            pointer-events: none;
        }
        .clinical-pdf-preview-wrap {
            max-height: 70vh;
            overflow: auto;
            background: #f8f9fa;
            border: 1px solid #dee2e6;
            border-radius: .25rem;
            padding: .75rem;
        }
        .clinical-pdf-preview-wrap #clinicalReportPdfPreviewClone {
            background: #ffffff;
        }
        .pdf-export-overlay {
            position: fixed;
            inset: 0;
            background: rgba(33, 37, 41, 0.45);
            z-index: 2000;
            display: none;
            align-items: center;
            justify-content: center;
            color: #ffffff;
        }
        .pdf-export-overlay.is-visible {
            display: flex;
        }
        .pdf-export-overlay .spinner-border {
            width: 3rem;
            height: 3rem;
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

            <div class="card mb-3">
                <div class="card-header">Patient Clinical Profile</div>
                <div class="card-body">
                    <c:choose>
                        <c:when test="${not empty patientProfile}">
                            <dl class="row mb-0">
                                <dt class="col-sm-3">Age</dt>
                                <dd class="col-sm-9">${patientProfile.age}</dd>
                                <dt class="col-sm-3">Height</dt>
                                <dd class="col-sm-9">${patientProfile.height} cm</dd>
                                <dt class="col-sm-3">Weight</dt>
                                <dd class="col-sm-9">${patientProfile.weight} kg</dd>
                                <dt class="col-sm-3">Gender</dt>
                                <dd class="col-sm-9">${patientProfile.gender}</dd>
                            </dl>
                        </c:when>
                        <c:otherwise>
                            <div class="text-muted">No patient profile found for this sample.</div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>

            <div class="card mb-3">
                <div class="card-header">Personalized Warfarin Dose (PoC)</div>
                <div class="card-body">
                    <c:choose>
                        <c:when test="${warfarinDoseSummary != null && warfarinDoseSummary.weeklyDose != null}">
                            <div class="alert alert-primary mb-2" role="alert">
                                <strong>Calculated Starting Dose: ${warfarinDoseSummary.formattedDose} mg/week</strong>
                            </div>
                            <div class="text-muted small">${warfarinDoseSummary.statusMessage}</div>
                        </c:when>
                        <c:otherwise>
                            <div class="text-muted">${warfarinDoseSummary.statusMessage}</div>
                        </c:otherwise>
                    </c:choose>
                </div>
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
                                        <c:if test="${fn:contains(fn:toLowerCase(item.name), 'warfarin') and item.calculatedDose != null}">
                                            <div class="alert alert-primary" role="alert">
                                                <strong>Calculated Starting Dose: ${item.calculatedDose} mg/week</strong>
                                            </div>
                                        </c:if>
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

                <div class="mb-3">
                    <h5>Patient Clinical Profile</h5>
                    <c:choose>
                        <c:when test="${not empty patientProfile}">
                            <table class="table table-sm table-bordered mb-0">
                                <tbody>
                                <tr>
                                    <th style="width: 30%;">Age</th>
                                    <td>${patientProfile.age}</td>
                                </tr>
                                <tr>
                                    <th>Height</th>
                                    <td>${patientProfile.height} cm</td>
                                </tr>
                                <tr>
                                    <th>Weight</th>
                                    <td>${patientProfile.weight} kg</td>
                                </tr>
                                <tr>
                                    <th>Gender</th>
                                    <td>${patientProfile.gender}</td>
                                </tr>
                                </tbody>
                            </table>
                        </c:when>
                        <c:otherwise>
                            <div class="text-muted">No patient profile available for this sample.</div>
                        </c:otherwise>
                    </c:choose>
                </div>

                <div class="mb-3 pdf-avoid-break">
                    <h5>Personalized Warfarin Dose (PoC)</h5>
                    <div class="pdf-summary-block">
                        <c:choose>
                            <c:when test="${warfarinDoseSummary != null && warfarinDoseSummary.weeklyDose != null}">
                                <div><strong>Calculated Starting Dose:</strong> ${warfarinDoseSummary.formattedDose} mg/week</div>
                                <div class="text-muted">${warfarinDoseSummary.statusMessage}</div>
                            </c:when>
                            <c:otherwise>
                                <div class="text-muted">${warfarinDoseSummary.statusMessage}</div>
                            </c:otherwise>
                        </c:choose>
                    </div>
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
                            <div class="border rounded p-2 mb-2 pdf-avoid-break">
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

            <div class="modal fade" id="clinicalReportPdfPreviewModal" tabindex="-1" role="dialog" aria-labelledby="clinicalReportPdfPreviewModalLabel" aria-hidden="true">
                <div class="modal-dialog modal-xl modal-dialog-scrollable" role="document">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title" id="clinicalReportPdfPreviewModalLabel">Preview Clinical Report Before Export</h5>
                            <button type="button" class="close" data-dismiss="modal" aria-label="Close">
                                <span aria-hidden="true">&times;</span>
                            </button>
                        </div>
                        <div class="modal-body">
                            <div class="d-flex align-items-center justify-content-between mb-2">
                                <div class="small text-muted">Export quality</div>
                                <select id="clinicalReportExportQuality" class="custom-select custom-select-sm w-auto">
                                    <option value="balanced" selected>Balanced (recommended)</option>
                                    <option value="high">High quality</option>
                                    <option value="small">Smaller file</option>
                                </select>
                            </div>
                            <div class="clinical-pdf-preview-wrap" id="clinicalReportPdfPreviewBody"></div>
                        </div>
                        <div class="modal-footer">
                            <button type="button" id="cancelClinicalReportPreviewBtn" class="btn btn-secondary" data-dismiss="modal">Cancel</button>
                            <button type="button" id="confirmClinicalReportExportBtn" class="btn btn-primary">Confirm Export PDF</button>
                        </div>
                    </div>
                </div>
            </div>
            <div id="clinicalReportPdfExportOverlay" class="pdf-export-overlay" aria-hidden="true">
                <div class="text-center">
                    <div class="spinner-border text-light" role="status" aria-hidden="true"></div>
                    <div class="mt-2">Generating PDF...</div>
                </div>
            </div>
        </main>
    </div>
</div>
<script>
    (function () {
        var exportButton = document.getElementById("exportClinicalReportBtn");
        var reportTemplate = document.getElementById("clinicalReportPdfTemplate");
        var previewModal = document.getElementById("clinicalReportPdfPreviewModal");
        var previewBody = document.getElementById("clinicalReportPdfPreviewBody");
        var confirmExportBtn = document.getElementById("confirmClinicalReportExportBtn");
        var cancelPreviewBtn = document.getElementById("cancelClinicalReportPreviewBtn");
        var qualitySelect = document.getElementById("clinicalReportExportQuality");
        var exportOverlay = document.getElementById("clinicalReportPdfExportOverlay");
        if (!exportButton || !reportTemplate || !previewModal || !previewBody || !confirmExportBtn || !cancelPreviewBtn || !qualitySelect || !exportOverlay) {
            return;
        }

        var originalExportBtnHtml = exportButton.innerHTML;
        var originalConfirmBtnHtml = confirmExportBtn.innerHTML;
        // 718px ≈ 190mm at 96 DPI (A4 content width after 10mm left/right margins).
        var A4_CONTENT_WIDTH_PX = 718;
        // 38px ≈ 10mm at 96 DPI (matches configured PDF page margin).
        var TEMPLATE_PADDING_PX = 38;
        var pendingExportRoot = null;
        var pendingExportClone = null;
        var isExporting = false;
        var QUALITY_PRESETS = {
            balanced: {scale: 1.5, imageQuality: 0.9},
            high: {scale: 2, imageQuality: 0.98},
            small: {scale: 1.2, imageQuality: 0.85}
        };

        var clearPendingExport = function () {
            if (pendingExportRoot && pendingExportRoot.parentNode) {
                pendingExportRoot.parentNode.removeChild(pendingExportRoot);
            }
            pendingExportRoot = null;
            pendingExportClone = null;
        };

        var setOverlayVisible = function (visible) {
            if (visible) {
                exportOverlay.classList.add("is-visible");
            } else {
                exportOverlay.classList.remove("is-visible");
            }
        };

        var createReportClone = function (cloneId) {
            var reportClone = reportTemplate.cloneNode(true);
            reportClone.id = cloneId;
            reportClone.classList.remove("pdf-render-template");
            reportClone.style.position = "relative";
            reportClone.style.left = "0";
            reportClone.style.top = "0";
            reportClone.style.transform = "none";
            reportClone.style.width = A4_CONTENT_WIDTH_PX + "px";
            reportClone.style.minWidth = A4_CONTENT_WIDTH_PX + "px";
            reportClone.style.maxWidth = "none";
            reportClone.style.padding = TEMPLATE_PADDING_PX + "px";
            reportClone.style.margin = "0";
            reportClone.style.boxSizing = "border-box";
            reportClone.style.background = "#ffffff";
            reportClone.style.color = "#212529";
            reportClone.style.fontSize = ".85rem";
            reportClone.style.lineHeight = "1.45";
            reportClone.style.display = "block";
            reportClone.style.visibility = "visible";
            return reportClone;
        };

        var getQualityPreset = function () {
            var selected = qualitySelect.value || "balanced";
            return QUALITY_PRESETS[selected] || QUALITY_PRESETS.balanced;
        };

        var buildFileName = function () {
            var sampleId = exportButton.getAttribute("data-sample-id") || "unknown";
            var dateStamp = new Date().toISOString().slice(0, 10).replace(/-/g, "");
            return "clinical_report_sample_" + sampleId + "_" + dateStamp + ".pdf";
        };

        var buildPdfMetadata = function () {
            var sampleId = exportButton.getAttribute("data-sample-id") || "unknown";
            return {
                title: "Clinical Report Sample " + sampleId,
                subject: "Clinical Pharmacogenomic Matching Report",
                author: "Precision Medicine Matching System"
            };
        };

        var waitForFontsReady = function () {
            if (document.fonts && document.fonts.ready) {
                return document.fonts.ready;
            }
            return Promise.resolve();
        };

        var waitForImagesReady = function (root, timeoutMs) {
            var images = Array.prototype.slice.call(root.querySelectorAll("img"));
            if (!images.length) {
                return Promise.resolve();
            }
            return new Promise(function (resolve, reject) {
                var remaining = images.length;
                var finished = false;
                var timeout = setTimeout(function () {
                    if (finished) {
                        return;
                    }
                    finished = true;
                    reject(new Error("EXPORT_TIMEOUT"));
                }, timeoutMs);
                var markDone = function () {
                    if (finished) {
                        return;
                    }
                    remaining -= 1;
                    if (remaining <= 0) {
                        finished = true;
                        clearTimeout(timeout);
                        resolve();
                    }
                };
                images.forEach(function (image) {
                    if (image.complete && image.naturalWidth > 0) {
                        markDone();
                    } else {
                        image.addEventListener("load", markDone, {once: true});
                        image.addEventListener("error", markDone, {once: true});
                    }
                });
            });
        };

        var waitForExportReady = function (root) {
            return Promise.all([waitForFontsReady(), waitForImagesReady(root, 8000)]);
        };

        var getExportErrorMessage = function (error) {
            if (error && /EXPORT_TIMEOUT/.test(error.message)) {
                return "Export timed out. Try a lower quality setting and retry.";
            }
            if (error && /(tainted|cross-origin|security|cors)/i.test(error.message)) {
                return "Export failed due to cross-origin images. Please ensure images are accessible.";
            }
            return "Unable to export clinical report right now. Please try again.";
        };

        $("#clinicalReportPdfPreviewModal").on("hidden.bs.modal", function () {
            if (isExporting) {
                return;
            }
            previewBody.innerHTML = "";
            clearPendingExport();
            exportButton.disabled = false;
            exportButton.innerHTML = originalExportBtnHtml;
            confirmExportBtn.disabled = false;
            confirmExportBtn.innerHTML = originalConfirmBtnHtml;
            cancelPreviewBtn.disabled = false;
            setOverlayVisible(false);
            document.body.style.overflow = "";
        });

        exportButton.addEventListener("click", function () {
            if (typeof html2pdf === "undefined") {
                alert("PDF export library failed to load. Please refresh and try again.");
                return;
            }
            clearPendingExport();
            previewBody.innerHTML = "";

            var previewClone = createReportClone("clinicalReportPdfPreviewClone");
            previewClone.style.margin = "0 auto";
            previewBody.appendChild(previewClone);
            pendingExportClone = previewClone;

            $("#clinicalReportPdfPreviewModal").modal("show");
        });

        confirmExportBtn.addEventListener("click", function () {
            if (!pendingExportClone || isExporting) {
                return;
            }
            if (typeof html2pdf === "undefined") {
                alert("PDF export library failed to load. Please refresh and try again.");
                return;
            }

            var exportRoot = document.createElement("div");
            exportRoot.id = "clinicalReportPdfExportRoot";
            exportRoot.className = "clinical-pdf-export-root";
            exportRoot.style.width = A4_CONTENT_WIDTH_PX + "px";
            pendingExportRoot = exportRoot;
            pendingExportClone.style.margin = "0";
            exportRoot.appendChild(pendingExportClone);
            previewBody.innerHTML = "";
            document.body.appendChild(exportRoot);

            isExporting = true;
            exportButton.disabled = true;
            confirmExportBtn.disabled = true;
            cancelPreviewBtn.disabled = true;
            exportButton.innerHTML = '<span class="spinner-border spinner-border-sm mr-1" role="status" aria-hidden="true"></span>Generating PDF...';
            confirmExportBtn.innerHTML = '<span class="spinner-border spinner-border-sm mr-1" role="status" aria-hidden="true"></span>Exporting...';
            setOverlayVisible(true);
            document.body.style.overflow = "hidden";

            waitForExportReady(pendingExportClone)
                .then(function () {
                    var qualityPreset = getQualityPreset();
                    var options = {
                        margin: [10, 10, 10, 10],
                        filename: buildFileName(),
                        image: {type: "jpeg", quality: qualityPreset.imageQuality},
                        html2canvas: {
                            scale: qualityPreset.scale,
                            useCORS: true,
                            backgroundColor: "#ffffff",
                            scrollX: 0,
                            scrollY: 0,
                            windowWidth: A4_CONTENT_WIDTH_PX
                        },
                        jsPDF: {unit: "mm", format: "a4", orientation: "portrait", compress: true},
                        pagebreak: {mode: ["css", "legacy"], avoid: ".pdf-avoid-break"}
                    };
                    var metadata = buildPdfMetadata();
                    return html2pdf()
                        .set(options)
                        .from(pendingExportRoot)
                        .toPdf()
                        .get("pdf")
                        .then(function (pdf) {
                            pdf.setProperties(metadata);
                        })
                        .save();
                })
                .catch(function (error) {
                    console.error("Clinical report PDF export failed:", error);
                    alert(getExportErrorMessage(error));
                })
                .finally(function () {
                    isExporting = false;
                    clearPendingExport();
                    previewBody.innerHTML = "";
                    exportButton.disabled = false;
                    exportButton.innerHTML = originalExportBtnHtml;
                    confirmExportBtn.disabled = false;
                    confirmExportBtn.innerHTML = originalConfirmBtnHtml;
                    cancelPreviewBtn.disabled = false;
                    setOverlayVisible(false);
                    document.body.style.overflow = "";
                    $("#clinicalReportPdfPreviewModal").modal("hide");
                });
        });
    })();
</script>
</body>
</html>
