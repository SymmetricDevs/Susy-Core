package supersymmetry.common.metatileentities.multi.primitive;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.PrimitiveRecipeLogic;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.api.items.itemhandlers.GTItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.metatileentity.multiblock.RecipeMapSteamMultiblockController;
import gregtech.api.pattern.*;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.recipes.RecipeMaps;
import gregtech.client.renderer.ICubeRenderer;
import gregtechfoodoption.client.GTFOClientHandler;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

import java.util.Collections;
import java.util.List;

public class MetaTileEntityPrimitiveSmelter extends RecipeMapPrimitiveMultiblockController {
    protected IItemHandlerModifiable inputInventory;
    protected IItemHandlerModifiable outputInventory;

    public MetaTileEntityPrimitiveSmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.PRIMITIVE_SMELTER);
        this.recipeMapWorkable = new PrimitiveSmelterRecipeLogic(this);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("OOO", "III", "SIS")
                .aisle("OOO", "I I", "III")
                .aisle("OOO", "ICI", "SIS")
                .where('I', casingPredicate().or(abilities(SuSyMultiblockAbilities.PRIMITIVE_IMPORT_ITEMS).setMaxGlobalLimited(3)))
                .where('C', selfPredicate())
                .where('O', casingPredicate().or(abilities(SuSyMultiblockAbilities.PRIMITIVE_EXPORT_ITEMS).setMaxGlobalLimited(1)))
                .where('S', states(ModuleCore.Blocks.MASONRY_BRICK_SLAB.getDefaultState()))
                .build();
    }

    public TraceabilityPredicate casingPredicate() {
        return states(ModuleCore.Blocks.MASONRY_BRICK.getDefaultState());
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return SusyTextures.MASONRY_BRICK;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(),
                this.recipeMapWorkable.isActive(), this.recipeMapWorkable.isWorkingEnabled());
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityPrimitiveSmelter(this.metaTileEntityId);
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        return Collections.singletonList(MultiblockShapeInfo.builder()
                .aisle("BBB", "BBB", "SBS")
                .aisle("BBB", "B B", "BBB")
                .aisle("BOB", "ICI", "SBS")
                .where('B', ModuleCore.Blocks.MASONRY_BRICK.getDefaultState())
                .where('C', SuSyMetaTileEntities.PRIMITIVE_SMELTER, EnumFacing.SOUTH)
                .where('I', SuSyMetaTileEntities.PRIMITIVE_ITEM_IMPORT, EnumFacing.SOUTH)
                .where('O', SuSyMetaTileEntities.PRIMITIVE_ITEM_EXPORT, EnumFacing.SOUTH)
                .where('S', ModuleCore.Blocks.MASONRY_BRICK_SLAB.getDefaultState())
                .build());
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return GTFOClientHandler.BAKING_OVEN_OVERLAY;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    public void activate() {
        if (!this.recipeMapWorkable.isWorkingEnabled()) {
            this.recipeMapWorkable.setWorkingEnabled(true);
            this.recipeMapWorkable.forceRecipeRecheck();
        }
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.initializeAbilities();
    }

    protected void initializeAbilities() {
        this.inputInventory = new ItemHandlerList(getAbilities(SuSyMultiblockAbilities.PRIMITIVE_IMPORT_ITEMS));
        this.outputInventory = new ItemHandlerList(getAbilities(SuSyMultiblockAbilities.PRIMITIVE_EXPORT_ITEMS));
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        resetTileAbilities();
    }

    protected void resetTileAbilities() {
        this.inputInventory = new GTItemStackHandler(this, 0);
        this.outputInventory = new GTItemStackHandler(this, 0);
    }

    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer entityPlayer) {
        return ModularUI.builder(GuiTextures.PRIMITIVE_BACKGROUND, 176, 166)
                .shouldColor(false)
                .widget(new LabelWidget(5, 5, getMetaFullName()))
                .widget(new RecipeProgressWidget(this::getProgressDisplayPercent, 79, 32, 18, 18,
                        GuiTextures.PROGRESS_BAR_BOILER_FUEL.get(true), ProgressWidget.MoveType.VERTICAL,
                        SuSyRecipeMaps.PRIMITIVE_SMELTER))
                .bindPlayerInventory(entityPlayer.inventory, GuiTextures.PRIMITIVE_SLOT, 0);
    }

    public double getProgressDisplayPercent() {
        return this.isActive() ? recipeMapWorkable.getProgressPercent() : 0;
    }

    public static class PrimitiveSmelterRecipeLogic extends PrimitiveRecipeLogic {
        public PrimitiveSmelterRecipeLogic(RecipeMapPrimitiveMultiblockController tileEntity) {
            super(tileEntity, SuSyRecipeMaps.PRIMITIVE_SMELTER);
            setParallelLimit(8);
        }

        @Override
        protected void trySearchNewRecipe() {
            super.trySearchNewRecipe();
            if (this.progressTime == 0) {
                this.setWorkingEnabled(false);
            }
        }

        @Override
        protected IItemHandlerModifiable getInputInventory() {
            MetaTileEntityPrimitiveSmelter controller = (MetaTileEntityPrimitiveSmelter) metaTileEntity;
            return controller.getInputInventory();
        }

        @Override
        protected IItemHandlerModifiable getOutputInventory() {
            MetaTileEntityPrimitiveSmelter controller = (MetaTileEntityPrimitiveSmelter) metaTileEntity;
            return controller.getOutputInventory();
        }

        @Override
        public long getMaxVoltage() {
            return 8L; // To handle the parallel
        }
    }

    public IItemHandlerModifiable getInputInventory() {
        return inputInventory;
    }

    public IItemHandlerModifiable getOutputInventory() {
        return outputInventory;
    }


}
