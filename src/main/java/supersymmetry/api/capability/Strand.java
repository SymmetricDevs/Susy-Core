package supersymmetry.api.capability;

import net.minecraft.nbt.NBTTagCompound;

import gregtech.api.GregTechAPI;
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

    public static NBTTagCompound serialize(NBTTagCompound nbt, Strand strand) {
        if (strand == null) {
            return nbt;
        }
        nbt.setDouble("Thickness", strand.thickness);
        nbt.setDouble("Width", strand.width);
        nbt.setBoolean("IsCut", strand.isCut);
        nbt.setString("Material", strand.material.toString());
        nbt.setInteger("Temperature", strand.temperature);
        return nbt;
    }

    public static Strand deserialize(NBTTagCompound nbt) {
        if (nbt == null || nbt.isEmpty()) {
            return null;
        }
        return new Strand(nbt.getDouble("Thickness"), nbt.getDouble("Width"), nbt.getBoolean("IsCut"),
                GregTechAPI.materialManager.getMaterial(nbt.getString("Material")), nbt.getInteger("Temperature"));
    }
}
