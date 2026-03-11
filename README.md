# Precision Medicine Matching System

A Java Servlet + JSP web application for pharmacogenomic (drug–gene) matching.  
The system parses patient ANNOVAR variant data and matches variants against a PharmGKB-sourced knowledge base to identify relevant drug labels and dosing guidelines.

---

## Quick Start

### Prerequisites
- Java 8+
- Apache Tomcat 8+
- MySQL 5.7+ / 8.0+

### Database Setup
```sql
-- Run src/main/sql/schema.sql to create the schema
-- Run src/main/sql/dbconfig.sql to create the database user
mysql -u root -p < src/main/sql/schema.sql
mysql -u root -p < src/main/sql/dbconfig.sql
```

### Configuration
Edit `src/main/resources/app.properties`:
```properties
jdbc.url=jdbc:mysql://127.0.0.1:3306/biomed?serverTimezone=GMT%2B8&useSSL=false&allowPublicKeyRetrieval=true
jdbc.username=biomed
jdbc.password=biomed
```

### Build & Deploy
```bash
mvn package
# Deploy target/haining_biomed.war to Tomcat webapps/
```

---

## How to Upload ANNOVAR Output

### What is ANNOVAR?

[ANNOVAR](https://annovar.openbioinformatics.org/) is a widely-used command-line tool that annotates genetic variants with functional information from multiple databases (population frequencies, pathogenicity scores, ACMG criteria, etc.).

### Step 1 – Run ANNOVAR on your VCF

```bash
perl table_annovar.pl patient.vcf humandb/ \
  -buildver hg19 \
  -out patient_anno \
  -remove \
  -protocol refGene,cytoBand,1000g2015aug_all,gnomAD_genome_ALL,\
            SIFT,Polyphen2_HDIV,intervar_20180118 \
  -operation g,r,f,f,f,f,f \
  -nastring . \
  -vcfinput
```

This produces a file such as `patient_anno.hg19_multianno.txt`.

### Step 2 – Prepare the File

| Requirement | Detail |
|-------------|--------|
| Format | Tab-separated (TSV) |
| Header | **No header line** – each row is one variant |
| Encoding | UTF-8 |
| Columns | First 153 columns mapped to known ANNOVAR fields; remaining columns stored as `Otherinfo` |

### Expected Column Order (first 11)

| # | Field |
|---|-------|
| 1 | Chr |
| 2 | Start |
| 3 | End |
| 4 | Ref |
| 5 | Alt |
| 6 | Func.refGene |
| 7 | Gene.refGene |
| 8 | GeneDetail.refGene |
| 9 | ExonicFunc.refGene |
| 10 | AAChange.refGene |
| 11 | cytoBand |
| … | (population frequencies, pathogenicity scores, ACMG evidence) |
| 153 | InterVar_automated |
| 154+ | Otherinfo (all remaining columns concatenated with tab) |

**Example row:**
```
chr1	925952	925952	G	A	exonic	SAMD11	.	nonsynonymous SNV	SAMD11:NM_015658:exon14:c.G1979A:p.R660H	p36.33	.	.	.	.	.	.	...
```

### Step 3 – Upload via the Web UI

1. Open the application in your browser and navigate to **Upload & Match** in the sidebar.
2. Click **Choose File** and select your ANNOVAR TSV file (max 50 MB).
3. Click **Run Matching**.
4. The system will:
   - Parse every row and store variants in the `annovar` database table.
   - Extract the list of distinct non-synonymous genes (`ExonicFunc.refGene != 'synonymous SNV'`).
   - Search the PharmGKB drug label summaries for those gene names.
   - Display matched drug labels with source and summary information.

### Step 4 – Review Results

After matching, you can:
- View matched drug labels directly on the upload results page.
- Browse all uploaded samples at `/samples`.
- Browse the full knowledge base at `/drugs`, `/drugLabels`, `/dosingGuideline`.

---

## Web Pages

| URL | Description |
|-----|-------------|
| `/` | Dashboard |
| `/matchingIndex` | Upload ANNOVAR file & run drug matching |
| `/samples` | List all uploaded samples with variant counts; search by uploader |
| `/drugs` | Browse drugs with search, sort (name/biomarker), pagination |
| `/drugLabels` | Browse drug labels with search and pagination |
| `/dosingGuideline` | Browse dosing guidelines with search and pagination |
| `/counter` | Visitor counter demo (Week 3 practical) |
| `/signin` | Sign in (required for dosing guideline access) |

### Query Parameters (all list pages)

| Parameter | Default | Description |
|-----------|---------|-------------|
| `q` | *(empty)* | Search/filter term (case-insensitive, partial match) |
| `page` | `1` | 1-based page number |
| `pageSize` | `20` | Items per page (max 100) |
| `sort` | `name` | Sort column — `/drugs` supports `name` or `biomarker` |
| `dir` | `asc` | Sort direction: `asc` or `desc` |

---

## Running Tests

```bash
mvn test
```

Tests cover:
- `Page<T>` pagination metadata calculations
- `DrugDao` parameter validation helpers (`buildLikePattern`, `validateSortColumn`, `validateSortDir`)
- `Sample` bean including the `variantCount` field
- Pagination parameter parsing and offset arithmetic
