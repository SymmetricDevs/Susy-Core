package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import supersymmetry.api.capability.impl.ContinuousMultiblockRecipeLogic;
import supersymmetry.api.recipes.SuSyRecipeMaps;

public class MetaTileEntityFluidizedBedReactor extends RecipeMapMultiblockController {
    public MetaTileEntityFluidizedBedReactor(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.FLUIDIZED_BED_REACTOR_RECIPES);
        this.recipeMapWorkable = new ContinuousMultiblockRecipeLogic(this, true);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityFluidizedBedReactor(this.metaTileEntityId);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(new String[]{"F F", "BBB", "XXX", "XXX", "TTT"})
                .aisle(new String[]{"   ", "BPB", "XPX", "XPX", "TPT"})
                .aisle(new String[]{"F F", "BSB", "XXX", "XXX", "TTT"})
                .where('S', this.selfPredicate())
                .where('F', states(new IBlockState[]{this.getFrameState()}))
                .where('P', states(new IBlockState[]{this.getPipeCasingState()}))
                .where('X', states(new IBlockState[]{this.getCasingState()}).or(this.autoAbilities(false, false, true, false, false, false, false)))
                .where('T', states(new IBlockState[]{this.getCasingState()}).or(this.autoAbilities(false, true, false, false, false, true, false)))
                .where('B', states(new IBlockState[]{this.getCasingState()}).or(this.autoAbilities(true, false, false, false, true, false, false))).build();
    }
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.INERT_PTFE_CASING;
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.PTFE_INERT_CASING);
    }
    protected IBlockState getFrameState() {
        return MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel);
    }

    protected IBlockState getPipeCasingState() {
        return MetaBlocks.BOILER_CASING.getState(BoilerCasingType.POLYTETRAFLUOROETHYLENE_PIPE);
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(TooltipHelper.RAINBOW_SLOW + I18n.format("gregtech.machine.perfect_oc", new Object[0]));
    }

    @Nonnull
    protected ICubeRenderer getFrontOverlay() {
        return Textures.LARGE_CHEMICAL_REACTOR_OVERLAY;
    }
}
