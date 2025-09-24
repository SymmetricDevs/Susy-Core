package supersymmetry.api.recipes.builders.logic;

import static gregtech.api.recipes.logic.OverclockingLogic.*;

import javax.annotation.Nonnull;

import supersymmetry.api.recipes.catalysts.CatalystInfo;

public final class SuSyOverclockingLogic {

    private SuSyOverclockingLogic() {}

    @Nonnull
    public static double[] continuousOverclockingLogic(double recipeEUt, long maxVoltage, double recipeDuration,
                                                       int numberOfOCs, double durationDivisor,
                                                       double voltageMultiplier) {
        if (numberOfOCs <= 0) return new double[] { recipeEUt, recipeDuration };

        double resultDuration = recipeDuration;

        double resultVoltage;
        for (resultVoltage = recipeEUt; numberOfOCs > 0; --numberOfOCs) {
            double potentialVoltage = resultVoltage * voltageMultiplier;
            if (potentialVoltage > maxVoltage) {
                break;
            }

            resultDuration /= durationDivisor;
            resultVoltage = potentialVoltage;
        }

        return new double[] { resultVoltage, resultDuration };
    }

    @Nonnull
    public static int[] catalystOverclockingLogic(int recipeEUt, long maximumVoltage, int recipeDuration,
                                                  int maxOverclocks, @Nonnull CatalystInfo catalystInfo,
                                                  int recipeRequiredCatalystTier, double durationDivisor,
                                                  double voltageMultiplier) {
        int amountAboveRecipeTier = catalystInfo.getTier() - recipeRequiredCatalystTier;

        double energyEfficiency = catalystInfo.getEnergyEfficiency();
        double catalystDurationDivisor = catalystInfo.getSpeedEfficiency();

        // overclock recipe once with catalysts bonuses and once again with the usual overclock
        if (amountAboveRecipeTier > 0) {
            // apply a multiplicative energy multiplier for every tier above the required recipe catalyst tier
            // only apply this boost when the delta is > 0, since x^0 = 1, skipping some math
            recipeEUt *= Math.min(1, Math.pow(energyEfficiency, amountAboveRecipeTier));

            // overclock
            double resultDurationDouble = recipeDuration / Math.pow(catalystDurationDivisor, amountAboveRecipeTier);

            recipeDuration = resultDurationDouble < 1 ? 1 : (int) resultDurationDouble;

            // overclock normally
            return standardOverclockingLogic(recipeEUt, maximumVoltage, recipeDuration, maxOverclocks, durationDivisor,
                    voltageMultiplier);
        }

        // no perfects are performed, do normal overclocking
        return standardOverclockingLogic(recipeEUt, maximumVoltage, recipeDuration, maxOverclocks, durationDivisor,
                voltageMultiplier);
    }

    @Nonnull
    public static double[] continuousCatalystOverclockingLogic(int recipeEUt, long maximumVoltage, int recipeDuration,
                                                               int maxOverclocks, @Nonnull CatalystInfo catalystInfo,
                                                               int recipeRequiredCatalystTier, double durationDivisor,
                                                               double voltageMultiplier) {
        int amountAboveRecipeTier = catalystInfo.getTier() - recipeRequiredCatalystTier;

        double energyEfficiency = catalystInfo.getEnergyEfficiency();
        double catalystDurationDivisor = catalystInfo.getSpeedEfficiency();

        // calculate catalyst bonus catalysts bonuses and once again with the usual overclock
        if (amountAboveRecipeTier > 0) {
            // apply a multiplicative energy multiplier for every tier above the required recipe catalyst tier
            // only apply this boost when the delta is > 0, since x^0 = 1, skipping some math
            recipeEUt *= Math.min(1, Math.pow(energyEfficiency, amountAboveRecipeTier));

            // catalyst bonus
            double resultDuration = recipeDuration / Math.pow(catalystDurationDivisor, amountAboveRecipeTier);

            // overclock normally
            return continuousOverclockingLogic(recipeEUt, maximumVoltage, resultDuration, maxOverclocks,
                    durationDivisor, voltageMultiplier);
        }

        // no catalyst duration bonus performed
        return continuousOverclockingLogic(recipeEUt, maximumVoltage, recipeDuration, maxOverclocks, durationDivisor,
                voltageMultiplier);
    }
}
