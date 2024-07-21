package supersymmetry.common.metatileentities.multi.electric;

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
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityCoolingUnit extends RecipeMapMultiblockController {

    public MetaTileEntityCoolingUnit(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.COOLING_UNIT_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityCoolingUnit(metaTileEntityId);
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        // Different characters use common constraints. Copied from GCyM
        TraceabilityPredicate casingPredicate = states(getCasingState()).setMinGlobalLimited(270);

        return FactoryBlockPattern.start()
                .aisle("AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "AADDDAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "       ", "  EEE  ")
                .aisle("AAAAAAA", "A#####A", "A#####A", "A#####A", "A#####A", "A#####A", "A#####A", "A#####A", "AAAAAAA", " EEEEE ", " EEEEE ")
                .aisle("AAAAAAA", "A##B##A", "A##B##A", "A##B##A", "D##B##D", "A##B##A", "A##B##A", "A##B##A", "AAAAAAA", " E   E ", "EE C EE")
                .aisle("AAAAAAA", "A#BDB#A", "A#BDB#A", "A#BDB#A", "D#BDB#D", "A#BDB#A", "A#BDB#A", "A#BDB#A", "AAAAAAA", " E C E ", "EECCCEE")
                .aisle("AAAAAAA", "A##B##A", "A##B##A", "A##B##A", "D##B##D", "A##B##A", "A##B##A", "A##B##A", "AAAAAAA", " E   E ", "EE C EE")
                .aisle("AAAAAAA", "A#####A", "A#####A", "A#####A", "A#####A", "A#####A", "A#####A", "A#####A", "AAAAAAA", " EEEEE ", " EEEEE ")
                .aisle("AAASAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "AADDDAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "AAAAAAA", "       ", "  EEE  ")
                .where('S', selfPredicate())
                .where('A', casingPredicate
                        .or(autoAbilities(true, true, true, true, true, true, false)))
                .where('B', states(MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel)))
                .where('C', states(MetaBlocks.FRAMES.get(Materials.StainlessSteel).getBlock(Materials.StainlessSteel)))
                .where('D', states(MetaBlocks.BOILER_CASING.getState(BoilerCasingType.STEEL_PIPE)))
                .where('E', casingPredicate)
                .where(' ', any())
                .where('#', air())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.COOLING_UNIT_OVERLAY;
    }
}
