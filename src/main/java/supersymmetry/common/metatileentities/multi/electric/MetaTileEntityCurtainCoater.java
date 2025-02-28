package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.*;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.*;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockConveyor;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

import javax.annotation.Nonnull;
import java.util.*;

import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.conveyorBelts;

public class MetaTileEntityCurtainCoater extends RecipeMapMultiblockController {

    private final List<Pair<BlockPos, RelativeDirection>> conveyorBlocks = new ArrayList<>();

    public MetaTileEntityCurtainCoater(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.CURTAIN_COATER);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityCurtainCoater(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("  X  ", "  X  ", "  X  ")
                .aisle("IXXXO", "BBBBB", "  X  ")
                .aisle("  S  ", "  F  ", "  G  ")
                .where('S', selfPredicate())
                .where('I', abilities(MultiblockAbility.IMPORT_ITEMS))
                .where('O', abilities(MultiblockAbility.EXPORT_ITEMS))
                .where('X', states(getCasingState()).setMinGlobalLimited(4)
                        .or(autoAbilities(true, true, false, false, false, false, false)))
                .where('G', states(getGearBoxState()))
                .where('F', abilities(MultiblockAbility.IMPORT_FLUIDS))
                .where('B', conveyorBelts(RelativeDirection.LEFT))
                .where(' ', any())
                .build();
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder baseBuilder = MultiblockShapeInfo.builder()
                .where('S', SuSyMetaTileEntities.CURTAIN_COATER, EnumFacing.SOUTH)
                .where('X', getCasingState())
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.LV], EnumFacing.SOUTH)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.LV], EnumFacing.SOUTH)
                .where('F', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.LV], EnumFacing.SOUTH)
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.LV], EnumFacing.NORTH)
                .where('G', getGearBoxState())
                .where('>', SuSyBlocks.CONVEYOR_BELT.getDefaultState().withProperty(BlockConveyor.FACING, EnumFacing.EAST))
                .where('<', SuSyBlocks.CONVEYOR_BELT.getDefaultState().withProperty(BlockConveyor.FACING, EnumFacing.WEST))
                .where('M',
                        () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH : getCasingState(), EnumFacing.SOUTH);
        shapeInfo.add(baseBuilder.shallowCopy()
                .aisle("  E  ", "  X  ", "  X  ")
                .aisle("IMXXO", ">>>>>", "  X  ")
                .aisle("  S  ", "  F  ", "  G  ")
                .build());
        shapeInfo.add(baseBuilder.shallowCopy()
                .aisle("  E  ", "  X  ", "  X  ")
                .aisle("OMXXI", "<<<<<", "  X  ")
                .aisle("  S  ", "  F  ", "  G  ")
                .build());
        return shapeInfo;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);

        conveyorBlocks.addAll(context.getOrDefault("ConveyorBelt", new LinkedList<>()));
    }

    public void invalidateStructure() {
        super.invalidateStructure();
        this.conveyorBlocks.clear();
    }

    protected void updateFormedValid() {
        super.updateFormedValid();

        World world = getWorld();
        for (Pair<BlockPos, RelativeDirection> posDirPair : conveyorBlocks) {
            // RelativeDirection will take into account of the multi flipping pattern
            EnumFacing conveyorFacing = posDirPair.getRight().getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped());

            BlockPos blockPos = posDirPair.getLeft();
            IBlockState blockState = world.getBlockState(blockPos);
            Block conveyor = blockState.getBlock();
            if (conveyor instanceof BlockConveyor && blockState.getValue(BlockConveyor.FACING) != conveyorFacing) {
                world.setBlockState(blockPos, blockState.withProperty(BlockConveyor.FACING, conveyorFacing));
            }
        }
    }


    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    protected IBlockState getGearBoxState() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STAINLESS_STEEL_GEARBOX);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    public boolean allowsFlip() {
        return true;
    }
}
