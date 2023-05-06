package supersymmetry.api.recipes.catalysts;



public class CatalystInfo {
    private int tier;
    private double yieldEfficiency;
    private double energyEfficiency;
    private double speedEfficiency;


    public CatalystInfo(int tier, double yieldEfficiency, double energyEfficiency, double speedEfficiency){
        this.tier = tier;
        this.yieldEfficiency = yieldEfficiency;
        this.energyEfficiency = energyEfficiency;
        this.speedEfficiency = speedEfficiency;
    }


    public int getTier () {
        return tier;
    }

    public double getYieldEfficiency () {
        return yieldEfficiency;
    }

    public double getEnergyEfficiency () {
        return energyEfficiency;
    }

    public double getSpeedEfficiency () {
        return speedEfficiency;
    }



}
