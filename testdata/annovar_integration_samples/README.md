# ANN OVAR Integration Test Samples (Precision Matching)

Location: `testdata/annovar_integration_samples/`

These files are designed for the upload endpoint and follow the parser contract in `AnnovarDao`:
- Tab-separated rows
- Required core columns at index 0..4 (chr/start/end/ref/alt)
- Gene symbol at column index 6 (`Gene.refGene`)
- Exonic function at index 8 (stored as JSON key `annovar_col_9`)
- ACMG classification at index 125

## Files

1. `01_blank_upload.tsv`
- Purpose: Validate blank-file upload handling and that no corrupted match results are produced.

2. `02_invalid_format.tsv`
- Purpose: Validate graceful error handling for malformed rows (< 6 columns).

3. `03_synonymous_only_benign.tsv`
- Purpose: All variants are `synonymous SNV`; used to verify Java benign filter prevents false-positive matching.

4. `04_multi_evidence_priority_genes.tsv`
- Purpose: Contains clinically important pharmacogenes (CYP2C19, DPYD, TPMT, HLA-B, SLCO1B1, UGT1A1) to increase chance of matching labels across evidence tiers and confirm descending score/recommendation rendering.

5. `05_persistence_and_cascade_validation.tsv`
- Purpose: Valid non-synonymous PGx variants for persistence checks (`matching_result` write/read), plus one synonymous SLCO1B1 control row to confirm exclusion behavior before cascade-flow validation.
