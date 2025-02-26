package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.*;
import gregtech.api.util.BlockInfo;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.*;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockConveyor;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

import javax.annotation.Nonnull;
import java.util.*;

public class MetaTileEntityCurtainCoater extends RecipeMapMultiblockController {

    public static TraceabilityPredicate conveyors(BlockConveyor.ConveyorType... variants) {

        return new TraceabilityPredicate(blockWorldState -> {
            IBlockState state = blockWorldState.getBlockState();
            if (state.getBlock() instanceof BlockConveyor) {
                blockWorldState.getMatchContext().getOrPut("Conveyors", new LinkedList<>()).add(blockWorldState.getPos());
                BlockConveyor.ConveyorType property = ((BlockConveyor) state.getBlock()).getState(state);
                return ArrayUtils.contains(variants, property);
            }
            return false;
        }, () -> Arrays.stream(variants).map(m -> new BlockInfo(SuSyBlocks.CONVEYOR_BELT.getState(m), null)).toArray(BlockInfo[]::new));
    }


    public MetaTileEntityCurtainCoater(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.CURTAIN_COATER);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityCurtainCoater(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        TraceabilityPredicate inputBus = abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setMaxGlobalLimited(1);
        TraceabilityPredicate outputBus = abilities(MultiblockAbility.EXPORT_ITEMS).setMinGlobalLimited(1).setMaxGlobalLimited(1);
        return FactoryBlockPattern.start()
                .aisle("  X  ", "  X  ", "  X  ")
                .aisle("IXXXI", "BBBBB", "  X  ")
                .aisle("  S  ", "  F  ", "  G  ")
                .where('S', selfPredicate())
                .where('I', inputBus.or(outputBus))
                .where('X', states(getCasingState()).setMinGlobalLimited(4)
                        .or(autoAbilities(true, true, false, false, false, false, false)))
                .where('G', states(getGearBoxState()))
                .where('F', abilities(MultiblockAbility.IMPORT_FLUIDS))
                .where('B', conveyors(BlockConveyor.ConveyorType.LV_CONVEYOR))
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
                .where('>', getConveyorState(BlockConveyor.ConveyorType.LV_CONVEYOR, EnumFacing.EAST))
                .where('<', getConveyorState(BlockConveyor.ConveyorType.LV_CONVEYOR, EnumFacing.WEST))
                .where('M',
                        () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH :
                                getCasingState(), EnumFacing.SOUTH);
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
        Set<IMultiblockPart> rawPartsSet = context.getOrCreate("MultiblockParts", HashSet::new);
        ArrayList<IMultiblockPart> parts = new ArrayList<>(rawPartsSet);

        BlockPos inputBusPos = null, outputBusPos = null;
        for (IMultiblockPart part : parts) {
            // these entities should be IMultiblockAbilityPart, and they should be a MTE
            if (part instanceof IMultiblockAbilityPart<?> && part instanceof MetaTileEntityMultiblockPart) {
                MultiblockAbility<?> ability = ((IMultiblockAbilityPart<?>) part).getAbility();
                if (ability == MultiblockAbility.IMPORT_ITEMS)
                    inputBusPos = ((MetaTileEntityMultiblockPart) part).getPos();
                if (ability == MultiblockAbility.EXPORT_ITEMS)
                    outputBusPos = ((MetaTileEntityMultiblockPart) part).getPos();
            }
        }

        // Set a default facing in case some weirdness happened so the direction cannot be determined
        EnumFacing conveyorFacing = this.getFrontFacing().rotateYCCW();
        if (inputBusPos != null && outputBusPos != null) {
            Vec3i direction = outputBusPos.subtract(inputBusPos);
            conveyorFacing = EnumFacing.getFacingFromVector(direction.getX(), direction.getY(), direction.getZ());
        }

        List<BlockPos> conveyorBlocks = context.getOrDefault("Conveyors", new LinkedList<>());
        if (conveyorBlocks != null && !conveyorBlocks.isEmpty()) {
            World world = getWorld();
            for (BlockPos blockPos : conveyorBlocks) {
                Block conveyor = world.getBlockState(blockPos).getBlock();
                if (conveyor instanceof BlockConveyor) {
                    ((BlockConveyor) conveyor).setFacing(world, blockPos, conveyorFacing);
                }
            }
        }
    }

    protected IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    protected IBlockState getGearBoxState() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STAINLESS_STEEL_GEARBOX);
    }

    protected IBlockState getConveyorState(BlockConveyor.ConveyorType type, EnumFacing facing) {
        return SuSyBlocks.CONVEYOR_BELT.getState(type, facing);
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
        return false;
    }
}
