package supersymmetry.api.capability;

import gregtech.api.unification.material.Material;

public class Strand {
    public int thickness;
    public int isCut;
    public Material material;
    public int temperature;

    public Strand(int thickness, int isCut, Material material, int temperature) {
        this.thickness = thickness;
        this.isCut = isCut;
        this.material = material;
        this.temperature = temperature;
    }
}
