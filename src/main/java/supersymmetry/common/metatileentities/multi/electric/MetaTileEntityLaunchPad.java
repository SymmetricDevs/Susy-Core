package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.metatileentities.multi.rocket.MetaTileEntityLandingPad;

public class MetaTileEntityLaunchPad extends RecipeMapMultiblockController {
    public MetaTileEntityLaunchPad(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.ROCKET_LAUNCH_PAD);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLandingPad(metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("     CCCCC     ")
                .aisle("   CC     CC   ")
                .aisle("  C         C  ")
                .aisle("  C         C  ")
                .aisle(" C           C ")
                .aisle(" C           C ")
                .aisle(" C           C ")
                .aisle(" C           C ")
                .aisle(" C           C ")
                .aisle("  C         C  ")
                .aisle("  C         C  ")
                .aisle("   CC     CC   ")
                .aisle("     CCCCC     ")
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }
}
