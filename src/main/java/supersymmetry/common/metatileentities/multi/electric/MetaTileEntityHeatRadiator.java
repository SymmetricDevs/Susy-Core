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
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import supersymmetry.api.capability.impl.NoEnergyMultiblockRecipeLogic;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockSerpentine;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityHeatRadiator extends RecipeMapMultiblockController {

    public MetaTileEntityHeatRadiator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.HEAT_RADIATOR_RECIPES);
        this.recipeMapWorkable = new NoEnergyMultiblockRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityHeatRadiator(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, UP)
                .aisle("AAAAASAAAAA")
                .aisle("CBBBBBBBBBC").setRepeatable(1,14)
                .aisle("AAAAAAAAAAA")
                .where('S', selfPredicate())
                .where('A', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities(false, true, false, false, false, false, false)))
                .where('B', states(getRadiatorElementState()))
                .where('C', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities(false, false, false, false, true, false, false).setMinGlobalLimited(1))
                        .or(autoAbilities(false, false, false, false, false, true, false).setMinGlobalLimited(1)))
                .build();
    }

    public IBlockState getRadiatorElementState() {
        return SuSyBlocks.SERPENTINE.getState(BlockSerpentine.SerpentineType.BASIC);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.RADIATOR_OVERLAY;
    }
}
