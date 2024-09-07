package supersymmetry.common.metatileentities.multi.primitive;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.codetaylor.mc.pyrotech.modules.core.ModuleCore;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapPrimitiveMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.client.renderer.CubeRendererState;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.cclop.ColourOperation;
import gregtech.client.renderer.cclop.LightMapOperation;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityPrimitiveSmelter extends RecipeMapPrimitiveMultiblockController {

    public MetaTileEntityPrimitiveSmelter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.PRIMITIVE_SMELTER);
    }

    public static TraceabilityPredicate casingPredicate() {
        return states(ModuleCore.Blocks.MASONRY_BRICK.getDefaultState());
    }

    @Override
    protected void initializeAbilities() {
        this.importItems = new ItemHandlerList(getAbilities(SuSyMultiblockAbilities.PRIMITIVE_IMPORT_ITEMS));
        this.exportItems = new ItemHandlerList(getAbilities(SuSyMultiblockAbilities.PRIMITIVE_EXPORT_ITEMS));
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.initializeAbilities();
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("OOO", "III", "SIS")
                .aisle("OOO", "I I", "I I")
                .aisle("OOO", "ICI", "SIS")
                .where('I', casingPredicate().or(abilities(SuSyMultiblockAbilities.PRIMITIVE_IMPORT_ITEMS).setMaxGlobalLimited(3)))
                .where('C', selfPredicate())
                .where('O', casingPredicate().or(abilities(SuSyMultiblockAbilities.PRIMITIVE_EXPORT_ITEMS).setMaxGlobalLimited(2)))
                .where('S', states(ModuleCore.Blocks.MASONRY_BRICK_SLAB.getDefaultState()))
                .where(' ', air())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return SusyTextures.MASONRY_BRICK;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityPrimitiveSmelter(this.metaTileEntityId);
    }

    @SideOnly(Side.CLIENT)
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.PRIMITIVE_SMELTER_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(),
                recipeMapWorkable.isActive(), recipeMapWorkable.isWorkingEnabled());
        if (recipeMapWorkable.isActive() && isStructureFormed()) {
            EnumFacing back = getFrontFacing().getOpposite();
            Matrix4 offset = translation.copy().translate(back.getXOffset(), 0.2, back.getZOffset());
            CubeRendererState op = Textures.RENDER_STATE.get();
            Textures.RENDER_STATE.set(new CubeRendererState(op.layer, CubeRendererState.PASS_MASK, op.world));
            SusyTextures.SLAG_HOT.renderSided(EnumFacing.UP, renderState, offset,
                    ArrayUtils.addAll(pipeline, new LightMapOperation(240, 240), new ColourOperation(0xFFFFFFFF)));
            Textures.RENDER_STATE.set(op);
        }
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
}
