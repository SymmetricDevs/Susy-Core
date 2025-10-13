package supersymmetry.api.metatileentity;

import java.util.function.Function;

import net.minecraft.util.ResourceLocation;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.SimpleMachineMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import supersymmetry.api.capability.impl.CatalystRecipeLogic;

public class CatalystMachineMetaTileEntity extends SimpleMachineMetaTileEntity {

    public CatalystMachineMetaTileEntity(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap,
                                         ICubeRenderer renderer, int tier, boolean hasFrontFacing,
                                         Function<Integer, Integer> tankScalingFunction) {
        super(metaTileEntityId, recipeMap, renderer, tier, hasFrontFacing, tankScalingFunction);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new CatalystMachineMetaTileEntity(this.metaTileEntityId, this.workable.getRecipeMap(), this.renderer,
                this.getTier(), this.hasFrontFacing(), this.getTankScalingFunction());
    }

    @Override
    protected CatalystRecipeLogic createWorkable(RecipeMap<?> recipeMap) {
        return new CatalystRecipeLogic(this, recipeMap, () -> this.energyContainer);
    }
}
