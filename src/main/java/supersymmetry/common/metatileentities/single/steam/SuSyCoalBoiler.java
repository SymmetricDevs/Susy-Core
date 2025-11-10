package supersymmetry.common.metatileentities.single.steam;

import static supersymmetry.api.capability.impl.SuSyBoilerLogic.isSupportedOrePrefix;

import java.util.Collections;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.Recipe;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.metatileentities.steam.boiler.SteamCoalBoiler;
import supersymmetry.api.recipes.SuSyRecipeMaps;

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
        burnTime = modifyBurnTime(burnTime, isHighPressure);

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

    public static int modifyBurnTime(int burnTime, boolean highPressure) {
        // So the burn time is stupid if we are eventually extending the CEu boiler.
        // In there, burn progress will only increment every 12 ticks. It increments by 2
        // for high pressure, and 1 for low pressure.
        // So we have to divide the burn time by 12 (low) or 6 (high) to make it match the
        // actual recipe duration.
        burnTime /= highPressure ? 6 : 12;

        // Another fun fact! The temperature only increases when the burn time is even.
        // So for a high pressure boiler, since it decrements by 2, an odd number means it
        // *never warms up*. We will throw players a bone and give them 1 more tick if it's odd.
        // It also never stops working if it's odd because it does a == 0 check and not a <= 0.
        if (highPressure && burnTime % 2 == 1) {
            burnTime++;
        }
        return burnTime;
    }
}
