package supersymmetry.common.metatileentities.single.electric;

import java.util.function.Supplier;

import net.minecraft.util.ResourceLocation;

import gregtech.api.GTValues;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityScreenPrinter extends SimpleMachineMetaTileEntity {

    public MetaTileEntityScreenPrinter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.SCREEN_PRINTER, SusyTextures.SCREEN_PRINTER_OVERLAY, 1, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityScreenPrinter(metaTileEntityId);
    }

    @Override
    protected RecipeLogicEnergy createWorkable(RecipeMap<?> recipeMap) {
        return new ScreenPrinterRecipeLogic(this, recipeMap, () -> energyContainer);
    }

    public class ScreenPrinterRecipeLogic extends RecipeLogicEnergy {

        public ScreenPrinterRecipeLogic(MetaTileEntity tileEntity, RecipeMap<?> recipeMap,
                                        Supplier<IEnergyContainer> energyContainer) {
            super(tileEntity, recipeMap, energyContainer);
        }

        @Override
        public long getMaxVoltage() {
            return GTValues.EV;
        }
    }
}
