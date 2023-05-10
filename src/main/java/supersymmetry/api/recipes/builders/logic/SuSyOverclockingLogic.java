package supersymmetry.api.recipes.builders.logic;

import supersymmetry.api.SusyLog;
import supersymmetry.api.recipes.catalysts.CatalystInfo;

import javax.annotation.Nonnull;

import static gregtech.api.recipes.logic.OverclockingLogic.*;

public class SuSyOverclockingLogic {


    @Nonnull
    public static double[] continuousOverclockingLogic(double recipeEUt, long maxVoltage, double recipeDuration, int numberOfOCs, double durationDivisor, double voltageMultiplier) {
        if(numberOfOCs <= 0) return new double[] {recipeEUt, recipeDuration};

        double resultDuration = recipeDuration;

        double resultVoltage;
        for(resultVoltage = recipeEUt; numberOfOCs > 0; --numberOfOCs) {
            double potentialVoltage = resultVoltage * voltageMultiplier;
            if (potentialVoltage > (double)maxVoltage) {
                break;
            }

            double potentialDuration = resultDuration / durationDivisor;

            resultDuration = potentialDuration;
            resultVoltage = potentialVoltage;
        }

        SusyLog.logger.info(resultDuration);
        return new double[]{resultVoltage, resultDuration};
    }


    @Nonnull
    public static int[] catalystOverclockingLogic(int recipeEUt, long maximumVoltage, int recipeDuration, int maxOverclocks, CatalystInfo catalystInfo, int recipeRequiredCatalystTier) {
        int amountAboveRecipeTier = catalystInfo.getTier() - recipeRequiredCatalystTier;

        double energyEfficiency = catalystInfo.getEnergyEfficiency();
        double catalystDurationDivisor = catalystInfo.getSpeedEfficiency();

        // apply a multiplicative energy multiplier for every tier above the required recipe catalyst tier
        recipeEUt *= Math.min(1, Math.pow(energyEfficiency, amountAboveRecipeTier));


        // overclock recipe once with catalysts bonuses and once again with the usual overclock
        if (amountAboveRecipeTier > 0) {
            // overclock
            int[] overclock = standardOverclockingLogic(recipeEUt, maximumVoltage, recipeDuration, amountAboveRecipeTier, catalystDurationDivisor, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER);

            // overclock normally
            return standardOverclockingLogic(overclock[0], maximumVoltage, overclock[1], maxOverclocks, STANDARD_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER);
        }

        // no perfects are performed, do normal overclocking
        return standardOverclockingLogic(recipeEUt, maximumVoltage, recipeDuration, maxOverclocks, STANDARD_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER);
    }

    @Nonnull
    public static double[] continuousCatalystOverclockingLogic(int recipeEUt, long maximumVoltage, int recipeDuration, int maxOverclocks, CatalystInfo catalystInfo, int recipeRequiredCatalystTier) {
        int amountAboveRecipeTier = catalystInfo.getTier() - recipeRequiredCatalystTier;

        double energyEfficiency = catalystInfo.getEnergyEfficiency();
        double catalystDurationDivisor = catalystInfo.getSpeedEfficiency();

        // apply a multiplicative energy multiplier for every tier above the required recipe catalyst tier
        recipeEUt *= Math.min(1, Math.pow(energyEfficiency, amountAboveRecipeTier));

        // calculate catalyst bonus catalysts bonuses and once again with the usual overclock
        if (amountAboveRecipeTier > 0) {
            // catalyst bonus
            double resultDuration = recipeDuration / Math.pow(catalystDurationDivisor, amountAboveRecipeTier);

            // overclock normally
            return continuousOverclockingLogic(recipeEUt, maximumVoltage, resultDuration, maxOverclocks, STANDARD_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER);
        }

        // no catalyst duration bonus performed
        return continuousOverclockingLogic(recipeEUt, maximumVoltage, recipeDuration, maxOverclocks, STANDARD_OVERCLOCK_DURATION_DIVISOR, STANDARD_OVERCLOCK_VOLTAGE_MULTIPLIER);
    }

}
