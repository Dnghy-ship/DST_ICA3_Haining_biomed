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
            <jsp:param name="active" value="matching_index" />
        </jsp:include>

        <main role="main" class="col-md-9 ml-sm-auto col-lg-10 px-4">
            <div class="d-flex justify-content-between flex-wrap flex-md-nowrap align-items-center pt-3 pb-2 mb-3 border-bottom">
                <h2>Matching</h2>
            </div>
            <div class="alert alert-info" role="alert">
                <h5 class="alert-heading mb-2">How Matching Works</h5>
                <ol class="mb-0 pl-3">
                    <li>Upload ANNOVAR output (.tsv).</li>
                    <li>System filters variants via ACMG criteria.</li>
                    <li>Results are matched against PharmGKB and scored by gene match, variant evidence, label evidence, and guidelines.</li>
                </ol>
            </div>
            <div class="table-responsive">
                <form method="post" action="upload" enctype="multipart/form-data">
                    <div class="form-row">
                        <div class="form-group col-md-3">
                            <label for="age">Age</label>
                            <input type="number" min="1" class="form-control" id="age" name="age" required>
                        </div>
                        <div class="form-group col-md-3">
                            <label for="height">Height (cm)</label>
                            <input type="number" min="1" step="0.1" class="form-control" id="height" name="height" required>
                        </div>
                        <div class="form-group col-md-3">
                            <label for="weight">Weight (kg)</label>
                            <input type="number" min="1" step="0.1" class="form-control" id="weight" name="weight" required>
                        </div>
                        <div class="form-group col-md-3">
                            <label for="gender">Gender</label>
                            <select id="gender" name="gender" class="form-control" required>
                                <option value="">Select gender</option>
                                <option value="Male">Male</option>
                                <option value="Female">Female</option>
                            </select>
                        </div>
                    </div>
                    <div class="form-group">
                        <label for="exampleFormControlFile1">Annovar Output</label>
                        <input type="file" class="form-control-file" id="exampleFormControlFile1" name="annovar">
                    </div>
                    <div class="form-group">
                        <label for="uploaded_by">Uploaded By</label>
                        <input type="input" class="form-control" id="uploaded_by" name="uploaded_by">
                    </div>
                    <button type="submit" class="btn btn-primary">Upload</button>
                </form>
            </div>
        </main>
    </div>
</div>
</body>
</html>
