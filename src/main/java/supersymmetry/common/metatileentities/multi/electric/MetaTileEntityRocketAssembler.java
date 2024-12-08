package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.SuSyRecipeMaps;

public class MetaTileEntityRocketAssembler extends RecipeMapMultiblockController {

    public MetaTileEntityRocketAssembler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.ROCKET_ASSEMBLER);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return null;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return null;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return null;
    }
}
