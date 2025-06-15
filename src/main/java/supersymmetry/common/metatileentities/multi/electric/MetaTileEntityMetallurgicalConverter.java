package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockRocketAssemblerCasing;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntityMetallurgicalConverter extends RecipeMapMultiblockController {

    public MetaTileEntityMetallurgicalConverter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.METALLURIGICAL_CONVERTER);
        this.recipeMapWorkable = new MetallurgicalConverterLogic(this);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("  GGG       ", "  GGG       ", "  GGG       ", "  GGG       ", "            ", "            ", "            ", "            ", "            ", "            ", "            ", "            ")
                .aisle(" GGCGG      ", " GGCGG      ", " GGCGG      ", " GGBGG      ", "            ", "            ", "            ", "            ", "            ", "            ", "            ", "            ")
                .aisle("  GGG       ", "  GGG       ", "  GGG       ", "  GHG       ", "            ", "            ", "            ", "            ", "            ", "            ", "            ", "            ")
                .aisle("            ", "            ", "            ", "   H        ", "            ", "            ", "            ", "            ", "            ", "            ", "            ", "            ")
                .aisle("            ", "            ", "   C        ", " CCCCC      ", " CCCCC      ", "  CCC       ", "  CCC       ", "            ", "            ", "            ", "            ", "            ")
                .aisle("            ", "  CCC       ", " CCCCC      ", "CRRRRRC     ", "CRRRRRC     ", " CRRRC      ", " CRRRC      ", "  CCC       ", "            ", "            ", "            ", "            ")
                .aisle("         RRR", " CCCCC   RFR", " CRRRC   RRR", "CR###RC     ", "CR###RC     ", "CRR#RRC     ", "CRR#RRC     ", " CRRRC      ", "            ", "            ", "  AAA       ", "  AAA       ")
                .aisle("         RRR", " CCCCC   F#F", "CCRRRCC  R#R", "CR###RC####L", "CR###RC####L", "CR###RC####L", "CR###RC####L", " CRPRC     L", "   P       L", "   P LLLLLLL", "  APA       ", "  AAA       ")
                .aisle("         RRR", " CCCCC   RFR", " CRRRC   RRR", "CR###RC     ", "CR###RC     ", "CRR#RRC     ", "CRR#RRC     ", " CRRRC      ", "            ", "     L      ", "  ASA       ", "  AAA       ")
                .aisle("            ", "  CCC       ", " CCCCC      ", "CRRRRRC     ", "CRRRRRC     ", " CRRRC      ", " CRRRC      ", "  CCC       ", "            ", "   LLL      ", "            ", "            ")
                .aisle("            ", "            ", "   C        ", " CCCCC      ", " CCCCC      ", "  CCC       ", "  CCC       ", "            ", "            ", "            ", "            ", "            ")
                .aisle("            ", "            ", "            ", "   H        ", "            ", "            ", "            ", "            ", "            ", "            ", "            ", "            ")
                .aisle("  GGG       ", "  GGG       ", "  GGG       ", "  GHG       ", "            ", "            ", "            ", "            ", "            ", "            ", "            ", "            ")
                .aisle(" GGCGG      ", " GGCGG      ", " GGCGG      ", " GGBGG      ", "            ", "            ", "            ", "            ", "            ", "            ", "            ", "            ")
                .aisle("  GGG       ", "  GGG       ", "  GGG       ", "  GGG       ", "            ", "            ", "            ", "            ", "            ", "            ", "            ", "            ")
                .where('#', air())
                .where('S', selfPredicate())
                .where('R', states(getRefractoryState()))
                .where('A', states(getCasingState()).or(autoAbilities(true, true, true, false, true, false, false)))
                .where('F', states(getRefractoryState()).or(autoAbilities(false, false, false, true, false, true, false)))
                .where('H', states(MetaBlocks.COMPRESSED.get(Materials.Steel).getBlock(Materials.Steel)))
                .where('G', states(SuSyBlocks.ROCKET_ASSEMBLER_CASING.getState(BlockRocketAssemblerCasing.RocketAssemblerCasingType.REINFORCED_FOUNDATION)))
                .where('C', states(getCasingState()))
                .where('P', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('B', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('L', frames(Materials.Steel))
                .where(' ', any())
                .build();
    }

    protected EnumFacing getRelativeFacing(RelativeDirection dir) {
        return dir.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped());
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    private IBlockState getRefractoryState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.PRIMITIVE_BRICKS);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMetallurgicalConverter(metaTileEntityId);
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.METALLURGICAL_CONVERTER_OVERLAY;
    }

    private class MetallurgicalConverterLogic extends MultiblockRecipeLogic {
        public MetallurgicalConverterLogic(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        public int getParallelLimit() {
            return 256;
        }

        @Override
        protected long getMaxParallelVoltage() {
            return 2147432767L;
        }

        @Override
        public boolean consumesEnergy() {
            return false;
        }

        @Override
        public @NotNull ParallelLogicType getParallelLogicType() {
            return ParallelLogicType.APPEND_ITEMS;
        }
    }
}
