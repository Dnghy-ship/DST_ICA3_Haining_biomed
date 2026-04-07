package cn.edu.zju.bean;

public class VariantCore {

    private int id;
    private int sampleId;
    private String chr;
    private String startPos;
    private String endPos;
    private String refAllele;
    private String altAllele;
    private VariantAnnotation annotation;

    public VariantCore() {
    }

    public VariantCore(int id, int sampleId, String chr, String startPos, String endPos, String refAllele, String altAllele) {
        this.id = id;
        this.sampleId = sampleId;
        this.chr = chr;
        this.startPos = startPos;
        this.endPos = endPos;
        this.refAllele = refAllele;
        this.altAllele = altAllele;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSampleId() {
        return sampleId;
    }

    public void setSampleId(int sampleId) {
        this.sampleId = sampleId;
    }

    public String getChr() {
        return chr;
    }

    public void setChr(String chr) {
        this.chr = chr;
    }

    public String getStartPos() {
        return startPos;
    }

    public void setStartPos(String startPos) {
        this.startPos = startPos;
    }

    public String getEndPos() {
        return endPos;
    }

    public void setEndPos(String endPos) {
        this.endPos = endPos;
    }

    public String getRefAllele() {
        return refAllele;
    }

    public void setRefAllele(String refAllele) {
        this.refAllele = refAllele;
    }

    public String getAltAllele() {
        return altAllele;
    }

    public void setAltAllele(String altAllele) {
        this.altAllele = altAllele;
    }

    public VariantAnnotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(VariantAnnotation annotation) {
        this.annotation = annotation;
    }
}
