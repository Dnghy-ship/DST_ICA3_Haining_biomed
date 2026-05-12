package cn.edu.zju.service;

import cn.edu.zju.bean.PatientProfile;
import cn.edu.zju.bean.VariantAnnotation;
import cn.edu.zju.bean.VariantCore;
import cn.edu.zju.bean.WarfarinDoseSummary;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class DosageCalculatorService {

    private static final double VKORC1_PENALTY = -0.8677d;
    private static final double CYP2C9_PENALTY = -0.5211d;

    public Double calculateWarfarinDose(PatientProfile profile, List<VariantCore> patientVariants) {
        if (!isValidProfile(profile)) {
            return null;
        }
        GeneFlags flags = resolveGeneFlags(patientVariants);
        return calculateWarfarinDose(profile, calculateGenePenalty(flags));
    }

    public WarfarinDoseSummary buildWarfarinDoseSummary(PatientProfile profile,
                                                        List<VariantCore> patientVariants,
                                                        boolean warfarinMatched) {
        WarfarinDoseSummary summary = new WarfarinDoseSummary();
        summary.setWarfarinMatched(warfarinMatched);
        boolean profileValid = isValidProfile(profile);
        summary.setProfileValid(profileValid);
        summary.setVariantsPresent(patientVariants != null && !patientVariants.isEmpty());
        if (!profileValid) {
            summary.setStatusMessage("Patient profile is incomplete; warfarin dose cannot be calculated.");
            return summary;
        }

        GeneFlags flags = resolveGeneFlags(patientVariants);
        summary.setHasVkorc1(flags.hasVkorc1);
        summary.setHasCyp2c9(flags.hasCyp2c9);
        double genePenalty = calculateGenePenalty(flags);
        summary.setGenePenalty(genePenalty);
        summary.setFormattedGenePenalty(String.format(Locale.ROOT, "%.4f", genePenalty));

        Double dose = calculateWarfarinDose(profile, genePenalty);
        summary.setWeeklyDose(dose);
        if (dose != null) {
            summary.setFormattedDose(String.format(Locale.ROOT, "%.2f", dose));
        }

        if (warfarinMatched) {
            summary.setStatusMessage("Calculated from patient profile and genotype flags; applied to Warfarin label.");
        } else {
            summary.setStatusMessage("Calculated from patient profile; no Warfarin label matched, dose shown for reference.");
        }
        return summary;
    }

    private double calculateWarfarinDose(PatientProfile profile, double genePenalty) {
        double sqrtWeeklyDose = 5.6044d
                - 0.2614d * (profile.getAge() / 10.0d)
                + 0.0087d * profile.getHeight().doubleValue()
                + 0.0128d * profile.getWeight().doubleValue()
                + genePenalty;
        return sqrtWeeklyDose * sqrtWeeklyDose;
    }

    private boolean isValidProfile(PatientProfile profile) {
        if (profile == null || profile.getAge() == null || profile.getHeight() == null || profile.getWeight() == null) {
            return false;
        }
        return profile.getAge() > 0
                && profile.getHeight().doubleValue() > 0
                && profile.getWeight().doubleValue() > 0;
    }

    private double calculateGenePenalty(GeneFlags flags) {
        double penalty = 0d;
        if (flags.hasVkorc1) {
            penalty += VKORC1_PENALTY;
        }
        if (flags.hasCyp2c9) {
            penalty += CYP2C9_PENALTY;
        }
        return penalty;
    }

    private GeneFlags resolveGeneFlags(List<VariantCore> patientVariants) {
        GeneFlags flags = new GeneFlags();
        if (patientVariants == null || patientVariants.isEmpty()) {
            return flags;
        }
        for (VariantCore variant : patientVariants) {
            String symbol = Optional.ofNullable(variant)
                    .map(VariantCore::getAnnotation)
                    .map(VariantAnnotation::getGeneSymbol)
                    .orElse(null);
            if (symbol == null) {
                continue;
            }
            String normalized = symbol.toUpperCase(Locale.ROOT);
            if (normalized.contains("VKORC1")) {
                flags.hasVkorc1 = true;
            }
            if (normalized.contains("CYP2C9")) {
                flags.hasCyp2c9 = true;
            }
            if (flags.hasVkorc1 && flags.hasCyp2c9) {
                break;
            }
        }
        return flags;
    }

    private static class GeneFlags {
        private boolean hasVkorc1;
        private boolean hasCyp2c9;
    }
}
