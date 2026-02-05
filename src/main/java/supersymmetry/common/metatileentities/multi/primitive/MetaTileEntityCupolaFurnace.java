package supersymmetry.common.metatileentities.multi.primitive;

import static gregtech.api.util.RelativeDirection.*;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.*;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.capability.impl.PrimitiveRecipeLogic;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityCupolaFurnace extends RecipeMapPrimitiveMultiblockController {

    public int size;

    public MetaTileEntityCupolaFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.CUPOLA_FURNACE);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityCupolaFurnace(this.metaTileEntityId);
    }

    public static TraceabilityPredicate isIndicatorPredicate() {
        return new TraceabilityPredicate((blockWorldState) -> {
            if (air().test(blockWorldState)) {
                blockWorldState.getMatchContext().increment("tankLength", 1);
                return true;
            } else
                return false;
        });
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, BACK, UP)
                .aisle("FAF", "AOA", "FAF")
                .aisle("CCC", "CHC", "CSC")
                .aisle("CCC", "CHC", "CCC").setRepeatable(2, 8)
                .where('C',
                        states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PRIMITIVE_BRICKS))
                                .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1)
                                        .setMaxGlobalLimited(4)))
                .where('A', air())
                .where('H', isIndicatorPredicate())
                .where('S', selfPredicate())
                .where(' ', air())
                .where('O', abilities(MultiblockAbility.EXPORT_ITEMS))
                .where('F', frames(Materials.Steel))
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.initializeAbilities();
    }

    @Override
    protected void initializeAbilities() {
        this.importItems = new ItemHandlerList(getAbilities(MultiblockAbility.IMPORT_ITEMS));
        this.exportItems = new ItemHandlerList(getAbilities(MultiblockAbility.EXPORT_ITEMS));
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.PRIMITIVE_BRICKS;
    }

    @SideOnly(Side.CLIENT)
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.CUPOLA_FURNACE_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                recipeMapWorkable.isActive(), recipeMapWorkable.isWorkingEnabled());
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public void update() {
        super.update();
    }

    public class CupolaFurnaceLogic extends PrimitiveRecipeLogic {

        public CupolaFurnaceLogic(RecipeMapPrimitiveMultiblockController tileEntity,
                                  RecipeMap<?> recipeMap) {
            super(tileEntity, recipeMap);
        }

        public int getParallelLimit() {
            return ((MetaTileEntityCupolaFurnace) this.getMetaTileEntity()).size;
        }

        protected long getMaxParallelVoltage() {
            return 2147432767L;
        }

        public @NotNull ParallelLogicType getParallelLogicType() {
            return ParallelLogicType.MULTIPLY;
        }
    }
}
