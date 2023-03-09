package supersymmetry.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.IMultipleTankHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.GTTransferUtils;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.items.IItemHandlerModifiable;
import supersymmetry.api.block.BlockCoagulationTankWall;
import supersymmetry.api.recipe.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;

public class MetaTileEntityCoagulationTank extends RecipeMapPrimitiveMultiblockController {
    protected IItemHandlerModifiable outputInventory;
    protected IMultipleTankHandler inputFluidInventory;
    public MetaTileEntityCoagulationTank(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.COAGULATION_RECIPES);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityCoagulationTank(this.metaTileEntityId);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(new String[]{"XXX", "XXX", "XXX"})
                .aisle(new String[]{"XXX", "X#X", "X#X"}).setRepeatable(1, 4)
                .aisle(new String[]{"XXX", "XYX", "XXX"})
                .where('X', states(new IBlockState[]{SuSyBlocks.COAGULATION_TANK_WALL.getState(BlockCoagulationTankWall.CoagulationTankWallType.WOODEN_COAGULATION_TANK_WALL)}).or(abilities(new MultiblockAbility[]{MultiblockAbility.EXPORT_ITEMS}).setExactLimit(1)).or(abilities(new MultiblockAbility[]{MultiblockAbility.IMPORT_FLUIDS}).setExactLimit(1)))
                .where('#', air())
                .where('Y', this.selfPredicate()).build();
    }

    @Override
    public void checkStructurePattern() {
        super.checkStructurePattern();
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return SusyTextures.WOODEN_COAGULATION_TANK_WALL;
    }

    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.PRIMITIVE_BACKGROUND, 176,166);
        builder.widget(new RecipeProgressWidget(this.recipeMapWorkable::getProgressPercent, 76, 39, 20, 15, GuiTextures.PRIMITIVE_BLAST_FURNACE_PROGRESS_BAR, ProgressWidget.MoveType.HORIZONTAL, SuSyRecipeMaps.COAGULATION_RECIPES));

        builder.widget((new TankWidget(this.importFluids.getTankAt(0), 48, 39, 18, 18)).setAlwaysShowFull(true).setBackgroundTexture(GuiTextures.PRIMITIVE_SLOT).setContainerClicking(true, true));
        builder.widget((new SlotWidget(this.exportItems, 0, 106, 39, true, true).setBackgroundTexture(GuiTextures.PRIMITIVE_SLOT)));

        return builder;
    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), this.recipeMapWorkable.isActive(), this.recipeMapWorkable.isWorkingEnabled());
    }

    public void update() {
        super.update();
        if (this.getOffsetTimer() % 5 == 0){
            for (IFluidTank iFluidTank : this.inputFluidInventory.getFluidTanks()) {
                if(iFluidTank.getFluid() != null){
                    NonNullList<FluidStack> fluidStacks = NonNullList.create();
                    int toFill = (this.importFluids.getTankAt(0).getCapacity() - this.importFluids.getTankAt(0).getFluidAmount());
                    int amount = iFluidTank.getFluidAmount() >= toFill ? toFill : iFluidTank.getFluidAmount();
                    fluidStacks.add(new FluidStack(iFluidTank.getFluid().getFluid(),amount));
                    if(GTTransferUtils.addFluidsToFluidHandler(this.importFluids,true, fluidStacks)) {
                        GTTransferUtils.addFluidsToFluidHandler(this.importFluids,false, fluidStacks);
                        iFluidTank.drain(amount, true);
                    }
                }
            }
            for (int i = 0; i < this.exportItems.getSlots(); i++) {
                ItemStack stack = this.exportItems.getStackInSlot(i);
                if(stack != null){
                    this.exportItems.setStackInSlot(i,GTTransferUtils.insertItem(this.outputInventory, stack,false));
                }
            }
            this.fillInternalTankFromFluidContainer();
        }
    }

    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.initializeAbilities();
    }

    @Override
    protected void initializeAbilities() {
        super.initializeAbilities();
        this.outputInventory = new ItemHandlerList(this.getAbilities(MultiblockAbility.EXPORT_ITEMS));
        this.inputFluidInventory = new FluidTankList(false, this.getAbilities(MultiblockAbility.IMPORT_FLUIDS));
    }

    @Nonnull
    protected ICubeRenderer getFrontOverlay() {
        return Textures.PRIMITIVE_PUMP_OVERLAY;
    }

    public boolean hasMaintenanceMechanics() {
        return false;
    }
}
