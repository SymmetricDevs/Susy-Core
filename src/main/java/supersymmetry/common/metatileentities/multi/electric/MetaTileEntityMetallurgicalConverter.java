package supersymmetry.common.metatileentities.multi.electric;

import gregicality.multiblocks.common.block.GCYMMetaBlocks;
import gregicality.multiblocks.common.block.blocks.BlockLargeMultiblockCasing;
import gregicality.multiblocks.common.block.blocks.BlockUniqueCasing;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
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
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntityMetallurgicalConverter extends RecipeMapMultiblockController {

    public MetaTileEntityMetallurgicalConverter(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.METALLURIGICAL_CONVERTER);
        this.recipeMapWorkable = new MetallurgicalConverterLogic(this);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RelativeDirection.BACK, RelativeDirection.UP, RelativeDirection.RIGHT)
                .aisle(" F   F ", " F   F ", " F   F ", " F   F ", " FF FF ", "  FAF  ", "  AGA  ", "   F   ", "       ", "       ")
                .aisle("HHHHHH ", "HFFFFF ", "H ###  ", "  ###  ", "  BBB  ", "  VVV  ", "  BBB  ", "  VVV  ", "  BBB  ", "       ")
                .aisle(" HHHHHH", "HF###H ", " ##### ", " #BBB# ", " BRRRB ", " VRRRV ", " BRRRB ", " VRRRV ", " BRRRB ", "  BBB  ")
                .aisle("  HHHHH", "  #P#FS", " ##P## ", " #BBB# ", " BR#RB ", " VR#RV ", " BR#RB ", " VR#RV ", " BR#RB ", "  BMB  ")
                .aisle(" HHHHHH", "HF###H ", " ##### ", " #BBB# ", " BRRRB ", " VRRRV ", " BRRRB ", " VRRRV ", " BRRRB ", "  BBB  ")
                .aisle("HHHHHH ", "HFFFFF ", "H ###  ", "  ###  ", "  BBB  ", "  VVV  ", "  BBB  ", "  VVV  ", "  BBB  ", "       ")
                .aisle(" F   F ", " F   F ", " F   F ", " F   F ", " FF FF ", "  FAF  ", "  AGA  ", "   F   ", "       ", "       ")
                .where('#', air())
                .where('S', selfPredicate())
                .where('R', states(getRefractoryState()))
                .where('H', states(getCasingState()).setMinGlobalLimited(31).or(autoAbilities(true, true, true, true, true, true, false)))
                .where('A', states(getCasingState()))
                .where('B', states(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING.getState(BlockLargeMultiblockCasing.CasingType.STRESS_PROOF_CASING)))
                .where('F', frames(Materials.Steel))
                .where('V', states(GCYMMetaBlocks.UNIQUE_CASING.getState(BlockUniqueCasing.UniqueCasingType.HEAT_VENT)))
                .where('G', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('P', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
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
        return SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.ADVANCED_REFRACTORY_LINING);
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
