package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockElectrodeAssembly;
import supersymmetry.common.blocks.BlockMetallurgy;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;

import java.util.function.Supplier;

public class MetaTileEntityGasAtomizer extends RecipeMapMultiblockController {

    public MetaTileEntityGasAtomizer(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.GAS_ATOMIZER);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("  O  ", "  P  ", "  P  ", "  M  ", "     ", "     ", " EIE ", "     ")
                .aisle("     ", "  P  ", "     ", "     ", "     ", " HHH ", " EXE ", " HHH ")
                .aisle("R   R", "R P R", "CCCCC", " HHH ", " HHH ", " HHH ", " HXH ", " HHH ")
                .aisle(" CCC ", " CPC ", "CHHHC", "HHHHH", "HXXXH", "HXXXH", "HXXXH", " HHH ")
                .aisle(" CCC ", " CXC ", "CHXHC", "HHXHH", "HXXXH", "HXXXH", "HXXXH", " HFH ")
                .aisle(" CCC ", " CCC ", "CHHHC", "HHHHH", "HXXXH", "HXXXH", "HXXXH", " HHH ")
                .aisle("R   R", "R   R", "CCSCC", " HHH ", " HHH ", " HHH ", " HHH ", "     ")
                .where('P', states(getPipeCasingState()))
                .where('H', states(getHighTempCasingState()))
                .where('C', states(getCasingState()).or(autoAbilities(true, true, false, false, false, false, false)))
                .where('S', selfPredicate())
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where('O', abilities(MultiblockAbility.EXPORT_ITEMS))
                .where('F', abilities(MultiblockAbility.IMPORT_FLUIDS))
                .where('I', abilities(MultiblockAbility.IMPORT_ITEMS))
                .where('E', states(getElectrodeCasingState()))
                .where('R', frames(Materials.Steel))
                .where('X', air())
                .where(' ', any())
                .build();
    }

    public IBlockState getElectrodeCasingState() {
        return SuSyBlocks.ELECTRODE_ASSEMBLY.getState(BlockElectrodeAssembly.ElectrodeAssemblyType.CARBON);
    }

    protected TraceabilityPredicate orientation(IBlockState state, RelativeDirection direction, IProperty<EnumFacing> facingProperty) {
        EnumFacing facing = this.getRelativeFacing(direction);

        Supplier<BlockInfo[]> supplier = () -> new BlockInfo[]{new BlockInfo(state.withProperty(facingProperty, facing))};
        return new TraceabilityPredicate(blockWorldState -> {
            if (blockWorldState.getBlockState() != state.withProperty(facingProperty, facing)) {
                if (blockWorldState.getBlockState().getBlock() != state.getBlock()) return false;
                getWorld().setBlockState(blockWorldState.getPos(), state.withProperty(facingProperty, facing));
            }
            return true;
        }, supplier);
    }

    protected EnumFacing getRelativeFacing(RelativeDirection dir) {
        return dir.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped());
    }


    public IBlockState getPipeCasingState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE);
    }

    public IBlockState getHighTempCasingState() {
        return SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.SILICON_CARBIDE_CASING);
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityGasAtomizer(metaTileEntityId);
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.GAS_ATOMIZER_OVERLAY;
    }
}
