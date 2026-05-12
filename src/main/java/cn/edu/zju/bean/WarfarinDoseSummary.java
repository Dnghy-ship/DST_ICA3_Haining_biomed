package cn.edu.zju.bean;

public class WarfarinDoseSummary {

    private Double weeklyDose;
    private String formattedDose;
    private boolean profileValid;
    private boolean variantsPresent;
    private boolean hasVkorc1;
    private boolean hasCyp2c9;
    private double genePenalty;
    private String formattedGenePenalty;
    private boolean warfarinMatched;
    private String statusMessage;

    public Double getWeeklyDose() {
        return weeklyDose;
    }

    public void setWeeklyDose(Double weeklyDose) {
        this.weeklyDose = weeklyDose;
    }

    public String getFormattedDose() {
        return formattedDose;
    }

    public void setFormattedDose(String formattedDose) {
        this.formattedDose = formattedDose;
    }

    public boolean isProfileValid() {
        return profileValid;
    }

    public void setProfileValid(boolean profileValid) {
        this.profileValid = profileValid;
    }

    public boolean isVariantsPresent() {
        return variantsPresent;
    }

    public void setVariantsPresent(boolean variantsPresent) {
        this.variantsPresent = variantsPresent;
    }

    public boolean isHasVkorc1() {
        return hasVkorc1;
    }

    public void setHasVkorc1(boolean hasVkorc1) {
        this.hasVkorc1 = hasVkorc1;
    }

    public boolean isHasCyp2c9() {
        return hasCyp2c9;
    }

    public void setHasCyp2c9(boolean hasCyp2c9) {
        this.hasCyp2c9 = hasCyp2c9;
    }

    public double getGenePenalty() {
        return genePenalty;
    }

    public void setGenePenalty(double genePenalty) {
        this.genePenalty = genePenalty;
    }

    public String getFormattedGenePenalty() {
        return formattedGenePenalty;
    }

    public void setFormattedGenePenalty(String formattedGenePenalty) {
        this.formattedGenePenalty = formattedGenePenalty;
    }

    public boolean isWarfarinMatched() {
        return warfarinMatched;
    }

    public void setWarfarinMatched(boolean warfarinMatched) {
        this.warfarinMatched = warfarinMatched;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }
}
