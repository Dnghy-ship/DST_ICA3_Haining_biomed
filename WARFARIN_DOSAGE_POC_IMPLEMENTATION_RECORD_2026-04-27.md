# Personalized Dosage Calculator (Warfarin) PoC - Implementation Record

## 1) Scope
- Implemented end-to-end PoC for collecting patient clinical profile with sample upload.
- Added IWPC-style Warfarin starting-dose calculation service.
- Added result-page conditional display for calculated weekly dose.

## 2) Step-by-step implementation plan used
1. Inspect current upload flow (`matching_index.jsp` -> `/upload` -> `MatchingController` -> `SampleDao` / `AnnovarDao`).
2. Add data-layer support for `patient_profile` table and Java model.
3. Persist profile data at upload time and link by `sample_id`.
4. Add dosage engine service with simplified IWPC formula and gene penalties.
5. Inject dose into matched Warfarin label in controller.
6. Render dose in accordion only when label is Warfarin and dose exists.
7. Validate with existing Maven test/build commands.

## 3) SQL changes

### New migration
- `src/main/sql/migration_v4_patient_profile.sql`
    - Creates `patient_profile` with:
        - `id` (PK)
        - `sample_id` (FK -> `sample.id`)
        - `age` (INT)
        - `height` (DECIMAL(10,2))
        - `weight` (DECIMAL(10,2))
        - `gender` (VARCHAR(50))
    - Adds unique constraint on `sample_id` (1:1 profile per sample).
    - Adds index on `sample_id`.

### Base schema update
- `src/main/sql/schema.sql`
    - Added `patient_profile` table definition for fresh DB initialization consistency.

## 4) Java backend changes

### New JavaBean
- `src/main/java/cn/edu/zju/bean/PatientProfile.java`
    - Fields: `id`, `sampleId`, `age`, `height`, `weight`, `gender`.

### Existing bean extension
- `src/main/java/cn/edu/zju/bean/MatchedDrugLabel.java`
    - Added `calculatedDose` (`Double`) + getter/setter.

### New DAO
- `src/main/java/cn/edu/zju/dao/PatientProfileDao.java`
    - `save(PatientProfile profile)`
    - `findBySampleId(int sampleId)`

### New service
- `src/main/java/cn/edu/zju/service/DosageCalculatorService.java`
    - `calculateWarfarinDose(PatientProfile profile, List<VariantCore> patientVariants)`
    - Formula implemented:
        - `sqrt(WeeklyDose) = 5.6044 - 0.2614*(Age/10) + 0.0087*Height + 0.0128*Weight + GenePenalties`
        - `WeeklyDose = (sqrt(WeeklyDose))^2`
    - Placeholder gene penalties:
        - Contains `VKORC1` -> `-0.8677`
        - Contains `CYP2C9` -> `-0.5211`

### Controller integration
- `src/main/java/cn/edu/zju/controller/MatchingController.java`
    - Upload flow now reads/validates clinical params (`age`, `height`, `weight`, `gender`).
    - Saves `PatientProfile` after `sample` creation, linked by `sample_id`.
    - During matching and saved-result viewing:
        - Detects Warfarin labels
        - Loads patient profile
        - Calculates dose
        - Attaches `calculatedDose` to that `MatchedDrugLabel`.

## 5) JSP/UI changes

### Upload page
- `src/main/webapp/views/matching_index.jsp`
    - Added Bootstrap fields before upload submit:
        - Age
        - Height (cm)
        - Weight (kg)
        - Gender
    - Fields are submitted to existing `/upload` controller endpoint.

### Result rendering
- `src/main/webapp/views/matching_result.jsp`
- `src/main/webapp/views/matching_index_search.jsp`
    - Added JSTL functions taglib.
    - Inside each accordion card body:
        - If drug name contains `warfarin` (case-insensitive) and `calculatedDose != null`,
        - Show prominent alert:
            - `Calculated Starting Dose: X mg/week`

## 6) Validation
- Baseline before changes: `mvn test`, `mvn package -DskipTests`.
- Post-change validation: same commands executed to ensure no regressions.

## 7) Notes
- Current PoC uses placeholder genotype detection from variant gene symbol presence.
- Future enhancement: true genotype-level parsing and ethnicity/medication covariates.
