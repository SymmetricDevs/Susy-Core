package supersymmetry.common.metatileentities.multi.electric;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

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
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityLargeWeaponsFactory extends RecipeMapMultiblockController {

    public MetaTileEntityLargeWeaponsFactory(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.LARGE_WEAPONS_FACTORY_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLargeWeaponsFactory(metaTileEntityId);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        TraceabilityPredicate casingPredicate = states(getCasingState()).setMinGlobalLimited(4);
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
                .where('A', casingPredicate)
                .where('B',
                        states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('C', frames(Materials.Steel)
                        .or(autoAbilities(false, true, false, false, false, false, false).setExactLimit(1)))
                .where('D', casingPredicate
                        .or(autoAbilities(false, false, true, false, true, false, false)))
                .where('E', casingPredicate
                        .or(autoAbilities(true, false, false, false, false, false, false)))
                .where('F', casingPredicate
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

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID);
    }
}
