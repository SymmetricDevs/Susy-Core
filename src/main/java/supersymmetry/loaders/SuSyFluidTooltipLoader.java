package supersymmetry.loaders;

import com.google.common.collect.Lists;
import gregtech.api.fluids.attribute.AttributedFluid;
import gregtech.api.fluids.FluidState;
import gregtech.api.fluids.GTFluid;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.util.FluidTooltipUtil;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.TextFormatting;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class SuSyFluidTooltipLoader {

    public static void registerTooltips() {
        Supplier<List<String>> liquidVoidableTooltip = () -> Lists.newArrayList(TextFormatting.YELLOW + I18n.format("susy.fluid.voiding.liquid"));
        Supplier<List<String>> gasVoidableTooltip = () -> Lists.newArrayList(TextFormatting.YELLOW + I18n.format("susy.fluid.voiding.gas"));
        Supplier<List<String>> flammableVoidableTooltip = () -> Lists.newArrayList(TextFormatting.YELLOW + I18n.format("susy.fluid.voiding.flammable"));
    
        for (Fluid fluid : FluidRegistry.getRegisteredFluids().values()) {
            if (fluid instanceof AttributedFluid aFluid) {
                FluidState state = aFluid.getState();
                if (fluid instanceof GTFluid.GTMaterialFluid gtFluid) {
                    Material mat = gtFluid.getMaterial();
                    if (mat.hasFlag(MaterialFlags.FLAMMABLE)) {
                        FluidTooltipUtil.registerTooltip(fluid, flammableVoidableTooltip);
                        continue;
                    }
                }
                if (state == FluidState.LIQUID) FluidTooltipUtil.registerTooltip(fluid, liquidVoidableTooltip);
                else if (state == FluidState.GAS) FluidTooltipUtil.registerTooltip(fluid, gasVoidableTooltip);
            }
        }
    }
}
