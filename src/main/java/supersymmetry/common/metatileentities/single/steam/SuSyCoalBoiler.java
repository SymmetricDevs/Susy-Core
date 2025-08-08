package supersymmetry.common.metatileentities.single.steam;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.Recipe;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.common.metatileentities.steam.boiler.SteamCoalBoiler;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidUtil;
import supersymmetry.api.recipes.SuSyRecipeMaps;

import java.util.Collections;

import static supersymmetry.api.capability.impl.SuSyBoilerLogic.isSupportedOrePrefix;

public class SuSyCoalBoiler extends SteamCoalBoiler {
    public SuSyCoalBoiler(ResourceLocation metaTileEntityId, boolean isHighPressure) {
        super(metaTileEntityId, isHighPressure);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new SuSyCoalBoiler(metaTileEntityId, isHighPressure);
    }

    @Override
    protected void tryConsumeNewFuel() {
        ItemStack stack = importItems.extractItem(0, 1, true);
        OrePrefix prefix = OreDictUnifier.getPrefix(stack);
        if (!isSupportedOrePrefix(prefix)) return;

        Recipe solidFuelRecipe = SuSyRecipeMaps.BOILER_RECIPES.findRecipe(GTValues.V[GTValues.MAX],
                Collections.singletonList(stack), NonNullList.create());
        if (solidFuelRecipe == null) return;
        int burnTime = solidFuelRecipe.getDuration() * 1920 / this.getBaseSteamOutput();
        if (burnTime == 0) return;
        importItems.extractItem(0, 1, false);
        ItemStack remainderAsh = ModHandler.getBurningFuelRemainder(stack);
        if (!remainderAsh.isEmpty()) { // we don't care if we can't insert ash - it's chanced anyway
            exportItems.insertItem(0, remainderAsh, false);
        }
        setFuelMaxBurnTime(burnTime);
    }

    @Override
    protected int getBaseSteamOutput() {
        // 48/96 L per tick
        return isHighPressure ? 1920 : 960;
    }
}
