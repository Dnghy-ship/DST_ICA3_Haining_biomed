# Clinical UX Upgrade Fixes Record

## Scope
Branch target: `feature/clinical-ux-upgrade`  
Date: 2026-04-12

This document records the implementation for:
1. Database integration gap for normalized variant tables.
2. Dashboard knowledge-base stats enhancement.
3. Matching interface onboarding guidance.
4. Samples history sorting/action fixes.
5. Data library summary text wrapping fixes.

---

## Task 1: Database Integration Gap (normalized tables)

### Problem
`variant_core`, `variant_annotation`, and `variant_bio_details` were not fully leveraged in one integrated retrieval path, making their value less visible.

### Changes
- **File:** `src/main/java/cn/edu/zju/dao/AnnovarDao.java`
  - Updated `findAnnotationsBySampleId(int sampleId)` to:
    - Join all three normalized tables in one query:
      - `variant_core` (`vc`)
      - `variant_annotation` (`va`)
      - `variant_bio_details` (`vbd`)
    - Select `vbd.raw_details` together with core and annotation fields.
    - Populate `VariantCore.bioDetails` directly when `raw_details` exists.

### Outcome
- Matching flow now loads core + annotation + bio details in a single integrated path.
- Reduces N+1 lookups for bio details in common processing.
- Makes normalized-table design actively used and operationally meaningful.

---

## Task 2: Dashboard Enhancements (Knowledge Base Stats)

### Problem
Dashboard did not show knowledge-base scale metrics (Drugs and Guidelines).

### Changes
- **File:** `src/main/java/cn/edu/zju/dao/DrugDao.java`
  - Added `count()` using `select count(*) from drug`.

- **File:** `src/main/java/cn/edu/zju/dao/DosingGuidelineDao.java`
  - Added `count()` using `select count(*) from dosing_guideline`.

- **File:** `src/main/java/cn/edu/zju/bean/DashboardStats.java`
  - Added fields:
    - `totalDrugs`
    - `totalGuidelines`
  - Updated constructor and added getters/setters.

- **File:** `src/main/java/cn/edu/zju/service/DashboardService.java`
  - Injected `DrugDao` and `DosingGuidelineDao`.
  - Added totals retrieval and returned extended `DashboardStats`.

- **File:** `src/main/java/cn/edu/zju/controller/IndexController.java`
  - On stats loading exception, now sets `request.setAttribute("stats", new DashboardStats())` to keep JSP rendering stable.

- **File:** `src/main/webapp/views/index.jsp`
  - Added stat cards for:
    - `Total Drugs`
    - `Total Guidelines`
  - Preserved sample/variant stats.
  - Added quick-action buttons row for matching, samples, drugs, and guidelines pages.

### Outcome
- Dashboard now visibly reports key Knowledge Base stats from DB in real time.

---

## Task 3: Matching Interface Onboarding

### Problem
Upload page lacked workflow guidance for users.

### Changes
- **File:** `src/main/webapp/views/matching_index.jsp`
  - Added Bootstrap info alert with workflow:
    1. Upload ANNOVAR output (.tsv).
    2. System filters variants via ACMG criteria.
    3. Results are matched against PharmGKB and scored by evidence level.

### Outcome
- Improved first-time user onboarding and process clarity before upload.

---

## Task 4: Samples History Page Fixes

### Problem A: Date sorting not strictly chronological in DataTables.

### Changes
- **File:** `src/main/java/cn/edu/zju/bean/Sample.java`
  - Added `getCreatedAtFormatted()` returning `yyyy-MM-dd HH:mm:ss`.

- **File:** `src/main/webapp/views/samples.jsp`
  - `Uploaded At` now renders:
    - Display text: `${item.createdAtFormatted}`
    - Sort key: `data-order="${item.createdAt.time}"` (epoch milliseconds)
  - DataTables default order changed to `Uploaded At` descending:
    - `order: [[2, 'desc']]`

### Outcome
- Chronological sorting is now correct and stable.

### Problem B: Redundant "Run Matching" action.

### Changes
- **File:** `src/main/webapp/views/samples.jsp`
  - Replaced action buttons with one unified button:
    - `View Report` -> `/matchingResult?sampleId=...`

### Outcome
- Action column now follows report-first workflow.
- If no saved report exists, backend route fallback continues to generate via matching flow.

---

## Task 5: Data Library Summary Truncation

### Problem
Long `Summary` text was truncated in table cells.

### Changes
- **File:** `src/main/webapp/views/drug_labels.jsp`
  - Updated `.summary-cell` CSS:
    - `white-space: normal;`
    - `word-wrap: break-word;`
    - `overflow-wrap: break-word;`

- **File:** `src/main/webapp/views/dosing_guideline.jsp`
  - Applied same `.summary-cell` wrapping CSS updates.

### Outcome
- Long summary content now wraps within table cells instead of being cut off.

---

## Validation

Executed before and after implementation:
- `mvn test`
- `mvn package -DskipTests`

Both commands completed successfully.

