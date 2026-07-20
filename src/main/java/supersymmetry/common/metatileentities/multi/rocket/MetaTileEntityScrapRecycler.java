package supersymmetry.common.metatileentities.multi.rocket;

import gregtech.api.GTValues;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.RelativeDirection;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.Pair;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockConveyor;
import supersymmetry.common.blocks.BlockRobotArm;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.conveyorBelts;
import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.robotArms;

public class MetaTileEntityScrapRecycler extends RecipeMapMultiblockController {

    private final List<Pair<BlockPos, RelativeDirection>> conveyorBlocks = new ArrayList<>();
    private final List<Pair<BlockPos, RelativeDirection>> robotArmBlocks = new ArrayList<>();

    public MetaTileEntityScrapRecycler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.SCRAP_RECYCLER);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityScrapRecycler(this.metaTileEntityId);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" CCC ", "CCCCC", "CCCCC", "CCCCC", " CCC ")
                .aisle(" CCC ", "P^RVP", "PAAAP", "PAAAP", " CDC ")
                .aisle(" CCC ", "P^RVP", "PAAAP", "PAAAP", " CDC ")
                .aisle(" CCC ", "P^RVP", "PAAAP", "PAAAP", " CDC ")
                .aisle(" CCC ", "P^RVP", "PAAAP", "PAAAP", " CDC ")
                .aisle(" CCC ", "P^RVP", "PAAAP", "PAAAP", " CDC ")
                .aisle(" CCC ", "CISOC", "CCCCC", "PAAAP", " CCC ")
                .where(' ', any())
                .where('A', air())
                .where('S', this.selfPredicate())
                .where('P', states(getPipeCasingState()))
                .where('C', states(getCasingState()))
                .where('^', conveyorBelts(RelativeDirection.BACK))
                .where('V', conveyorBelts(RelativeDirection.FRONT))
                .where('R', robotArms(RelativeDirection.RIGHT))
                .where('I',
                        states(getCasingState()).or(this.autoAbilities(false, false, true, false, false, false, false)))
                .where('O',
                        states(getCasingState()).or(this.autoAbilities(false, false, false, true, false, false, false)))
                .where('D',
                        states(getCasingState()).or(this.autoAbilities(true, true, false, false, false, false, false)))
                .build();
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE);
    }

    protected static IBlockState getPipeCasingState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TITANIUM_PIPE);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.STABLE_TITANIUM_CASING;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        conveyorBlocks.addAll(context.getOrDefault("ConveyorBelt", new LinkedList<>()));
        robotArmBlocks.addAll(context.getOrDefault("RobotArm", new LinkedList<>()));
    }

    public void invalidateStructure() {
        super.invalidateStructure();
        this.conveyorBlocks.clear();
        this.robotArmBlocks.clear();
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder baseBuilder = MultiblockShapeInfo.builder()
                .where('S', SuSyMetaTileEntities.SCRAP_RECYCLER, EnumFacing.SOUTH)
                .where('C', getCasingState())
                .where('P', getPipeCasingState())
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.EV], EnumFacing.SOUTH)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.EV], EnumFacing.SOUTH)
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.IV], EnumFacing.UP)
                .where('^',
                        SuSyBlocks.CONVEYOR_BELT.getDefaultState().withProperty(BlockConveyor.FACING, EnumFacing.NORTH))
                .where('V',
                        SuSyBlocks.CONVEYOR_BELT.getDefaultState().withProperty(BlockConveyor.FACING, EnumFacing.SOUTH))
                .where('R',
                        SuSyBlocks.ROBOT_ARM.getState(BlockRobotArm.RobotArmType.GRABBER, EnumFacing.WEST))
                .where('M',
                        () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH :
                                getCasingState(),
                        EnumFacing.UP);
        shapeInfo.add(baseBuilder.shallowCopy()
                .aisle(" CCC ", "CCCCC", "CCCCC", "CCCCC", " CCC ")
                .aisle(" CCC ", "P^RVP", "PAAAP", "PAAAP", " CCC ")
                .aisle(" CCC ", "P^RVP", "PAAAP", "PAAAP", " CCC ")
                .aisle(" CCC ", "P^RVP", "PAAAP", "PAAAP", " CCC ")
                .aisle(" CCC ", "P^RVP", "PAAAP", "PAAAP", " CMC ")
                .aisle(" CCC ", "P^RVP", "PAAAP", "PAAAP", " CEC ")
                .aisle(" CCC ", "CISOC", "CCCCC", "PAAAP", " CCC ")
                .build());
        return shapeInfo;
    }

    protected void updateFormedValid() {
        super.updateFormedValid();

        World world = getWorld();
        for (Pair<BlockPos, RelativeDirection> posDirPair : conveyorBlocks) {
            // RelativeDirection will take into account of the multi flipping pattern
            EnumFacing conveyorFacing = posDirPair.getRight().getRelativeFacing(getFrontFacing(), getUpwardsFacing(),
                    isFlipped());

            BlockPos blockPos = posDirPair.getLeft();
            IBlockState blockState = world.getBlockState(blockPos);
            Block conveyor = blockState.getBlock();
            if (conveyor instanceof BlockConveyor && blockState.getValue(BlockConveyor.FACING) != conveyorFacing) {
                world.setBlockState(blockPos, blockState.withProperty(BlockConveyor.FACING, conveyorFacing));
            }
        }
        for (Pair<BlockPos, RelativeDirection> posDirPair : robotArmBlocks) {
            // RelativeDirection will take into account of the multi flipping pattern
            EnumFacing robotArmFacing = posDirPair.getRight().getRelativeFacing(getFrontFacing(), getUpwardsFacing(),
                    isFlipped());

            BlockPos blockPos = posDirPair.getLeft();
            IBlockState blockState = world.getBlockState(blockPos);
            Block robotArm = blockState.getBlock();
            if (robotArm instanceof BlockRobotArm && blockState.getValue(BlockRobotArm.FACING) != robotArmFacing) {
                world.setBlockState(blockPos, blockState.withProperty(BlockRobotArm.FACING, robotArmFacing));
            }
        }
    }



}
