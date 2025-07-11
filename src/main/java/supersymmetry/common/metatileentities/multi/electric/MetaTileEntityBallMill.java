package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockFireboxCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityBallMill extends RecipeMapMultiblockController {

    public MetaTileEntityBallMill(ResourceLocation metaTileEntityId, RecipeMap<?> recipeMap) {
        super(metaTileEntityId, recipeMap);
    }

    private static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    // Placeholder
    private static IBlockState getGearState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.POLYTETRAFLUOROETHYLENE_PIPE);
    }

    // Placeholder
    private static IBlockState getShellCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    // Placeholder
    // How should I call this???
    private static IBlockState getShellLineState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE);
    }

    // Placeholder
    // How should I call this???
    private static IBlockState getShellEndState() {
        return MetaBlocks.BOILER_FIREBOX_CASING.getState(BlockFireboxCasing.FireboxCasingType.STEEL_FIREBOX);
    }

    // Placeholder
    private static IBlockState getEngineCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PTFE_INERT_CASING);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityBallMill(metaTileEntityId, recipeMap);
    }

    @SideOnly(Side.CLIENT)
    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.BALL_MILL_OVERLAY;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXXXXXXXXXXX", "            ", "            ", " G          ", " G          ", " G          ", "            ", "            ")
                .aisle("X          X", "            ", " G          ", " LHHLHHLHHL ", " LHHLHHLHHL ", " LHHLHHLHHL ", " G          ", "            ")
                .aisle("X          X", "PG         X", "PLHHLHHLHHLX", "PT########TX", "PT########TX", "PT########T ", " LHHLHHLHHL ", " G          ")
                .aisle("X          X", " G          ", " LHHLHHLHHL ", "PT########T ", "I##########I", "PT########T ", " LHHLHHLHHL ", " G          ")
                .aisle("X          X", "PG         X", "PLHHLHHLHHLX", "PT########TX", "PT########TX", "PT########T ", " LHHLHHLHHL ", " G          ")
                .aisle("X          X", "            ", " G          ", " LHHLHHLHHL ", " LHHLHHLHHL ", " LHHLHHLHHL ", " G          ", "            ")
                .aisle("XXMMMXXXXXXX", "  MSM       ", "            ", " G          ", " G          ", " G          ", "            ", "            ")
                .where('M', states(getCasingState()).or(autoAbilities(
                        true, true, false,
                        false, false, false, false
                )))
                .where('I', states(getCasingState()).or(autoAbilities(
                        false, false, true,
                        true, false, false, false
                )))
                .where('T', states(getShellEndState()))
                .where('H', states(getShellCasingState()))
                .where('L', states(getShellLineState()))
                .where('P', states(getEngineCasingState()))
                .where('G', states(getGearState()))
                .where('X', frames(Materials.Steel))
                .where('S', selfPredicate())
                .where('#', air())
                .where(' ', any())
                .build();
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }
}
