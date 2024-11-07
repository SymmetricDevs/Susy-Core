package supersymmetry.api.metatileentity.multiblock;

import gregtech.api.capability.IDistillationTower;
import gregtech.api.capability.impl.DistillationTowerLogicHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static gregtech.api.util.RelativeDirection.UP;

public class MetaTileEntityOrderedDT extends RecipeMapMultiblockController implements IDistillationTower {
    protected DistillationTowerLogicHandler handler;

    public MetaTileEntityOrderedDT(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
        this.handler = new DistillationTowerLogicHandler(this);
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

    @Override
    public boolean allowSameFluidFillForOutputs() {
        return false;
    }

    public DistillationTowerLogicHandler getHandler() {
        return handler;
    }


    /**
     * Used if MultiblockPart Abilities need to be sorted a certain way, like
     * Distillation Tower and Assembly Line. <br>
     * <br>
     * There will be <i>consequences</i> if this is changed. Make sure to set the logic handler to one with
     * a properly overriden {@link DistillationTowerLogicHandler#determineOrderedFluidOutputs()}
     */
    @Override
    protected Function<BlockPos, Integer> multiblockPartSorter() {
        return UP.getSorter(getFrontFacing(), getUpwardsFacing(), isFlipped());
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        if (this.handler == null || this.structurePattern == null) return;
        handler.determineLayerCount(this.structurePattern);
        handler.determineOrderedFluidOutputs();
    }


    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        if (this.handler != null) handler.invalidate();
    }


    @Override
    public int getFluidOutputLimit() {
        if (this.handler != null) return this.handler.getLayerCount();
        else return super.getFluidOutputLimit();
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }
}
