package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.common.metatileentities.multi.electric.MetaTileEntityCleanroom;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.capability.impl.EvapRecipeLogic;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.coilsOrBeds;

public class MetaTileEntityEvaporationPool extends RecipeMapMultiblockController {

    /// The max/min size of the evaporation pool in blocks.
    /// Measured by the outermost side length.
    private static final int MAX_DIAMETER = 32;
    private static final int MIN_DIAMETER = 7;

    /// Dimensions of the evaporation pool in blocks. Serialized.
    private int lDist = 0;
    private int rDist = 0;
    private int bDist = 0;

    /// The base temperature of the coils in the structure
    /// Does not need to be serialized since it's set during [#formStructure(PatternMatchContext context)]
    int coilTemp = 0;

    /// Concurrently updating the exposed block count, reducing the load on the main thread
    private static final AtomicInteger THREAD_CNTR = new AtomicInteger(0);
    private static final ScheduledExecutorService SCHEDULED_EXECUTOR = new ScheduledThreadPoolExecutor(
            Math.min(Math.max(Runtime.getRuntime().availableProcessors() / 4, 4), 8), // TODO: how many is enough?
            r -> new Thread(r, "Evaporation Pool Exposure Counter Thread-" + THREAD_CNTR.incrementAndGet()));
    private static final int TASK_DELAY = 1000; /// 1000 milliseconds' delay between each check

    @Nullable
    private ScheduledFuture<?> counterTask; /// Stored here to cancel the task when the structure is invalidated
    private int exposedBlocks = 0; /// Does not need to be serialized since it's updated (hopefully) once every second


    public MetaTileEntityEvaporationPool(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.EVAPORATION_POOL);
        this.recipeMapWorkable = new EvapRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityEvaporationPool(this.metaTileEntityId);
    }

    @Override
    public void checkStructurePattern() {
        if (!this.isStructureFormed()) {
            reinitializeStructurePattern();
        }
        super.checkStructurePattern();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        Object type = context.get("CoilType");
        if (type instanceof IHeatingCoilBlockStats coilStats) {
            this.coilTemp = coilStats.getCoilTemperature();
        }
        this.counterTask = SCHEDULED_EXECUTOR.scheduleWithFixedDelay(new ExposureCountTask(this),
                TASK_DELAY, TASK_DELAY, TimeUnit.MILLISECONDS);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.coilTemp = 0;
        this.exposedBlocks = 0;
        if (this.counterTask != null) {
            this.counterTask.cancel(true);
        }
    }

    /// @see MetaTileEntityCleanroom#updateStructureDimensions()
    @SuppressWarnings("UnusedReturnValue")
    public boolean updateStructureDimensions() {

        World world = getWorld();
        EnumFacing front = getFrontFacing();
        EnumFacing back = front.getOpposite();
        EnumFacing right = front.rotateYCCW(); // right as if you were looking at it, not controller's left
        EnumFacing left = right.getOpposite();

        BlockPos.MutableBlockPos lPos = new BlockPos.MutableBlockPos(getPos().offset(back, 2));
        BlockPos.MutableBlockPos rPos = new BlockPos.MutableBlockPos(getPos().offset(back, 2));
        BlockPos.MutableBlockPos bPos = new BlockPos.MutableBlockPos(getPos().offset(back, 2));

        int lDist = 0;
        int rDist = 0;
        int bDist = 0;

        for (int i = 1; i <= MAX_DIAMETER - 4; i++) {
            if (lDist == 0 && isBlockEdge(world, lPos, left)) lDist = i;
            if (rDist == 0 && isBlockEdge(world, rPos, right)) rDist = i;
            if (bDist == 0 && isBlockEdge(world, bPos, back)) bDist = i;
            if (lDist != 0 && rDist != 0 && bDist != 0) break;
        }

        int width = lDist + rDist + 3;
        int length = bDist + 4;
        if (lDist == 0 || rDist == 0 || length < MIN_DIAMETER || width > MAX_DIAMETER || width < MIN_DIAMETER) {
            invalidateStructure();
            return false;
        }

        this.lDist = lDist;
        this.rDist = rDist;
        this.bDist = bDist;

        writeCustomData(GregtechDataCodes.UPDATE_STRUCTURE_SIZE, buf -> {
            buf.writeInt(this.lDist);
            buf.writeInt(this.rDist);
            buf.writeInt(this.bDist);
        });

        return true;
    }

    public boolean isBlockEdge(@NotNull World world, @NotNull BlockPos.MutableBlockPos pos,
                               @NotNull EnumFacing direction) {
        return world.getBlockState(pos.move(direction)) == getConcreteState()
                || GTUtility.getMetaTileEntity(world, pos) instanceof IMultiblockPart;
    }

    protected static IBlockState getConcreteState() {
        return MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH)
                .getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT);
    }

    protected static IBlockState getEvaporationBedState() {
        return SuSyBlocks.EVAPORATION_BED.getDefaultState();
    }

    protected String line(Slice slice) {
        return this.isFlipped() ? slice.gen(rDist, lDist) : slice.gen(lDist, rDist);
    }

    /// 'S' = controller
    /// 'C' = concrete
    /// 'B' = evaporation bed
    /// 'H' = heating coil (or evaporation bed, for predicates)
    /// 'E' = energy hatch
    /// 'I' = fluid input hatch
    /// 'O' = fluid output hatch
    /// 'T' = item output bus
    /// '#' = air
    /// ' ' = any
    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        /// return the default structure, even if there is no valid size found
        /// this means auto-build will still work, and prevents terminal crashes.
        if (getWorld() != null) updateStructureDimensions();

        return FactoryBlockPattern.start()
                .aisle(line(Slice.B_ALL), line(Slice.T_NONE))
                .aisle(line(Slice.B_ALL), line(Slice.T_SIDES))
                .aisle(line(Slice.B_END), line(Slice.T_MIDDLE))
                .aisle(line(Slice.B_MIDDLE), line(Slice.T_MIDDLE)).setRepeatable(bDist - 2)
                .aisle(line(Slice.B_START), line(Slice.T_MIDDLE))
                .aisle(line(Slice.B_ALL), line(Slice.T_SIDES))
                .aisle(line(Slice.B_SELF), line(Slice.T_NONE))
                .where('S', selfPredicate())
                .where('C', states(getConcreteState()).or(autoAbilities()))
                .where('B', states(getEvaporationBedState()))
                .where('H', coilsOrBeds())
                .where('#', air())
                .where(' ', any())
                .build();
    }

    @SuppressWarnings("DataFlowIssue")
    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        int l = 3, r =3;
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle(Slice.B_P_HATCHES.gen(l, r), Slice.T_NONE.gen(l, r))
                .aisle(Slice.B_ALL.gen(l, r), Slice.T_SIDES.gen(l, r))
                .aisle(Slice.B_END.gen(l, r), Slice.T_MIDDLE.gen(l, r))
                .aisle(Slice.B_MIDDLE.gen(l, r), Slice.T_MIDDLE.gen(l, r))
                .aisle(Slice.B_MIDDLE.gen(l, r), Slice.T_MIDDLE.gen(l, r))
                .aisle(Slice.B_MIDDLE.gen(l, r), Slice.T_MIDDLE.gen(l, r))
                .aisle(Slice.B_START.gen(l, r), Slice.T_MIDDLE.gen(l, r))
                .aisle(Slice.B_ALL.gen(l, r), Slice.T_SIDES.gen(l, r))
                .aisle(Slice.B_SELF.gen(l, r), Slice.T_NONE.gen(l, r))
                .where('S', SuSyMetaTileEntities.EVAPORATION_POOL, EnumFacing.SOUTH)
                .where('C', getConcreteState())
                .where('B', getEvaporationBedState())
                .where('#', Blocks.AIR.getDefaultState())
                .where(' ', Blocks.AIR.getDefaultState())
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.LV], EnumFacing.NORTH)
                .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.LV], EnumFacing.NORTH)
                .where('O', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.LV], EnumFacing.NORTH)
                .where('T', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.LV], EnumFacing.NORTH);
        /// Supplying one with no coils
        shapeInfo.add(builder.where('H', getEvaporationBedState()).build());
        GregTechAPI.HEATING_COILS.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().getTier()))
                .forEach(entry -> shapeInfo.add(builder.where('H', entry.getKey()).build()));
        return shapeInfo;
    }

    public void updateExposedBlocks(int exposedBlocks) {
        this.exposedBlocks = exposedBlocks;
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        textList.add(new TextComponentString(String.format("lDist: %s; rDist: %s; bDist: %s", lDist, rDist, bDist)));
        textList.add(new TextComponentString(String.format("Exposed Blocks: %s", exposedBlocks)));
        textList.add(new TextComponentString(String.format("Coil Temp: %s", coilTemp)));
        textList.add(new TextComponentString(line(Slice.B_ALL) + " " + line(Slice.T_NONE)));
        textList.add(new TextComponentString(line(Slice.B_ALL) + " " + line(Slice.T_SIDES)));
        textList.add(new TextComponentString(line(Slice.B_END) + " " + line(Slice.T_MIDDLE)));
        for (int i = 0; i < bDist - 2; i++) {
            textList.add(new TextComponentString(line(Slice.B_MIDDLE) + " " + line(Slice.T_MIDDLE)));
        }
        textList.add(new TextComponentString(line(Slice.B_START) + " " + line(Slice.T_MIDDLE)));
        textList.add(new TextComponentString(line(Slice.B_ALL) + " " + line(Slice.T_SIDES)));
        textList.add(new TextComponentString(line(Slice.B_SELF) + " " + line(Slice.T_NONE)));
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("lDist", this.lDist);
        data.setInteger("rDist", this.rDist);
        data.setInteger("bDist", this.bDist);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.lDist = data.hasKey("lDist") ? data.getInteger("lDist") : this.lDist;
        this.rDist = data.hasKey("rDist") ? data.getInteger("rDist") : this.rDist;
        this.bDist = data.hasKey("bDist") ? data.getInteger("bDist") : this.bDist;
        reinitializeStructurePattern();
    }

    //order matters for these
    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.lDist);
        buf.writeInt(this.rDist);
        buf.writeInt(this.bDist);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.lDist = buf.readInt();
        this.rDist = buf.readInt();
        this.bDist = buf.readInt();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_STRUCTURE_SIZE) {
            this.lDist = buf.readInt();
            this.rDist = buf.readInt();
            this.bDist = buf.readInt();
        }
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    protected ICubeRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }

    @Override
    public boolean isMultiblockPartWeatherResistant(@Nonnull IMultiblockPart part) {
        return true;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    /// The core code for structure pattern generation.
    ///
    /// Let's use an evaporation pool with a diameter of 12 here as an example,
    /// assuming lDist = 3 and rDist = 6 (the length doesn't really matter here)
    ///
    /// The bottom layer:
    /// 0123456789AB
    /// B CCCCCCCCCCCC    B_ALL
    /// A CCCCCCCCCCCC    B_ALL
    /// 9 CCHBHHHBHHCC    B_END
    /// 8 CCHBHBHBHBCC    B_MIDDLE
    /// : (same as row 3)
    /// 3 CCHBHBHBHBCC    B_MIDDLE
    /// 2 CCHHHBHHHBCC    B_START
    /// 1 CCCCCCCCCCCC    B_ALL
    /// 0 CCCCSCCCCCCC    B_SELF. You can see there are lDist + 1 'C's on the left side of the controller, and same for the right side
    ///
    /// As you can see the heating coils are placed in a zigzag pattern, with the first one starting on the left side of the controller
    ///
    /// The top layer:
    /// 0123456789AB
    /// B                 T_NONE (empty)
    /// A  CCCCCCCCCC     T_SIDES
    /// 9  C########C     T_MIDDLE
    /// : (same as row 2)
    /// 2  C########C     T_MIDDLE
    /// 1  CCCCCCCCCC     T_SIDES
    /// 0                 T_NONE (empty)
    ///
    /// With this we can generate the structure pattern for the evaporation pool.
    ///
    /// @see #updateStructureDimensions()
    /// @see #createStructurePattern()
    protected enum Slice {
        /// B = bottom layer, T = top layer, P = preview only
        /// Yeah I'm def abusing the ternary operator here
        B_ALL((i, l, r) -> 'C'),
        B_SELF((i, l, r) -> i == l + 1 ? 'S' : 'C'),
        B_START((i, l, r) -> i < 2 || i > l + r ? 'C' : i % 4 == 1 ? 'B' : 'H'),
        B_MIDDLE((i, l, r) -> i < 2 || i > l + r ? 'C' : i % 2 == 1 ? 'B' : 'H'),
        B_END((i, l, r) -> i < 2 || i > l + r ? 'C' : i % 4 == 3 ? 'B' : 'H'),
        B_P_HATCHES((i, l, r) -> i == 0 ? 'E' : i == 1 ? 'I' : i == 2 ? 'O' : i == 3 ? 'T' : 'C'),
        T_NONE((i, l, r) -> ' '),
        T_SIDES((i, l, r) -> i == 0 || i == l + r + 2 ? ' ' : 'C'),
        T_MIDDLE((i, l, r) -> i == 0 || i == l + r + 2 ? ' ' : i == 1 || i == l + r + 1 ? 'C' : '#'),
        ;

        private final SliceConstructor cons;

        /// @param cons the constructor for the slice, using the index and the left/right distances as parameters
        Slice(SliceConstructor cons) {
            this.cons = cons;
        }

        public String gen(int lDist, int rDist) {
            int width = lDist + rDist + 3;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < width; i++) {
                builder.append(cons.at(i, lDist, rDist));
            }
            return builder.toString();
        }

        @FunctionalInterface
        private interface SliceConstructor {
            char at(int i, int lDist, int rDist);
        }
    }

    @SuppressWarnings("ClassCanBeRecord")
    public static class ExposureCountTask implements Runnable {

        public final MetaTileEntityEvaporationPool pool;

        public ExposureCountTask(MetaTileEntityEvaporationPool pool) {
            this.pool = pool;
        }

        @Override
        public void run() {
            if (!pool.isStructureFormed()) return;
            World world = pool.getWorld();
            int exposedBlocks = 0;
            for (BlockPos pos : pool.variantActiveBlocks) {
                if (GTUtility.canSeeSunClearly(world, pos)) exposedBlocks += 1;
            }
            pool.updateExposedBlocks(exposedBlocks);
        }
    }
}
