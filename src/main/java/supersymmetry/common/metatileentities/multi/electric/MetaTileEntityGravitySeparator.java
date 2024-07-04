package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockSeparatorRotor;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

import static gregtech.api.util.RelativeDirection.*;
import static supersymmetry.api.blocks.VariantHorizontalRotatableBlock.FACING;

public class MetaTileEntityGravitySeparator extends RecipeMapMultiblockController {
    public MetaTileEntityGravitySeparator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.GRAVITY_SEPARATOR_RECIPES);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityGravitySeparator(this.metaTileEntityId);
    }

    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, UP, FRONT)
                //front of R facing right side
                .aisle("C   C", "CC CC", "CFCFC", "CCSCC", " CCC ", " CCC ", "     ")
                .aisle("     ", " OOO ", "C###C", "RRRRR", "C###C", "C   C", "     ")
                .aisle("C   C", "C   C", "MCCCM", "C###C", "RRRRR", "C   C", "     ")
                .aisle("C   C", "C   C", "ECCCE", "RRRRR", "C###C", "C###C", "     ")
                .aisle("     ", "     ", "C   C", "CCCCC", "RRRRR", "C###C", "C###C")
                .aisle("     ", "C   C", "C   C", " CCC ", "C###C", "RRRRR", "J###J")
                .aisle("C   C", "CC CC", "CCCCC", " CCC ", " CCC ", "CCCCC", "CIIIC")
                 /* Other orientation
                .aisle("C CC  C", "C CC CC", "CCCECCC", "CRCRC  ", " CRCRC ", " CCCCRC", "    CJC")
                .aisle("       ", "CO    C", "F#CC  C", "CR#RCCC", "C#R#R#C", "C  ##RC", "    ##I")
                .aisle("       ", " O    C", "C#CC  C", "CR#RCCC", "C#R#R#C", "C  ##RC", "    ##I")
                .aisle("       ", "CO    C", "F#CC  C", "CR#RCCC", "C#R#R#C", "C  ##RC", "    ##I")
                .aisle("C CC  C", "C CC CC", "CCCSCCC", "CRCRC  ", " CRCRC ", " CCCCRC", "    CJC")
                */
                .where('S', selfPredicate())
                .where('R', rotorOrientation())
                .where('C', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID)))
                .where('M', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH)).setExactLimit(1))
                .where('E', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities(true, false, false, false, false, false, false)))
                .where('I', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities(false, false, true, false, false, false, false)))
                .where('O', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities(false, false, false, true, false, false, false)))
                .where('J', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities(false, false, false, false, true, false, false)))
                .where('F', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities(false, false, false, false, false, true, false)))
                .where('#', air())
                .where(' ', any())
                .build();
    }

    /* can be reimplemented with states for R if rotation is not supposed to be specified
    public IBlockState[] getRotorStates() {
        return new IBlockState[] {
                SuSyBlocks.SEPARATOR_ROTOR.getState(BlockSeparatorRotor.BlockSeparatorRotorType.STEEL).withProperty(FACING, EnumFacing.SOUTH),
                SuSyBlocks.SEPARATOR_ROTOR.getState(BlockSeparatorRotor.BlockSeparatorRotorType.STEEL).withProperty(FACING, EnumFacing.NORTH),
                SuSyBlocks.SEPARATOR_ROTOR.getState(BlockSeparatorRotor.BlockSeparatorRotorType.STEEL).withProperty(FACING, EnumFacing.EAST),
                SuSyBlocks.SEPARATOR_ROTOR.getState(BlockSeparatorRotor.BlockSeparatorRotorType.STEEL).withProperty(FACING, EnumFacing.WEST)
        };
    }
     */

    //makes sure block at position is properly oriented rotor
    protected TraceabilityPredicate rotorOrientation() {
        //makes sure rotor's front faces the left side (relative to the player) of controller front
        EnumFacing leftFacing = RelativeDirection.RIGHT.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped());

        // converting the left facing to positive x or z axis direction
        // this is needed for the following update which converts this rotatable block from horizontal directional into axial directional.
        EnumFacing axialFacing = leftFacing.getIndex() < 4 ? EnumFacing.SOUTH : EnumFacing.WEST;

        Supplier<BlockInfo[]> supplier = () -> new BlockInfo[]{new BlockInfo(steelRotorState().withProperty(FACING, axialFacing))};
        return new TraceabilityPredicate(blockWorldState -> {
            IBlockState state = blockWorldState.getBlockState();
            if (!(state.getBlock() instanceof BlockSeparatorRotor)) return false;

            // auto-correct rotor orientation
            if (state != steelRotorState().withProperty(FACING, axialFacing)) {
                getWorld().setBlockState(blockWorldState.getPos(), steelRotorState().withProperty(FACING, axialFacing));
            }
            return true;
        }, supplier);
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    protected IBlockState steelRotorState() {
        return SuSyBlocks.SEPARATOR_ROTOR.getState(BlockSeparatorRotor.BlockSeparatorRotorType.STEEL);
    }

    @Nonnull
    protected ICubeRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }
}
