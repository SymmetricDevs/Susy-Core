package supersymmetry;

import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

import gregtech.api.unification.material.MarkerMaterials.*;
import gregtech.api.unification.material.Material;

public class SuSyValues {

    public static Material[] TierMaterials = new Material[] { Tier.ULV, Tier.LV, Tier.MV, Tier.HV, Tier.EV, Tier.IV,
            Tier.LuV, Tier.ZPM, Tier.UV, Tier.UHV, Tier.UEV, Tier.UIV, Tier.UXV, Tier.OpV, Tier.MAX };
    public static String MODID_IMMERSIVERAILROADING = "immersiverailroading";
    public static String MODID_LITTLE_TILES = "littletiles";

    public static final ModelResourceLocation modelRocket = new ModelResourceLocation(
            new ResourceLocation(Supersymmetry.MODID, "soyuz"), "inventory");
}
