<%--
  matching_index.jsp – ANNOVAR file upload and drug matching results page.
  How to use:
    1. Prepare your ANNOVAR output file (tab-separated, no header).
    2. Upload the file using the form below.
    3. The system will parse the variants, run pharmacogenomic matching against PharmGKB drug labels,
       and display matched drug labels.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %>

<!doctype html>
<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no">
    <title>Upload &amp; Match · Precision Medicine</title>

    <link href="<%=request.getContextPath()%>/static/bootstrap/css/bootstrap.css" rel="stylesheet">
    <script src="<%=request.getContextPath()%>/static/jquery/jquery-3.4.1.js"></script>
    <script src="<%=request.getContextPath()%>/static/bootstrap/js/bootstrap.bundle.min.js"></script>
    <link href="<%=request.getContextPath()%>/static/css/app.css" rel="stylesheet">
    <style>
        .bd-placeholder-img { font-size: 1.125rem; text-anchor: middle; -webkit-user-select: none; -moz-user-select: none; -ms-user-select: none; user-select: none; }
        @media (min-width: 768px) { .bd-placeholder-img-lg { font-size: 3.5rem; } }
        pre.example-block { background: #f4f4f4; border: 1px solid #ddd; border-radius: 4px; padding: 12px; font-size: 0.82em; overflow-x: auto; }
    </style>
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
                <h2>&#x1F9EC; Upload ANNOVAR Output &amp; Run Drug Matching</h2>
            </div>

            <%-- Error message --%>
            <c:if test="${not empty error}">
                <div class="alert alert-danger">${error}</div>
            </c:if>

            <%-- ========= How-to guidance ========= --%>
            <div class="card mb-4">
                <div class="card-header"><strong>&#8505; How to Use This Page</strong></div>
                <div class="card-body">
                    <h5 class="card-title">Step 1 &ndash; Generate ANNOVAR Output</h5>
                    <p>Run <a href="https://annovar.openbioinformatics.org/" target="_blank" rel="noopener noreferrer">ANNOVAR</a>
                       on your patient&rsquo;s variant call file (VCF) to produce a tab-separated annotation file.
                       A typical ANNOVAR command looks like:</p>
                    <pre class="example-block">perl table_annovar.pl input.vcf humandb/ \
  -buildver hg19 \
  -out patient_anno \
  -remove \
  -protocol refGene,cytoBand,1000g2015aug_all,...,intervar_20180118 \
  -operation g,r,f,...,f \
  -nastring . \
  -vcfinput</pre>
                    <p>This produces a file such as <code>patient_anno.hg19_multianno.txt</code>.</p>

                    <h5 class="card-title mt-3">Step 2 &ndash; Prepare the File</h5>
                    <ul>
                        <li>The file must be <strong>tab-separated (TSV)</strong>.</li>
                        <li>The file must <strong>have no header line</strong> &ndash; each row is a variant.</li>
                        <li>The system reads the <strong>first 153 columns</strong> (mapped to known ANNOVAR fields).</li>
                        <li>All remaining columns are concatenated as <code>Otherinfo</code>.</li>
                    </ul>

                    <h5 class="card-title mt-3">Expected Column Order (first 5 of 153)</h5>
                    <div class="table-responsive">
                        <table class="table table-bordered table-sm" style="font-size:0.85em;">
                            <thead class="thead-light">
                            <tr>
                                <th>Col</th><th>1</th><th>2</th><th>3</th><th>4</th><th>5</th>
                                <th>6</th><th>7</th><th>8</th><th>9&ndash;10</th><th>11</th><th>&hellip;</th><th>153+</th>
                            </tr>
                            </thead>
                            <tbody>
                            <tr>
                                <td><strong>Field</strong></td>
                                <td>Chr</td><td>Start</td><td>End</td><td>Ref</td><td>Alt</td>
                                <td>Func.refGene</td><td>Gene.refGene</td><td>GeneDetail.refGene</td>
                                <td>ExonicFunc.refGene / AAChange.refGene</td>
                                <td>cytoBand</td>
                                <td>&hellip;(population frequencies, pathogenicity scores, ACMG criteria)</td>
                                <td>Otherinfo (remainder)</td>
                            </tr>
                            </tbody>
                        </table>
                    </div>

                    <h5 class="card-title mt-3">Example row</h5>
                    <pre class="example-block">chr1&#9;925952&#9;925952&#9;G&#9;A&#9;exonic&#9;SAMD11&#9;.&#9;nonsynonymous SNV&#9;SAMD11:NM_015658:exon14:c.G1979A:p.R660H&#9;p36.33&#9;.&#9;.&#9;.&#9;.&#9;.&#9;.&#9;.&#9;...&#9;Otherinfo_value</pre>

                    <div class="alert alert-info mt-2" style="font-size:0.9em;">
                        <strong>Note:</strong> Only non-synonymous variants (<code>ExonicFunc.refGene != 'synonymous SNV'</code>)
                        are used for drug matching. Synonymous variants are stored but excluded from gene-drug lookup.
                    </div>
                </div>
            </div>

            <%-- ========= Upload form ========= --%>
            <c:if test="${empty matchedLabels}">
            <div class="card mb-4">
                <div class="card-header"><strong>&#x1F4C2; Upload ANNOVAR File</strong></div>
                <div class="card-body">
                    <form method="post" action="<%=request.getContextPath()%>/matchingIndex" enctype="multipart/form-data">
                        <div class="form-group">
                            <label for="annovarFile">Select ANNOVAR TSV file (no header, tab-separated):</label>
                            <input type="file" class="form-control-file" id="annovarFile" name="annovarFile" accept=".txt,.tsv,.csv" required>
                            <small class="form-text text-muted">Max file size: 50 MB. Accepted formats: .txt, .tsv</small>
                        </div>
                        <button type="submit" class="btn btn-success">&#x25B6; Run Matching</button>
                    </form>
                </div>
            </div>
            </c:if>

            <%-- ========= Results section ========= --%>
            <c:if test="${not empty matchedLabels}">
            <div class="alert alert-success">
                <strong>&#x2714; Matching complete!</strong>
                Sample ID <strong>${sampleId}</strong> &mdash;
                Found <strong>${refGenes.size()}</strong> non-synonymous gene(s),
                matched <strong>${matchedLabels.size()}</strong> drug label(s).
                <a href="<%=request.getContextPath()%>/samples" class="ml-2">View all samples &rarr;</a>
            </div>

            <h5>Identified Genes</h5>
            <div class="mb-3">
                <c:forEach items="${refGenes}" var="gene">
                    <span class="badge badge-primary mr-1">${gene}</span>
                </c:forEach>
            </div>

            <h5>Matched Drug Labels (${matchedLabels.size()})</h5>
            <div class="table-responsive">
                <table class="table table-striped table-sm">
                    <thead class="thead-dark">
                    <tr>
                        <th>#</th>
                        <th>Name</th>
                        <th>Source</th>
                        <th>Summary</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach items="${matchedLabels}" var="label">
                        <tr>
                            <td>${label.id}</td>
                            <td>${label.name}</td>
                            <td>${label.source}</td>
                            <td>${label.summaryMarkdown}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>

            <a href="<%=request.getContextPath()%>/matchingIndex" class="btn btn-outline-secondary mt-2">&#x2B06; Upload another file</a>
            </c:if>

        </main>
    </div>
</div>
</body>
</html>
