# Test Data for ANNOVAR Upload

## File: `annovar_test.tsv`

A minimal ANNOVAR output file compatible with the **master-branch** `AnnovarDao.save()` parser.

### Format
| Property | Value |
|----------|-------|
| Separator | Tab (`\t`) |
| Header row | **None** |
| Columns per row | 154 (153 required + 1 Otherinfo column) |
| Encoding | UTF-8 |

### Rows included

| Row | Chr | Position | Gene.refGene | ExonicFunc.refGene | dbSNP |
|-----|-----|----------|-------------|-------------------|-------|
| 1 | chr10 | 96702047 | **CYP2C9** | nonsynonymous SNV | rs1799853 (*2 allele) |
| 2 | chr22 | 42523528 | **CYP2D6** | nonsynonymous SNV | rs3892097 (*4 allele) |
| 3 | chr16 | 31096368 | **VKORC1** | synonymous SNV    | rs9923231 (-1639G>A) |
| 4 | chr16 | 31096370 | **VKORC1** | nonsynonymous SNV | rs61742245 |

* Rows 1, 2 and 4 have `ExonicFunc.refGene != 'synonymous SNV'`, so they are returned by `AnnovarDao.getRefGenes()` and will participate in drug-label matching.
* Row 3 is **synonymous SNV** – it is intentionally excluded by the filter to verify the filter works.
* CYP2C9, CYP2D6 and VKORC1 are well-known PGx genes present in PharmGKB drug-label `summaryMarkdown` fields, so you should get matching drug labels after upload.

---

## How to upload and test matching via the web UI

### Prerequisites
1. The web application is running (Tomcat, default `http://localhost:8080`).
2. The database has been populated by the PharmGKB importer (drug labels exist).

### Steps

1. **Open the upload page**

   Navigate to:
   ```
   http://localhost:8080/matchingIndex
   ```

2. **Fill in "Uploaded By"**

   Enter any identifier, e.g. `test-user`.

3. **Choose the test file**

   Click **Browse / Choose File** and select `test-data/annovar_test.tsv` from this repository.

4. **Upload**

   Click **Upload**. The server will:
   - Insert a new row in the `sample` table.
   - Parse all 4 lines and batch-insert them into the `annovar` table.
   - Redirect you to the matching results page.

5. **View matching results**

   The results page (`/matching?sampleId=<N>`) shows all drug labels whose `summaryMarkdown` contains at least one of the non-synonymous gene names (`CYP2C9`, `CYP2D6`, `VKORC1`).  
   You should see drug labels related to warfarin, clopidogrel, and other PGx-relevant medications.

6. **Check all samples**

   Navigate to `http://localhost:8080/samples` to see the uploaded sample in the list and click **Matching** to re-open the results.

### Troubleshooting

| Symptom | Likely cause | Fix |
|---------|-------------|-----|
| "annovar output file is invalid" | File has fewer than 153 tab-separated columns on some row | Use the provided `annovar_test.tsv` without modification |
| Empty matching results | Drug-label table is empty | Run the PharmGKB crawler/importer first |
| No non-synonymous genes returned | All rows have `ExonicFunc.refGene = 'synonymous SNV'` | Rows 1, 2, 4 are non-synonymous — check the file was not edited |
