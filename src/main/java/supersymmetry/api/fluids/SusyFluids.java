package supersymmetry.api.fluids;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import gregtech.api.GregTechAPI;
import gregtech.api.fluids.MaterialFluid;
import gregtech.api.fluids.fluidType.FluidType;
import gregtech.api.fluids.fluidType.FluidTypes;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialIconType;
import gregtech.api.util.FluidTooltipUtil;
import gregtech.api.util.GTUtility;
import gregtech.api.util.LocalizationUtils;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import supersymmetry.api.unification.material.info.SuSyMaterialFlags;

import java.util.ArrayList;
import java.util.List;

public class SusyFluids {

    private static final Table<Material, FluidType, ResourceLocation> fluidTextures = HashBasedTable.create();

    public static void init() {
        for (Material material : GregTechAPI.materialManager.getRegisteredMaterials()) {
            boolean slurryFlag = material.hasFlag(SuSyMaterialFlags.GENERATE_SLURRIES);

            if (slurryFlag) {
                registerSlurry(material);
            }
        }
    }

    public static void registerSlurry(Material material) {

        if (!fluidTextures.contains(material, FluidTypes.LIQUID)) {
            fluidTextures.put(material, FluidTypes.LIQUID, MaterialIconType.fluid.getBlockTexturePath(material.getMaterialIconSet()));
        }

        ResourceLocation textureLocation = fluidTextures.get(material, FluidTypes.LIQUID);

        Fluid impureSlurry = new MaterialFluid("impure_" + material + "_slurry", material, SusyFluidTypes.IMPURE_SLURRY, textureLocation);
        impureSlurry.setTemperature(293);
        impureSlurry.setDensity((int) (material.getMass() * 100));

        Fluid pureSlurry = new MaterialFluid(material + "_slurry", material, SusyFluidTypes.PURE_SLURRY, textureLocation);
        pureSlurry.setTemperature(293);
        pureSlurry.setDensity((int) (material.getMass() * 100));

        if (material.hasFluidColor()) {
            pureSlurry.setColor(GTUtility.convertRGBtoOpaqueRGBA_MC(material.getMaterialRGB()));
            impureSlurry.setColor(GTUtility.convertRGBtoOpaqueRGBA_MC((int) (material.getMaterialRGB() * 0.999)));
        }

        List<String> tooltip = new ArrayList<>();

        tooltip.add(LocalizationUtils.format("gregtech.fluid.temperature", 293));
        tooltip.add(LocalizationUtils.format(FluidTypes.LIQUID.getUnlocalizedTooltip()));
        tooltip.addAll(FluidTypes.LIQUID.getAdditionalTooltips());

        FluidTooltipUtil.registerTooltip(impureSlurry, tooltip);
        FluidTooltipUtil.registerTooltip(pureSlurry, tooltip);

        FluidRegistry.registerFluid(impureSlurry);
        FluidRegistry.addBucketForFluid(impureSlurry);

        FluidRegistry.registerFluid(pureSlurry);
        FluidRegistry.addBucketForFluid(pureSlurry);
    }
}
