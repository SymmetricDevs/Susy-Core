package supersymmetry.api.recipes.catalysts;

import javax.annotation.Nonnull;

public class CatalystInfo implements Comparable<CatalystInfo> {

    public static final int NO_TIER = -1;

    private final int tier;
    private final double yieldEfficiency;
    private final double energyEfficiency;
    private final double speedEfficiency;

    public CatalystInfo(int tier, double yieldEfficiency, double energyEfficiency, double speedEfficiency) {
        this.tier = tier;
        this.yieldEfficiency = yieldEfficiency;
        this.energyEfficiency = energyEfficiency;
        this.speedEfficiency = speedEfficiency;
    }

    public int getTier() {
        return tier;
    }

    public double getYieldEfficiency() {
        return yieldEfficiency;
    }

    public double getEnergyEfficiency() {
        return energyEfficiency;
    }

    public double getSpeedEfficiency() {
        return speedEfficiency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CatalystInfo that = (CatalystInfo) o;

        if (getTier() != that.getTier()) return false;
        if (Double.compare(that.getYieldEfficiency(), getYieldEfficiency()) != 0) return false;
        if (Double.compare(that.getEnergyEfficiency(), getEnergyEfficiency()) != 0) return false;
        return Double.compare(that.getSpeedEfficiency(), getSpeedEfficiency()) == 0;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = getTier();
        temp = Double.doubleToLongBits(getYieldEfficiency());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getEnergyEfficiency());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getSpeedEfficiency());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public int compareTo(@Nonnull CatalystInfo o) {
        // compare order: Tier, Speed, Yield, Energy
        int result = Integer.compare(this.getTier(), o.getTier());
        if (result != 0) return result;
        result = Double.compare(this.getSpeedEfficiency(), o.getSpeedEfficiency());
        if (result != 0) return result;
        result = Double.compare(this.getYieldEfficiency(), o.getYieldEfficiency());
        if (result != 0) return result;
        result = Double.compare(this.getEnergyEfficiency(), o.getEnergyEfficiency());
        return result;
    }
}
