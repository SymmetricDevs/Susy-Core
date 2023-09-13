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
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nonnull;

public class MetaTileEntityLargeWeaponsFactory extends RecipeMapMultiblockController {

    public MetaTileEntityLargeWeaponsFactory(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.LARGE_WEAPONS_FACTORY_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLargeWeaponsFactory(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("FBF", "FFF")
                .aisle("CBC", " A ")
                .aisle("CBC", " A ")
                .aisle("CBC", "EAE")
                .aisle("CBC", "EAE")
                .aisle("CBC", " A ")
                .aisle("CBC", " A ")
                .aisle("DDD", "DSD")
                .where('S', selfPredicate())
                .where('A', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID)))
                .where('B', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('C', states(MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel))
                .or(autoAbilities(false, true, false, false, false, false, false).setExactLimit(1)))
                .where('D', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                .or(autoAbilities(false, false, true, false, true, false, false)))
                .where('E', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                .or(autoAbilities(true, false, false, false, false, false, false)))
                .where('F', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                .or(autoAbilities(false, false, false, true, false, false, false)))
                .where(' ', any())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.LARGE_WEAPONS_FACTORY_OVERLAY;
    }
}
