package supersymmetry.common.metatileentities.multi.electric;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockFusionCasing;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.recipes.SuSyRecipeMaps;

public class MetaTileEntityMagnetohydrodynamicGenerator extends RecipeMapMultiblockController {

    public MetaTileEntityMagnetohydrodynamicGenerator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.MAGNETOHYDRODYNAMIC_FUELS);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMagnetohydrodynamicGenerator(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" A ", " C ", " A ", "   ", "   ")
                .aisle("   ", " B ", " B ", " B ", "   ")
                .aisle(" B ", "AAA", "AAA", "AAA", " B ")
                .aisle(" B ", "AAA", "EAE", "AAA", " B ")
                .aisle(" B ", "AAA", "AAA", "AAA", " B ")
                .aisle("   ", " B ", " B ", " B ", "   ")
                .aisle(" A ", " S ", " A ", "   ", "   ")
                .where('S', selfPredicate())
                .where('A', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.TUNGSTENSTEEL_ROBUST))
                        .or(autoAbilities(false, true, false, false, false, false, false).setExactLimit(1)))
                .where('B', states(MetaBlocks.FUSION_CASING.getState(BlockFusionCasing.CasingType.FUSION_CASING)))
                .where('C', abilities(MultiblockAbility.OUTPUT_ENERGY))
                .where('E', autoAbilities(false, false, false, false, true, false, false)
                        .or(autoAbilities(false, false, false, false, false, true, false)))
                .where(' ', any())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.ROBUST_TUNGSTENSTEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }
}
