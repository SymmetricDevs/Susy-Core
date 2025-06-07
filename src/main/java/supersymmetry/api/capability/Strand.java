package supersymmetry.api.capability;

import gregtech.api.unification.material.Material;

public class Strand {
    public double thickness;
    public double width;
    public boolean isCut;
    public Material material;
    public int temperature;

    public Strand(double thickness, double width, boolean isCut, Material material, int temperature) {
        this.thickness = thickness;
        this.width = width;
        this.isCut = isCut;
        this.material = material;
        this.temperature = temperature;
    }

    public Strand(Strand strand) {
        this.thickness = strand.thickness;
        this.width = strand.width;
        this.isCut = strand.isCut;
        this.material = strand.material;
        this.temperature = strand.temperature;
    }
}
