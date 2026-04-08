package supersymmetry.common.metatileentities.single.electric;

import static gregtech.api.recipes.logic.OverclockingLogic.standardOverclockingLogic;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.EnergyContainerHandler;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.recipeproperties.IRecipePropertyStorage;
import gregtech.api.util.GTUtility;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityScreenPrinter extends SimpleMachineMetaTileEntity {
    public MetaTileEntityScreenPrinter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.SCREEN_PRINTER, SusyTextures.SCREEN_PRINTER, 1, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBathCondenser(metaTileEntityId);
    }

    public class ScreenPrinterRecipeLogic extends RecipeLogicEnergy {
        public ScreenPrinterRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap, Supplier<IEnergyContainer> energyContainer) {
            super(tileEntity, recipeMap, energyContainer);
        }

        @Override
        public long getMaxVoltage() {
            return GTValues.EV;
        }
    }
}
