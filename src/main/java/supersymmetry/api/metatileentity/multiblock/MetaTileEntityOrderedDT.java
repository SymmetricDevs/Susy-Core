package supersymmetry.api.metatileentity.multiblock;

import gregtech.api.capability.IDistillationTower;
import gregtech.api.capability.impl.DistillationTowerLogicHandler;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.RecipeMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.common.recipes.DistillationTowerRecipeLogic;

import java.util.List;
import java.util.function.Function;

import static gregtech.api.util.RelativeDirection.UP;

public abstract class MetaTileEntityOrderedDT extends RecipeMapMultiblockController implements IDistillationTower {

    protected DistillationTowerLogicHandler handler;

    public MetaTileEntityOrderedDT(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
        this.handler = createHandler();
        this.recipeMapWorkable = new DistillationTowerRecipeLogic(this);
    }

    @NotNull
    public DistillationTowerLogicHandler createHandler() {
        return new DistillationTowerLogicHandler(this);
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

    @Override
    public void addInformation(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.machine.ordered_dt.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.ordered_dt.tooltip.2"));
    }
}
