package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;

import javax.annotation.Nonnull;

public class MetaTileEntityHeatRadiator extends RecipeMapMultiblockController {

    public MetaTileEntityHeatRadiator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.HEAT_RADIATOR_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityHeatRadiator(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("AAAAASAAAAA", "DBBBBBBBBBC", "DBBBBBBBBBC", "DBBBBBBBBBC", "DBBBBBBBBBC", "DBBBBBBBBBC", "DBBBBBBBBBC", "DBBBBBBBBBC", "DBBBBBBBBBC", "DBBBBBBBBBC", "DBBBBBBBBBC", "DBBBBBBBBBC", "DBBBBBBBBBC", "DBBBBBBBBBC", "DBBBBBBBBBC", "DBBBBBBBBBC", "AAAAAAAAAAA")
                .where('S', selfPredicate())
                .where('A', states(new IBlockState[]{MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID)})
                        .or(autoAbilities(false, true, false, false, false, false, false)))
                .where('B', states(new IBlockState[]{MetaBlocks.BOILER_CASING.getState(BoilerCasingType.STEEL_PIPE)}))
                .where('C', states(new IBlockState[]{MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID)})
                        .or(autoAbilities(false, false, false, false, true, false, false)))
                .where('D', states(new IBlockState[]{MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID)})
                        .or(autoAbilities(false, false, false, false, false, true, false)))
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }
}
