package supersymmetry.common.metatileentities.multi.electric;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityPressureSwingAdsorber extends RecipeMapMultiblockController {

    public MetaTileEntityPressureSwingAdsorber(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.PRESSURE_SWING_ADSORBER_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityPressureSwingAdsorber(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("AAA", "AAA", "AAA", "AAA")
                .aisle("AAA", "ABA", "ABA", "AAA")
                .aisle("AAA", "ASA", "AAA", "AAA")
                .where('S', selfPredicate())
                .where('A',
                        states(MetaBlocks.METAL_CASING.getState(MetalCasingType.ALUMINIUM_FROSTPROOF))
                                .setMinGlobalLimited(25)
                                .or(autoAbilities(true, true, true, true, true, true, false)))
                .where('B', states(MetaBlocks.BOILER_CASING.getState(BoilerCasingType.STEEL_PIPE)))
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.FROST_PROOF_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.PRESSURE_SWING_ABSORBER_OVERLAY;
    }
}
