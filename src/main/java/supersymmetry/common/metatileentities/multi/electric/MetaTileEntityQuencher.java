package supersymmetry.common.metatileentities.multi.electric;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityQuencher extends RecipeMapMultiblockController {

    public MetaTileEntityQuencher(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.QUENCHER_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityQuencher(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        // Different characters use common constraints. Copied from GCyM
        TraceabilityPredicate casingPredicate = states(getCasingState()).setMinGlobalLimited(15);

        return FactoryBlockPattern.start()
                .aisle("GGBBF", "GG   ", "     ")
                .aisle("GABC ", "GG D ", " CDD ")
                .aisle("GGB F", "GG   ", "     ")
                .aisle("  AAA", "  AAA", "     ")
                .aisle("  ASA", "  AAA", "     ")
                .where('S', selfPredicate())
                .where('A', casingPredicate)
                .where('B', states(MetaBlocks.BOILER_CASING.getState(BoilerCasingType.STEEL_PIPE)))
                .where('C',
                        states(MetaBlocks.TURBINE_CASING
                                .getState(BlockTurbineCasing.TurbineCasingType.STAINLESS_STEEL_GEARBOX)))
                .where('D', frames(Materials.StainlessSteel))
                .where('F', autoAbilities(false, false, false, false, false, true, false).setExactLimit(1)
                        .or(autoAbilities(false, false, false, false, true, false, false).setExactLimit(1)))
                .where('G', casingPredicate
                        .or(autoAbilities(true, true, true, true, false, false, false)))
                .where(' ', any())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.QUENCHER_OVERLAY;
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.STAINLESS_CLEAN);
    }
}
