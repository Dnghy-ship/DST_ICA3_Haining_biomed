package cn.edu.zju.bean;

public class VariantBioDetails {

    private int variantId;
    private String rawDetails;

    public VariantBioDetails() {
    }

    public VariantBioDetails(int variantId, String rawDetails) {
        this.variantId = variantId;
        this.rawDetails = rawDetails;
    }

    public int getVariantId() {
        return variantId;
    }

    public void setVariantId(int variantId) {
        this.variantId = variantId;
    }

    public String getRawDetails() {
        return rawDetails;
    }

    public void setRawDetails(String rawDetails) {
        this.rawDetails = rawDetails;
    }
}
