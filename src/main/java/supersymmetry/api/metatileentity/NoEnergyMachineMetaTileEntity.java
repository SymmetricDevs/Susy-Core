package supersymmetry.api.metatileentity;

import gregtech.api.capability.impl.RecipeLogicEnergy;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.util.ResourceLocation;
import supersymmetry.api.capability.impl.NoEnergyRecipeLogic;

import java.util.function.Function;

public class NoEnergyMachineMetaTileEntity extends SimpleMachineMetaTileEntity {
    public NoEnergyMachineMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap, ICubeRenderer renderer, int tier, boolean hasFrontFacing, Function<Integer, Integer> tankScalingFunction) {
        super(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, tankScalingFunction);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new NoEnergyMachineMetaTileEntity(this.metaTileEntityId, this.workable.getRecipeMap(), this.renderer, this.getTier(), this.hasFrontFacing(), this.getTankScalingFunction());
    }

    @Override
    protected NoEnergyRecipeLogic createWorkable(RecipeMap<?> recipeMap) {
        return new NoEnergyRecipeLogic(this, recipeMap, () -> this.energyContainer);
    }
}
