package cn.edu.zju.service;

import cn.edu.zju.bean.PatientProfile;
import cn.edu.zju.bean.VariantAnnotation;
import cn.edu.zju.bean.VariantCore;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class DosageCalculatorService {

    private static final double VKORC1_PENALTY = -0.8677d;
    private static final double CYP2C9_PENALTY = -0.5211d;

    public Double calculateWarfarinDose(PatientProfile profile, List<VariantCore> patientVariants) {
        // This PoC intentionally follows the provided simplified formula terms only:
        // age, height, weight, and placeholder gene penalties.
        if (profile == null || profile.getAge() == null || profile.getHeight() == null || profile.getWeight() == null) {
            return null;
        }
        if (profile.getAge() <= 0 || profile.getHeight().doubleValue() <= 0 || profile.getWeight().doubleValue() <= 0) {
            return null;
        }

        double genePenalty = resolveGenePenalty(patientVariants);
        double sqrtWeeklyDose = 5.6044d
                - 0.2614d * (profile.getAge() / 10.0d)
                + 0.0087d * profile.getHeight().doubleValue()
                + 0.0128d * profile.getWeight().doubleValue()
                + genePenalty;
        return sqrtWeeklyDose * sqrtWeeklyDose;
    }

    private double resolveGenePenalty(List<VariantCore> patientVariants) {
        if (patientVariants == null || patientVariants.isEmpty()) {
            return 0d;
        }
        boolean hasVkorc1 = false;
        boolean hasCyp2c9 = false;
        for (VariantCore variant : patientVariants) {
            String symbol = Optional.ofNullable(variant)
                    .map(VariantCore::getAnnotation)
                    .map(VariantAnnotation::getGeneSymbol)
                    .orElse(null);
            if (symbol == null || symbol.isBlank()) {
                continue;
            }
            String normalized = symbol.toUpperCase(Locale.ROOT);
            if (normalized.contains("VKORC1")) {
                hasVkorc1 = true;
            }
            if (normalized.contains("CYP2C9")) {
                hasCyp2c9 = true;
            }
            if (hasVkorc1 && hasCyp2c9) {
                break;
            }
        }
        double penalty = 0d;
        if (hasVkorc1) {
            penalty += VKORC1_PENALTY;
        }
        if (hasCyp2c9) {
            penalty += CYP2C9_PENALTY;
        }
        return penalty;
    }
}
