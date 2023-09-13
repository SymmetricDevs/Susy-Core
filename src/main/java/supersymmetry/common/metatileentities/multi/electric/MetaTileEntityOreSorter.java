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
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nonnull;

public class MetaTileEntityOreSorter extends RecipeMapMultiblockController {

    public MetaTileEntityOreSorter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.ORE_SORTER_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityOreSorter(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" C C ", " C C ", " C C ", " D D ")
                .aisle("     ", "     ", "     ", " D D ")
                .aisle("ABBBA", "ABBBA", "ABBBA", " D D ")
                .aisle("ABBBA", "B###B", "ABBBA", " D D ")
                .aisle("ABSBA", "ABBBA", "ABBBA", " D D ")
                .where('S', selfPredicate())
                .where('A', states(MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel)))
                .where('B', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                        .setMinGlobalLimited(16)
                        .or(autoAbilities(true, true, true, true, false, false, false)))
                .where('C', states(MetaBlocks.BOILER_CASING.getState(BoilerCasingType.STEEL_PIPE))
                        .or(autoAbilities(false, false, false, false, true, true, false)))
                .where('D', states(MetaBlocks.FRAMES.get(Materials.Aluminium).getBlock(Materials.Aluminium)))
                .where(' ', any())
                .where('#', air())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.ORE_SORTER_OVERLAY;
    }
}
