package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityMetallurgicalConverter extends RecipeMapMultiblockController {

    public MetaTileEntityMetallurgicalConverter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.METALLURIGICAL_CONVERTER);
        this.recipeMapWorkable = new MetallurgicalConverterLogic(this);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("  GGG       ", "  GGG       ", "  GGG       ", "  GGG       ", "            ", "            ", "            ", "            ", "            ", "            ", "            ", "            ")
                .aisle("  GCG       ", "  GCG       ", "  GCG       ", "  GCG       ", "            ", "            ", "            ", "            ", "            ", "            ", "            ", "            ")
                .aisle("  GGG       ", "  GGG       ", "  GGG       ", "  GCG       ", "            ", "            ", "            ", "            ", "            ", "            ", "            ", "            ")
                .aisle("            ", "            ", "            ", "   C        ", "            ", "            ", "            ", "            ", "            ", "            ", "            ", "            ")
                .aisle("            ", "            ", "   C        ", " CCCCC      ", " CCCCC      ", "  CCC       ", "  CCC       ", "            ", "            ", "            ", "            ", "            ")
                .aisle("            ", "  CCC       ", " CCCCC      ", "CRRRRRC     ", "CRRRRRC     ", " CRRRC      ", " CRRRC      ", "  CCC       ", "            ", "            ", "            ", "            ")
                .aisle("         RRR", " CCCCC   RFR", " CRRRC   RRR", "CR###RC     ", "CR###RC     ", "CRR#RRC     ", "CRR#RRC     ", " CRRRC      ", "            ", "            ", "  AAA       ", "  AAA       ")
                .aisle("         RRR", " CCCCC   F#F", "CCRRRCC  R#R", "CR###RC     ", "CR###RC     ", "CR###RC     ", "CR###RC     ", " CRPRC      ", "   P        ", "   P        ", "  APA       ", "  AAA       ")
                .aisle("         RRR", " CCCCC   RFR", " CRRRC   RRR", "CR###RC     ", "CR###RC     ", "CRR#RRC     ", "CRR#RRC     ", " CRRRC      ", "            ", "            ", "  ASA       ", "  AAA       ")
                .aisle("            ", "  CCC       ", " CCCCC      ", "CRRRRRC     ", "CRRRRRC     ", " CRRRC      ", " CRRRC      ", "  CCC       ", "            ", "            ", "            ", "            ")
                .aisle("            ", "            ", "   C        ", " CCCCC      ", " CCCCC      ", "  CCC       ", "  CCC       ", "            ", "            ", "            ", "            ", "            ")
                .aisle("            ", "            ", "            ", "   C        ", "            ", "            ", "            ", "            ", "            ", "            ", "            ", "            ")
                .aisle("  GGG       ", "  GGG       ", "  GGG       ", "  GCG       ", "            ", "            ", "            ", "            ", "            ", "            ", "            ", "            ")
                .aisle("  GCG       ", "  GCG       ", "  GCG       ", "  GCG       ", "            ", "            ", "            ", "            ", "            ", "            ", "            ", "            ")
                .aisle("  GGG       ", "  GGG       ", "  GGG       ", "  GGG       ", "            ", "            ", "            ", "            ", "            ", "            ", "            ", "            ")
                .where('#', air())
                .where('R', SuSyPredicates.sinteringBricks())
                .where('A', states(getCasingState()).or(autoAbilities(true, true, true, false, true, false, false)))
                .where('F', SuSyPredicates.sinteringBricks().or(autoAbilities(false, false, false, true, false, true, false)))
                .where('S', selfPredicate())
                .where('G', states(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT)))
                .where('C', states(getCasingState()))
                .where('P', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where(' ', any())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
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
