package cn.edu.zju.bean;

public class VariantAnnotation {

    private int variantId;
    private String geneSymbol;
    private String acmgClassification;

    public VariantAnnotation() {
    }

    public VariantAnnotation(int variantId, String geneSymbol, String acmgClassification) {
        this.variantId = variantId;
        this.geneSymbol = geneSymbol;
        this.acmgClassification = acmgClassification;
    }

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    public String getGeneSymbol() {
        return geneSymbol;
    }

    public void setGeneSymbol(String geneSymbol) {
        this.geneSymbol = geneSymbol;
    }

    public String getAcmgClassification() {
        return acmgClassification;
    }

    public void setAcmgClassification(String acmgClassification) {
        this.acmgClassification = acmgClassification;
    }
}
