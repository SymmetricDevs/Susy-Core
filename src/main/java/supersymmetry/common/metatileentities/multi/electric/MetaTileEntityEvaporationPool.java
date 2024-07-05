package supersymmetry.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.BlockInfo;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.SusyLog;
import supersymmetry.api.capability.impl.EvapRecipeLogic;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockEvaporationBed;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import static gregtech.api.GregTechAPI.HEATING_COILS;
import static supersymmetry.common.metatileentities.SuSyMetaTileEntities.EVAPORATION_POOL_ID;

public class MetaTileEntityEvaporationPool extends RecipeMapMultiblockController{

    //reinventing the wheel
    class BitArray {
        int[] intArray;

        //assumes little endian [> index -> > significance]
        public BitArray(byte[] byteArray) {
            int bitCount = 0;
            bitCount += (byteArray[0]);
            bitCount += (byteArray[1] << 8);
            bitCount += (byteArray[2] << 16);
            bitCount += (byteArray[3] << 24);

            int byteCount = ((bitCount >> 3) + (((bitCount & 7) * -1) >>> 31) +4);
            intArray = new int[(bitCount >> 5) + (((bitCount & 31) * -1) >>> 31) +1];
            intArray[0] = bitCount;

            for (int i = 4; i < byteCount; ++i) {
                //all ints in array start as zero; bits are little endian and thus must be shifted by 8 "left" index % 4 times
                intArray[i/4] += byteArray[i] << (8 * (i & 3));
            }
        }

        public BitArray(int bitCount) {
            //(~(i * -1)) >>> 31
            intArray = new int[(bitCount >> 5) + (((bitCount & 31) * -1) >> 31) +1];
            intArray[0] = bitCount;
        }

        public BitArray(int bitCount, boolean initialState) {
            //0 * -1 is the only value in which the sign bit is not swapped after-wards and thus >>> 31 is 0 for 0
            intArray = new int[(bitCount >> 5) + (((bitCount & 31) * -1) >>> 31) +1];
            intArray[0] = bitCount;
            //SusyLog.logger.atError().log("number of ints: " + (bitCount >> 5) + (((bitCount & 31) * -1) >>> 31) + " with size: " + bitCount + "; size limit: " + (bitCount >> 5) +1);
            for (int i = 1; initialState && (i < (bitCount >> 5) +2); ++i) {
                intArray[i] = 0xFFFFFFFF; //set all to true if condition is true
            }
        }

        //index = 0 is least sig, with index = 31 as most sig.
        public boolean getBit(int index) {
            //index into integers based on index's multiple of 32 (index >> 5), then discard all less sig bits and mask all more sig bits. If result is 1 return 1, else 0
            return ((intArray[(index >> 5) +1] >> (index & 31)) & 1) == 1;
        }

        public void setBit(int index, boolean value) {
            int entry = intArray[(index >> 5) +1];
            //set appropriate integer equal to itself with single target bit at index masked, then or passed value into spot
            intArray[(index >> 5) +1] = entry & ~(1 << (index & 31)) | ((value ? 1 : 0) << (index & 31));
        }

        //uses little endian [> index -> > significance]
        public byte[] toByteArray() {
            int bitCount = intArray[0];
            int byteCount = ((bitCount >> 3) + (((bitCount & 7) * -1) >>> 31) +4);
            byte[] byteArray = new byte[byteCount];

            byteArray[0] = (byte) (bitCount & 0x00_00_00_FF);
            byteArray[1] = (byte) (bitCount & 0x00_00_FF_00);
            byteArray[2] = (byte) (bitCount & 0x00_FF_00_00);
            byteArray[3] = (byte) (bitCount & 0xFF_00_00_00);

            for (int i = 4; i < byteCount; ++i) {
                byteArray[i] = (byte) (intArray[i/4] & (0x00_00_00_FF << (8 * (i & 3))) );
            }

            return byteArray;
        }

        public String toString() {
            int bitCount = intArray[0];
            String output = "";
            for (int i = 0; i < bitCount; ++i) {
                if ((i & 7) == 0) {
                    output += "\n"; //new line for new byte
                }

                output += getBit(i) ? "1" : "0";
            }

            return output;
        }
    }

    /*
        For future reference: "((IGregTechTileEntity)world.getTileEntity(pos)).getMetaTileEntity() instanceof IMultiblockAbilityPart"
        is the way ceu gets mte from te. You might also try using MetaTileEntityHolder.
     */

    public static final int MAX_COLUMNS = 12; //two edge layers on either side, shouldn't exceed chunk boundary at max size

    public static final int structuralDimensionsID = 1051354;
    int columnCount = 1; //number of columns in row of controller (1 -> EDGEself(controller)EDGE, 2 -> EsCOLUMNe)
    int rowCount = 1; //number of rows where controller is placed on "edge" row
    int controllerPosition = 0; //column placement from left to right, where 0 = one from edge [ESCCCCE]

    public static final int predicateID = 10142156;
    public boolean isHeated = false;
    public int coilStateMeta = -1; //order is last in order dependent ops because I'm lazy

    public static final int energyValuesID = 10868607;
    int exposedBlocks = 0;
    BitArray wasExposed; //indexed with row*col + col with row = 0 being furthest and col 0 being leftmost when looking at controller
    int kiloJoules = 0; //about 1000J/s on a sunny day for 1/m^2 of area
    int joulesBuffer = 0;
    int tickTimer = 0;

    public static final int fluidNameID = 12090539;

    //just initialized on formation
    public IHeatingCoilBlockStats coilStats;

    public int getColumnCount() { return columnCount; }
    public int getRowCount() { return rowCount; }
    public int getControllerPosition() { return controllerPosition; }

    public static final ArrayList<IBlockState> validContainerStates = new ArrayList<>();

    static {
        validContainerStates.add(SuSyBlocks.EVAPORATION_BED.getState(BlockEvaporationBed.EvaporationBedType.DIRT));
        validContainerStates.addAll(MetaBlocks.WIRE_COIL.getBlockState().getValidStates()); //add all coils as valid container blocks
    }

    public MetaTileEntityEvaporationPool(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.EVAPORATION_POOL);
        this.recipeMapWorkable = new EvapRecipeLogic(this);

        columnCount = 1; //minimum of one column for controller to be placed on
        controllerPosition = 0; //controller starts off furthest to left
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityEvaporationPool(this.metaTileEntityId);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        //SusyLog.logger.atError().log("Invalidating Structure");
        this.isHeated = false;
        this.coilStateMeta = -1;

        this.exposedBlocks = 0;
        this.wasExposed = new BitArray(0);
        this.kiloJoules = 0;
        this.tickTimer = 0;

        this.writeCustomData(predicateID, (buf) -> {
            buf.writeBoolean(isHeated);
            buf.writeInt(coilStateMeta);
        });

        this.writeCustomData(energyValuesID, buf -> {
            buf.writeInt(exposedBlocks);
            buf.writeByteArray(wasExposed.toByteArray());
            buf.writeInt(kiloJoules);
            buf.writeInt(tickTimer);
        });
    }

    public boolean updateStructureDimensions() {

        World world = getWorld();
        EnumFacing front = this.getFrontFacing();
        EnumFacing back = front.getOpposite();
        EnumFacing right = front.rotateYCCW();
        EnumFacing left = right.getOpposite(); //left as if you were looking at it, not controller's left

        //distance to use when located edges of structure, moved inwards to container block portion for detection purposes
        BlockPos.MutableBlockPos lPos = new BlockPos.MutableBlockPos(getPos().offset(back, 2)); //start in container section
        BlockPos.MutableBlockPos rPos = new BlockPos.MutableBlockPos(getPos().offset(back, 2)); //start in container section
        BlockPos.MutableBlockPos bPos = new BlockPos.MutableBlockPos(getPos().offset(back)); //start one before container section

        //zero on both lDist and rDist indicates 1 column
        int lDist = -1;
        int rDist = -1;
        int bDist = -1;

        //SusyLog.logger.atError().log("BEFORE CALCS lDist: " + lDist + ", rDist: " + rDist + ", bDist: " + bDist);

        //find when container block section is exited left, right, and back
        for (int i = 1; i < MAX_COLUMNS +1; i++) {
            if (lDist == -1 && !isContainerBlock(world, lPos, left)) lDist += i; //0 -> immediate left is !container
            if (rDist == -1 && !isContainerBlock(world, rPos, right)) rDist += i; //0 -> immediate left is !container
            if (bDist == -1 && !isContainerBlock(world, bPos, back)) bDist += i; //0 -> no container block section
            if (lDist != -1 && rDist != -1 && bDist != -1) break;
        }

        //if l or r dist exceed max, or if bdist exceeds max/ is 0, or if l and r dist individually don't exceed max, but produce a columnCount > max
        if (lDist < 0 || rDist < 0 || bDist < 1 || lDist + rDist +1 > MAX_COLUMNS) {
            invalidateStructure();
            return false;
        }

        //SusyLog.logger.atError().log("AFTER CALCS lDist: " + lDist + ", rDist: " + rDist + ", bDist: " + bDist);

        //r,l dist = #container blocks in respective dir. +1 for controller block
        columnCount = rDist + lDist +1;
        rowCount = bDist; //"Depth" of container blocks
        controllerPosition = lDist; //if there are no blocks to the left controller is left most spot

        //SusyLog.logger.atFatal().log("I have just finished structure check" + getPos());
        //SusyLog.logger.atError().log("columnCount: " + columnCount + ", controllerPosition: " + controllerPosition + ", rowCount: " + rowCount);
        //SusyLog.logger.atError().log("isHeated: " + isHeated + ", kiloJoules: " + kiloJoules + ", tickTimer: " + tickTimer + ", coilStateMeta: " + coilStateMeta);

        //store the known dimensions for structure check
        this.writeCustomData(structuralDimensionsID, (buf) -> {
            buf.writeInt(columnCount);
            buf.writeInt(rowCount);
            buf.writeInt(controllerPosition);
        });

        return true; //successful formation
    }

    public boolean isContainerBlock(@Nonnull World world, @Nonnull BlockPos.MutableBlockPos pos, @Nonnull EnumFacing direction) {
        return validContainerStates.contains(world.getBlockState(pos.move(direction)));
    }

    public static String repeat(String s, int count) {
        if (s.length() == 0 || count < 1) { return ""; }
        if (count == 1) { return s; }

        //create empty char array, convert to string which places null terminators in all its positions, then replace all
        return new String(new char[count * s.length()]).replace("\0", s);
    }

    //generates rows with earlier entries being closer and later entries being further from controller
    public String[] centerRowsPattern() {
        //rows are done with custom logic due to coil placement; only bottom layer
        String[] containerRows = new String[rowCount];
        final String[] flooring = {"G", "C"};

        //i = row number, j = col number goes from closer rows to further rows
        for (int i = 0; i < rowCount; ++i) {
            //construct row builder for i'th row, setting first two entries to edge blocks
            StringBuilder containerRowBuilder = new StringBuilder(columnCount +4);
            containerRowBuilder.replace(0, 2, "EE");

            for (int j = 2; j < columnCount +2; ++j) {
                //if even (least sig bit [2^0] is 0) then do coil column
                if ((j & 1) == 0) {
                    //coil columns always have coils at their position
                    containerRowBuilder.replace(j, j +1, "C");
                } else if ((j -2 & 3) == 1) { //if j % 4 == 1, then do further crossover column
                    //if on last row (++i = rowCount) use "C" [1], otherwise use "G" [0]
                    containerRowBuilder.replace(j, j +1, flooring[(i +1)/rowCount]);
                } else { //if j % 4 == 3, then do closer crossover column
                    //if on first row then use "C" [1], otherwise use "G" [0] (0 is only number which is non neg after * -1. this means bit flip keeps most sig as 1)
                    containerRowBuilder.replace(j, j +1, flooring[(~(i * -1)) >>> 31]);
                }
            }

            //place edge blocks in last two positions, then store current row
            containerRowBuilder.replace(columnCount +2, columnCount +4, "EE");
            containerRows[i] = containerRowBuilder.toString();
        }

        return containerRows;
    }

    protected BlockPattern createStructurePattern() {
        /*
            'E' for "edge" block, that is to say essentially the blocks that contain the water.
            '#' for air block where hypothetical water would be.
            'S' for self, or controller.
            'C' for "container blocks" that can be coils, with coils required for powered heating.
            'G' for ground blocks which must be some non coil block
         */

        int currColCount = columnCount;
        int currRowCount = rowCount;
        int currContPos = controllerPosition;

        // return the default structure, even if there is no valid size found
        // this means auto-build will still work, and prevents terminal crashes.
        if (getWorld() != null) updateStructureDimensions();

        initializeCoilStats();

        // these can sometimes get set to 0 when loading the game, breaking JEI (Apparently; text from cleanroom impl)
        if (columnCount < 1) columnCount = 1;
        if (rowCount < 1) rowCount = 1;

        //if no bits are being stored, initialize
        if (wasExposed == null || wasExposed.intArray[0] == 0) {
            wasExposed = new BitArray(columnCount * rowCount, false);
            this.exposedBlocks = 0; //ensure exposedBlocks dont go higher than expected
        }

        if (structurePattern != null && currColCount == columnCount && currRowCount == rowCount && currContPos == controllerPosition) return structurePattern;

        //abstracted away construction of center rows for later use
        String[] containerRows = centerRowsPattern();

        FactoryBlockPattern pattern;

        //do first two rows including controller row and row behind controller
        pattern = FactoryBlockPattern.start()
                .aisle(repeat("E", columnCount +4), repeat(" ", columnCount +4))
                .aisle(repeat("E",columnCount +4), " ".concat(repeat("E",columnCount +2)).concat(" "));

        //place all generated aisles (rows stored closer to further, this wants them further to closer) and save to pattern explicitly (unsure if the explicit assignment is necessary; probably no harm in being safe)
        for (int i = 0; i < rowCount; ++i) { pattern = pattern.aisle(containerRows[rowCount -1 -i], " E".concat(repeat("#", columnCount).concat("E "))); }

        //place last two aisles
        pattern = pattern.aisle(repeat("E",columnCount +4), " ".concat(repeat("E",columnCount +2)).concat(" "))
                .aisle(repeat("E",controllerPosition +2).concat("S").concat(repeat("E",columnCount +1 -controllerPosition)), repeat(" ", columnCount +4))

                //begin predicates
                .where('S', selfPredicate())
                .where('E', isEdge()
                        .or(autoAbilities(false, false, true, true, true, true, false).setMaxGlobalLimited(8))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setMinGlobalLimited(0).setMaxGlobalLimited(2).setPreviewCount(0)))
                .where('G', isGround())
                .where('C', isContainer())
                .where('#', air())
                .where(' ', any());

        return pattern.build();
    }

    protected TraceabilityPredicate isEdge() {
        //supplies block info for display
        Supplier<BlockInfo[]> supplier = () -> new BlockInfo[]{new BlockInfo(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT))};

        //returns true if blockstate is concrete
        return new TraceabilityPredicate(blockWorldState -> {
            IBlockState state = blockWorldState.getBlockState();
            return state == MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT);
        }, supplier);
    }

    protected TraceabilityPredicate isGround() {
        //supplies block info for display
        Supplier<BlockInfo[]> supplier = () -> new BlockInfo[]{new BlockInfo(SuSyBlocks.EVAPORATION_BED.getState(BlockEvaporationBed.EvaporationBedType.DIRT))};

        //returns true if blockstate is "ground"
        return new TraceabilityPredicate(blockWorldState -> {
            IBlockState state = blockWorldState.getBlockState();
            //SusyLog.logger.atError().log("Checking 'G' predicate with state: " + state.toString());
            return isValidGround(state);
        }, supplier);
    }

    //ensures coil pattern is either entirely there or completely absent for structure to be valid. Sets isHeated accordingly
    protected TraceabilityPredicate isContainer() {
        Supplier<BlockInfo[]> supplier = () -> {
            ArrayList<BlockInfo> containerInfo = new ArrayList<BlockInfo>();

            //add evap bed types
            for (BlockEvaporationBed.EvaporationBedType type : BlockEvaporationBed.EvaporationBedType.values()) {
                containerInfo.add(new BlockInfo(SuSyBlocks.EVAPORATION_BED.getState(type)));
            }

            //add coil types
            GregTechAPI.HEATING_COILS.entrySet().stream()
                    .sorted(Comparator.comparingInt(entry -> entry.getValue().getTier())).forEach(entry -> containerInfo.add(new BlockInfo(entry.getKey())));

            //create array of correct size to "move" entries to
            BlockInfo[] containerInfoArray = new BlockInfo[containerInfo.size()];
            return containerInfo.toArray(containerInfoArray);
        };

        return new TraceabilityPredicate(blockWorldState -> {
            IBlockState state = blockWorldState.getBlockState();
            //SusyLog.logger.atError().log("Checking 'C' predicate with state: " + state.toString());

            return isValidCoil(state);
        }, supplier);
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                /* is backwards but lighting looks better
                .aisle("EEEEEESEEE", "          ")
                .aisle("EEEEEEEEEE", " EEEEEEEE ")
                .aisle("EEGCCCGCEE", " E######E ")
                .aisle("EEGCGCGCEE", " E######E ")
                .aisle("EEGCGCGCEE", " E######E ")
                .aisle("EEGCGCGCEE", " E######E ")
                .aisle("EEGCGCGCEE", " E######E ")
                .aisle("EECCGCCCEE", " E######E ")
                .aisle("EEEEEEEEEE", " EEEEEEEE ")
                .aisle("efFIEEEEEE", "          ")
                 */
                .aisle("efFIEEEEEE", "          ")
                .aisle("EEEEEEEEEE", " EEEEEEEE ")
                .aisle("EECCCGCCEE", " E######E ")
                .aisle("EECGCGCGEE", " E######E ")
                .aisle("EECGCGCGEE", " E######E ")
                .aisle("EECGCGCGEE", " E######E ")
                .aisle("EECGCGCGEE", " E######E ")
                .aisle("EECGCCCGEE", " E######E ")
                .aisle("EEEEEEEEEE", " EEEEEEEE ")
                .aisle("EEESEEEEEE", "          ")
                .where('S', GregTechAPI.MTE_REGISTRY.getObjectById(EVAPORATION_POOL_ID), EnumFacing.SOUTH)
                .where('E', MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT))
                .where('G', SuSyBlocks.EVAPORATION_BED.getState(BlockEvaporationBed.EvaporationBedType.DIRT))
                .where('#', Blocks.AIR.getDefaultState())
                .where(' ', Blocks.AIR.getDefaultState()) //supposed to be any
                .where('e', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.LV], EnumFacing.NORTH)
                .where('f', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.LV], EnumFacing.NORTH)
                .where('F', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.LV], EnumFacing.NORTH)
                .where('I', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.LV], EnumFacing.NORTH);

        GregTechAPI.HEATING_COILS.entrySet().stream()
                .sorted(Comparator.comparingInt(entry -> entry.getValue().getTier()))
                .forEach(entry -> shapeInfo.add(builder.where('C', entry.getKey()).build()));

        return shapeInfo;
    }

    //returns -1 for no errors, -2 for groundcheck error, else it returns position of failure, counting from left to right then closer to further
    public int coilPatternCheck(boolean checkGround) {
        //first coil block has been found; ensure full coil pattern is present so that future validation checks may be skipped
        String[] centerPattern = centerRowsPattern();
        IBlockState targetState = (coilStateMeta == -1) ? null : MetaBlocks.WIRE_COIL.getStateFromMeta(coilStateMeta); //if stored coilstate exists

        EnumFacing right = getFrontFacing().rotateYCCW(); //right if you are looking at controller
        EnumFacing back = getFrontFacing().getOpposite(); //further from front of controller

        //get blockpos of first row, leftmost center block
        BlockPos.MutableBlockPos targetPos = new BlockPos.MutableBlockPos(getPos().offset(back, 2)); //place on first row
        targetPos.move(right.getOpposite(), controllerPosition +1); //move to the furthest left center block +1 (looking at controller) to account for immediate right deviation in j loop

        //traverse center pattern rows (closer to further)
        for (int i = 0; i < rowCount; ++i) {
            //traverse center pattern columns in 'i'th row, skipping edges
            for (int j = 2; j < columnCount + 2; ++j) {
                targetPos.move(right); //go right one first to allow for continue to be used later

                if (centerPattern[i].charAt(j) != 'C') {
                    if (!checkGround) continue; //skip non coil entries if checkGround is false

                    //do not error out if invalid
                    if (getWorld().getBlockState(targetPos) == SuSyBlocks.EVAPORATION_BED.getState(BlockEvaporationBed.EvaporationBedType.DIRT)) continue;
                    return ((i * columnCount + j -2) * -1) -2; //ground check failed; return index number's negative shifted down two such that range => [0, INT_MAX] -> [-INT_MAX, -2]
                }

                //store state of first coil. Might do weird stuff if meta of coilState > 7, as jei error-ed when trying to display them even though they are same material type
                if (targetState == null) {
                    targetState = getWorld().getBlockState(targetPos); //establish coil type from first coil
                    if (MetaBlocks.WIRE_COIL.getBlockState().getValidStates().contains(targetState)) continue; //if first coil position found ensure it is valid coil and continue if it is
                }
                else if (getWorld().getBlockState(targetPos).toString().equals(targetState.toString())) continue; //proceed only if current coil is exact same as previous coil(s). equals seems to fail, so a string comparison works best)

                //current coilpos block is either not a coil (first coil position), or it is not identical to previous coil(s)
                return i * columnCount + j -2; //indicate failure of solid coil structure through >0 value. Stores relative position of failure in int (kind of pointless now; was for old code that got pos from this)
            }

            targetPos.move(back); //go back one row
            targetPos.move(right.getOpposite(), columnCount); //reset to leftmost center position +1 unit left due to order of traversal inside nested loop
        }

        if (!checkGround) coilStateMeta = targetState.getBlock().getMetaFromState(targetState); //store correct coilstate (necessary as passing coilState would pass the reference by value, meaning reassignment would not carry to caller)
        return -1; //no checks failed
    }

    //takes result int from coilPatternCheck, outputting boolean related to whether or not structure was found to be valid
    public boolean handleCoilCheckResult(int result) {
        if (result != -1) {
            coilStateMeta = -1;
            isHeated = false;
            initializeCoilStats();
            return false;
        } //block is a coil at this point, so if coil pattern fails exit with false (invalidates structure)

        //if check is passed structure is valid heatable evap pool
        isHeated = true;
        initializeCoilStats();

        this.writeCustomData(predicateID, (buf) -> {
            buf.writeBoolean(isHeated);
            buf.writeInt(coilStateMeta);
        });

        return true;
    }

    public void initializeCoilStats() { coilStats = coilStateMeta > -1 ? HEATING_COILS.get(MetaBlocks.WIRE_COIL.getStateFromMeta(coilStateMeta)) : null; }

    public boolean isValidCoil(IBlockState state) {
        if (!MetaBlocks.WIRE_COIL.getBlockState().getValidStates().contains(state)) return this.isValidGround(state);

        //if not contained in valid coil states this part of method wouldn't have run
        if (coilStateMeta == -1) return true;
        return state.toString().equals(MetaBlocks.WIRE_COIL.getStateFromMeta(coilStateMeta).toString());
    }

    public boolean isValidGround(IBlockState state) {
        return state == SuSyBlocks.EVAPORATION_BED.getState(BlockEvaporationBed.EvaporationBedType.DIRT);
    }

    /*
        apparently the structurepattern's worldstate is being set to error at the furthest and leftmost coil block whenever
        multiple coils are destroyed, which causes weird behavior when attempting to reform the structure. The only way to get around
        this without more thoroughly understanding how the structure checks work is to manually change the error to any invalid center block,
        though I suspect even if I did know what the issue was more specifically this would still be the only solution.
        Incomprehensibly bugged kila code lets fucking go
     */
    @Override
    public void checkStructurePattern() {
        //SusyLog.logger.atError().log("Checking structure pattern");
        // This is here so that it automatically updates the dimensions once a second if it isn't formed
        // hope this doesn't put too much of a toll on TPS - It really should not
        if (!isStructureFormed() || structurePattern == null) {
            reinitializeStructurePattern();
        }

        super.checkStructurePattern();
        //SusyLog.logger.atError().log("Finished Super structure pattern check");

        //only do check every 4 seconds while structure is formed
        if ((tickTimer / 20 & 3) == 0 && structurePattern != null && structurePattern.getError() == null) {
            handleCoilCheckResult(coilPatternCheck(false));
        }

        //SusyLog.logger.atError().log("StructurePattern: " + (structurePattern == null ? "null" : (structurePattern.getError() == null ? "No Error" : structurePattern.getError().getErrorInfo())));
    }

    //for future reference
    /*
    public void removeError() {
        try {
            Field privateField = structurePattern.getClass().getDeclaredField("worldState"); //grab worldState field
            privateField.setAccessible(true); //make world state accessible
            BlockWorldState structurePatternWorldState = (BlockWorldState)privateField.get(structurePattern); //grab worldState
            structurePatternWorldState.setError(null);
            SusyLog.logger.atError().log("Caballed error.");
        } catch (Exception e) {
            SusyLog.logger.atError().log("failed to do reflection for evaporation pool");
        }
    }
     */

    //for future reference
    /*
    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(TooltipHelper.RAINBOW_SLOW + I18n.format("gregtech.machine.perfect_oc", new Object[0]));

        //adds visual for edge blocks, "container blocks", and the controller where E -> 0, C -> o, and S -> X.
        tooltip.add("00".concat(repeat("o", controllerPosition).concat("X").concat(repeat("o", columnCount -controllerPosition -1))).concat("00"));
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        textList.add(new TextComponentString("00".concat(repeat("o", controllerPosition).concat("X").concat(repeat("o", columnCount -controllerPosition -1))).concat("00")));
        super.addDisplayText(textList);
    }

    //felt like using this instead of a gui to vary controller pos. Custom logic on runs on crouch so player can choose if they want it muted or not after.
    @Override
    public boolean onHardHammerClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing, CuboidRayTraceResult hitResult) {
        // if not sneaking, don't perform special logic. If sneaking, don't perform muting logic
        if (!playerIn.isSneaking()) {
            return super.onHardHammerClick(playerIn, hand, facing, hitResult);
        }

        controllerPosition = (controllerPosition +1) % columnCount; // increase controller position/index on range [0, columnCount -1]

        this.writeCustomData(108, (buf) -> {
            buf.writeInt(controllerPosition);
        });

        return true;
    }

    //nothing takes advantage of this being open, so I'm going to use it to control number of columns. More important property is more easily accessed.
    @Override
    public void onLeftClick(EntityPlayer player, EnumFacing facing, CuboidRayTraceResult hitResult) {
        //if not sneaking, don't perform special logic
        if (!player.isSneaking()) {
            super.onLeftClick(player, facing, hitResult);
            return;
        }

        //increase number of columns on range [1, maxColumns]
        columnCount = (columnCount +1) % maxColumns +1;

        if (columnCount == 0) {
            ++columnCount; //ensure at least one column at all times
            controllerPosition = 0; //ensure controller position <= columnCount
        }

        //write custom data (not sure if this is necessary or not)
        this.writeCustomData(104, (buf) -> {
            buf.writeInt(columnCount);
        });

        this.writeCustomData(108, (buf) -> {
            buf.writeInt(controllerPosition);
        });
    }
     */

    @Override
    public void addInformation(ItemStack stack, World player, @NotNull List<String> tooltip, boolean advanced) {
        if (TooltipHelper.isShiftDown()) {
            tooltip.add(I18n.format("gregtech.machine.evaporation_pool.tooltip.structure_info", MAX_COLUMNS, MAX_COLUMNS) + "\n");
        }
    }

    public BlockPos.MutableBlockPos getCorner(boolean isClose, boolean isLeft) {
        BlockPos.MutableBlockPos corner = new BlockPos.MutableBlockPos(getPos());
        EnumFacing back = getFrontFacing().getOpposite();
        EnumFacing left = getFrontFacing().rotateY(); //left if you're looking at controller

        corner.move(EnumFacing.UP); //place on level of fluid in pool

        //Puts on leftmost or rightmost column
        if (isLeft) {
            corner.move(left, controllerPosition); //line up with leftmost column of interior
        } else {
            corner.move(left.getOpposite(), columnCount - controllerPosition -1); //line up w/ furthest right interior column
        }

        //Puts on closest or furthest row
        if (isClose) {
            corner.move(back, 2); //place in interior
        } else {
            corner.move(back, rowCount +1); //move onto last row
        }

        return corner;
    }

    /*
    public static void renderTankFluid(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, FluidTank tank, IBlockAccess world, BlockPos pos, EnumFacing frontFacing) {
        float lastBrightnessX = OpenGlHelper.lastBrightnessX;
        float lastBrightnessY = OpenGlHelper.lastBrightnessY;
        if (world != null) {
            renderState.setBrightness(world, pos);
        }
        FluidStack stack = tank.getFluid();
        if (stack == null || stack.amount == 0)
            return;

        Cuboid6 partialFluidBox = new Cuboid6(1.0625 / 16.0, 2.0625 / 16.0, 1.0625 / 16.0, 14.9375 / 16.0, 14.9375 / 16.0, 14.9375 / 16.0);

        double fillFraction = (double) stack.amount / tank.getCapacity();
        if (tank.getFluid().getFluid().isGaseous()) {
            partialFluidBox.min.y = Math.max(13.9375 - (11.875 * fillFraction), 2.0) / 16.0;
        } else {
            partialFluidBox.max.y = Math.min((11.875 * fillFraction) + 2.0625, 14.0) / 16.0;
        }

        renderState.setFluidColour(stack);
        ResourceLocation fluidStill = stack.getFluid().getStill(stack);
        TextureAtlasSprite fluidStillSprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(fluidStill.toString());
        for (EnumFacing facing : EnumFacing.VALUES) {
            Textures.renderFace(renderState, translation, pipeline, facing, partialFluidBox, fluidStillSprite, BlockRenderLayer.CUTOUT_MIPPED);
        }
        GlStateManager.resetColor();

        renderState.reset();
    }
     */

    public <T extends MultiblockRecipeLogic> void renderFluid(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline, IBlockAccess world, EnumFacing frontFacing, T recipeMapWorkable, BlockPos.MutableBlockPos controllerPos) {

        if ((this.tickTimer +1) % 20 == 0) {
            SusyLog.logger.atError().log("About to attempt rendering with getWorld: " + (world == null ? "null" : world) + ", isStructureFormed: " + (isStructureFormed()) + ", isActive: " + (recipeMapWorkable == null ? "null" : (recipeMapWorkable.isActive()) + ", previousRecipe: " + (recipeMapWorkable.getPreviousRecipe() == null ? "null" : recipeMapWorkable.isActive())));
        } else { return; }

        if (world != null) {
            renderState.setBrightness(world, controllerPos);
        }

        if (controllerPos == null || !this.isStructureFormed() || !recipeMapWorkable.isActive() || recipeMapWorkable.getPreviousRecipe() == null) { return; }

        SusyLog.logger.atError().log("Passed rendering check");

        //Create a box with slight margins since going from exactly 0 to exactly 1 causes some faces to not render properly
        Cuboid6 partialFluidBox = new Cuboid6(0.001, 0.001, 0.001, 0.999,
                0.875, 0.999);

        //Set our box to the percentage of the tank that is full
        //partialFluidBox.max.y = 0.875f; //should be 15 pixels out of 16 full

        //when looking at controller facing south leftmost col is West, and when facing West leftmost is North
        boolean isLeftMostColumn = frontFacing == EnumFacing.SOUTH || frontFacing == EnumFacing.WEST;
        boolean isFurthestRow = frontFacing == EnumFacing.SOUTH || frontFacing == EnumFacing.WEST;

        //scale only works into positive direction, so we want block in most negative corner for both coords [W, N]
        BlockPos.MutableBlockPos corner = getCorner(isFurthestRow, isLeftMostColumn);

        int EWFacing = frontFacing.getHorizontalIndex() & 1; //odd horizontal index
        int NSFacing = (frontFacing.getHorizontalIndex() +1) & 1; //even horizontal index

        //apply translations to scale properly; "back distance and length are switched in the case of WEST and EAST as a form of "rotation" since translation.rotate is broken..."
        translation.translate((corner.getX() - controllerPos.getX()), 1, corner.getZ() - controllerPos.getZ());
        translation.scale(EWFacing * rowCount + NSFacing * columnCount, 1, EWFacing * columnCount + NSFacing * rowCount);

        FluidStack fluidStack = Materials.Water.getFluid(1000); //default is water

        //previous recipe is actually previously found recipe which is now being run, or attempting to be run
        if (recipeMapWorkable.getPreviousRecipe().getFluidInputs() != null) {
            fluidStack = recipeMapWorkable.getPreviousRecipe().getFluidInputs().get(0).getInputFluidStack();
        }

        //store previous renderer state
        //CubeRendererState op = Textures.RENDER_STATE.get();
        renderState.setBrightness(getWorld(), controllerPos);

        //modify render state for own uses and then render top
        //Textures.RENDER_STATE.set(new CubeRendererState(op.layer, CubeRendererState.PASS_MASK, op.world));
        renderState.setFluidColour(fluidStack);
        TextureAtlasSprite fluidStillSprite = Minecraft.getMinecraft().getTextureMapBlocks()
                .getAtlasSprite(fluidStack.getFluid().getStill(fluidStack).toString());

        for (EnumFacing facing : EnumFacing.VALUES) {
            Textures.renderFace(renderState, translation, pipeline, facing, partialFluidBox, fluidStillSprite,
                    BlockRenderLayer.CUTOUT_MIPPED);
        }

        //Go back to normal for the next rendering call
        GlStateManager.resetColor();

        renderState.reset();
    }

    /* I give up
    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        if ((tickTimer +1) % 20 == 0) {
            SusyLog.logger.atError().log("BEFORE RENDER CALL with world: " + (this.getWorld() == null ? "null" : this.getWorld()) + ", isStructureFormed: " + (this.isStructureFormed()) + ", isActive: " + (this.getRecipeMapWorkable() == null ? "null" : (this.getRecipeMapWorkable().isActive()) + ", previousRecipe: " + (this.getRecipeMapWorkable().getPreviousRecipe() == null ? "null" : this.getRecipeMapWorkable().isActive())));
            SusyLog.logger.atError().log("BEFORE RENDER CALL; pos: " + getPos());
        }

        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), recipeMapWorkable.isActive(), recipeMapWorkable.isWorkingEnabled());
        renderFluid(renderState, translation, pipeline, getWorld(), getFrontFacing(), getRecipeMapWorkable(), new BlockPos.MutableBlockPos(getPos()));

            EnumFacing back = getFrontFacing().getOpposite();
            Matrix4 offset = translation.copy().translate(back.getXOffset(), -0.3, back.getZOffset());
            CubeRendererState op = Textures.RENDER_STATE.get();
            Textures.RENDER_STATE.set(new CubeRendererState(op.layer, CubeRendererState.PASS_MASK, op.world));
            Textures.renderFace(renderState, offset,
                    ArrayUtils.addAll(pipeline, new LightMapOperation(240, 240), new ColourOperation(0xFFFFFFFF)),
                    EnumFacing.UP, Cuboid6.full, TextureUtils.getBlockTexture("lava_still"), BloomEffectUtil.getRealBloomLayer());
            Textures.RENDER_STATE.set(op);
    }
    */

    @Override
    public void update() {
        super.update(); //means recipe logic happens before heating is added

        if(this.getWorld().isRemote) {
            if (this.isActive()) {
                //if world is clientside (remote from server) do custom rendering
                evaporationParticles();
            }

            return; //dont do logic on client
        }

        //dont do processing for unformed blocks
        if (structurePattern.getError() != null) return;

        //ensure timer is non-negative by anding sign bit with 0
        tickTimer = tickTimer & 0b01111111111111111111111111111111;

        //should skip/cost an extra tick the first time and then anywhere from 1-19 extra when rolling over. Determines exposedblocks
        if (tickTimer % 20 == 0 && tickTimer != 0) {
            //no sunlight heat generated when raining or during night. May be incongruent with partial exposure to sun, but oh well
            if (getWorld().isRainingAt(getPos().offset(getFrontFacing().getOpposite(), 2)) || !getWorld().isDaytime()) {
                //SusyLog.logger.atError().log("setting exposed blocks to 0");
                exposedBlocks = 0;
            }

            //checks for ow and skylight access to prevent beneath portal issues (-1 = the Nether, 0 = normal world)
            else if (getWorld().provider.getDimension() == 0) {
                //tickTimer is always multiple of 20 at this point, so division by 20 yields proper counter. You can treat (tickTimer/20) as 'i'
                int row = ((tickTimer /20) /columnCount) % rowCount; //going left to right, further to closer checking skylight access.
                int col = ((tickTimer /20) % columnCount);
                //SusyLog.logger.atError().log("Back: " + getFrontFacing().getOpposite() + ", Left: " + getFrontFacing().rotateY() + ", Right: " + getFrontFacing().rotateYCCW() + "; row: " + row + ", col: " + col);
                //places blockpos for skycheck into correct position. Row counts from furthest to closest (kinda inconsistent but oh well)
                BlockPos.MutableBlockPos skyCheckPos = new BlockPos.MutableBlockPos(getPos().offset(EnumFacing.UP, 2));
                skyCheckPos.move(getFrontFacing().getOpposite(), rowCount - row +1);
                skyCheckPos.move(getFrontFacing().rotateY(), controllerPosition); //move to the furthest left
                skyCheckPos.move(getFrontFacing().rotateYCCW(), col); //traverse down row

                //SusyLog.logger.atError().log("About to check sky access for block at pos: " + skyCheckPos + " with bitArray: " + wasExposed.toString());

                //Perform skylight check
                if (!getWorld().canBlockSeeSky(skyCheckPos)) {
                    //SusyLog.logger.atError().log("Failed sky check at " + skyCheckPos + " ; " + getWorld().canBlockSeeSky(skyCheckPos) + " w/ exposedBlocks: " + exposedBlocks);
                    //only decrement exposedBlocks if previously exposed block is found to no longer be exposed and one full pass has occurred
                    if (wasExposed.getBit((row * columnCount) + col) && tickTimer/20 > rowCount * columnCount) {
                        exposedBlocks = Math.max(0, exposedBlocks -1);
                        wasExposed.setBit((row * columnCount) + col, false);
                    }
                }
                else {
                    //SusyLog.logger.atError().log("Passed sky check at " + skyCheckPos + " ; " + getWorld().canBlockSeeSky(skyCheckPos) + " w/ exposedBlocks: " + exposedBlocks);
                    //only increment if block was not previously exposed
                    if (!wasExposed.getBit((row * columnCount) + col)) {
                        if (exposedBlocks < rowCount * columnCount) ++exposedBlocks;
                        wasExposed.setBit((row * columnCount) + col, true);
                    }
                    //SusyLog.logger.atError().log("exposedBlock: " + exposedBlocks);
                }
            }

        } //finish once a ~second check

        //if (tickTimer % 100 == 0) {
            //SusyLog.logger.atFatal().log("I am still alive at " + getPos());
            //SusyLog.logger.atError().log("columnCount: " + columnCount + ", controllerPosition: " + controllerPosition + ", rowCount: " + rowCount);
            //SusyLog.logger.atError().log("isHeated: " + isHeated + ", exposedBlocks: " + exposedBlocks + ", kiloJoules: " + kiloJoules + ", joulesBuffer: " + joulesBuffer + ", tickTimer: " + tickTimer + ", coilStateMeta: " + coilStateMeta);
        //}

        inputEnergy(exposedBlocks * 50); //1kJ/s /m^2 -> 50J/t

        //convert joules in buffer to kJ
        if (joulesBuffer >= 1000) {
            int tempBuffer = joulesBuffer;
            joulesBuffer = 0;
            //if energy was not stored into kiloJoules, place everything back into buffer manually
            if (!inputEnergy(tempBuffer)) joulesBuffer = tempBuffer;
        }

        ++tickTimer;

        //store relevant values
        this.writeCustomData(energyValuesID, buf -> {
            buf.writeInt(exposedBlocks);
            buf.writeByteArray(wasExposed == null ? new BitArray(0).toByteArray() : wasExposed.toByteArray());
            buf.writeInt(kiloJoules);
            buf.writeInt(tickTimer);
        });
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("columnCount", this.columnCount);
        data.setInteger("rowCount", this.rowCount);
        data.setInteger("controllerPosition", this.controllerPosition);
        data.setBoolean("isHeated", this.isHeated);
        data.setInteger("exposedBlocks", this.exposedBlocks);
        data.setByteArray("wasExposed", this.wasExposed == null ? new BitArray(0).toByteArray() : this.wasExposed.toByteArray());
        data.setInteger("kiloJoules", this.kiloJoules);
        data.setInteger("tickTimer", this.tickTimer);
        data.setInteger("coilStateMeta", this.coilStateMeta);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        if (data.hasKey("columnCount")) {
            this.columnCount = data.getInteger("columnCount");
        }
        if (data.hasKey("rowCount")) {
            this.rowCount = data.getInteger("rowCount");
        }
        if (data.hasKey("controllerPosition")) {
            this.controllerPosition = data.getInteger("controllerPosition");
        }
        if (data.hasKey("isHeated")) {
            this.isHeated = data.getBoolean("isHeated");
        }
        if (data.hasKey("exposedBlocks")) {
            this.exposedBlocks = data.getInteger("exposedBlocks");
        }
        if (data.hasKey("wasExposed")) {
            this.wasExposed = new BitArray(data.getByteArray("wasExposed"));
        }
        if (data.hasKey("kiloJoules")) {
            this.kiloJoules = data.getInteger("kiloJoules");
        }
        if (data.hasKey("tickTimer")) {
            this.tickTimer = data.getInteger("tickTimer");
        }
        if (data.hasKey("coilStateMeta")) {
            this.coilStateMeta = data.getInteger("coilStateMeta");
            initializeCoilStats();
        }
        //SusyLog.logger.atFatal().log("before reinit");
        //SusyLog.logger.atError().log("columnCount: " + columnCount + ", controllerPosition: " + controllerPosition + ", rowCount: " + rowCount);
        //SusyLog.logger.atError().log("isHeated: " + isHeated + ", kiloJoules: " + kiloJoules + ", tickTimer: " + tickTimer + ", coilStateMeta: " + coilStateMeta);
        reinitializeStructurePattern();
        //SusyLog.logger.atFatal().log("after reinit");
        //SusyLog.logger.atError().log("columnCount: " + columnCount + ", controllerPosition: " + controllerPosition + ", rowCount: " + rowCount);
        //SusyLog.logger.atError().log("isHeated: " + isHeated + ", kiloJoules: " + kiloJoules + ", tickTimer: " + tickTimer + ", coilStateMeta: " + coilStateMeta);
    }

    //order matters for these
    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.columnCount);
        buf.writeInt(this.rowCount);
        buf.writeInt(this.controllerPosition);
        buf.writeBoolean(this.isHeated);
        buf.writeInt(this.exposedBlocks);
        buf.writeByteArray(this.wasExposed == null ? new BitArray(0).toByteArray() : this.wasExposed.toByteArray());
        buf.writeInt(this.kiloJoules);
        buf.writeInt(this.tickTimer);
        buf.writeInt(this.coilStateMeta);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.columnCount = buf.readInt();
        this.rowCount = buf.readInt();
        this.controllerPosition = buf.readInt();
        this.isHeated = buf.readBoolean();
        this.exposedBlocks = buf.readInt();
        this.wasExposed = new BitArray(buf.readByteArray());
        this.kiloJoules = buf.readInt();
        this.tickTimer = buf.readInt();
        this.coilStateMeta = buf.readInt();
        reinitializeStructurePattern();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == structuralDimensionsID) {
            this.columnCount = buf.readInt();
            this.rowCount = buf.readInt();
            this.controllerPosition = buf.readInt();
        } else if (dataId == predicateID) {
            this.isHeated = buf.readBoolean();
            this.coilStateMeta = buf.readInt();
            initializeCoilStats();
        } else if (dataId == energyValuesID) {
            this.exposedBlocks = buf.readInt();
            this.wasExposed = new BitArray(buf.readByteArray());
            this.kiloJoules = buf.readInt();
            this.tickTimer = buf.readInt();
        }
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    private void evaporationParticles() {
        final EnumFacing back = this.getFrontFacing().getOpposite();
        final EnumFacing left = back.rotateYCCW();

        //conversion from blockpos to in world pos places particle position at corner of block such that relative direction must be accounted for
        //add 1 if x pos or z pos (offsets mutually exclusively non-zero for given facing) as conversion to float pos rounds down in both coords
        int leftOffset = ((left.getXOffset() * -1) >>> 31) + ((left.getZOffset() * -1) >>> 31);
        int backOffset = ((back.getXOffset() * -1) >>> 31) + ((back.getZOffset() * -1) >>> 31);

        //place pos in closest leftmost corner
        final BlockPos pos = this.getPos().offset(left, controllerPosition + leftOffset).offset(back, 1 + backOffset);

        //Spawn number of particles on range [1, 3 * colCount * rowCount/9] (~3 particles for every 3x3 = 9m^2)
        for (int i = 0; i < Math.max(1, columnCount * rowCount/3); i++) {
            //either single line along x axis intersects all rows, or it intersects all columns. Same applies to z axis. getXOffset indicates +, -, or "0" direction of facing for given axis
            float xLength = (back.getXOffset() * rowCount) + (left.getXOffset() * columnCount * -1); //we start on leftmost closest corner and want to go back and to the right
            float zLength = (back.getZOffset() * rowCount) + (left.getZOffset() * columnCount * -1); //so we invert the sign of the left offsets to effectively get right displacement

            //if (tickTimer % 100 == 0) SusyLog.logger.atError().log("xLength: " + xLength + ", zLength: " + zLength + ", leftOffset: " + leftOffset + ", backOffset: " + backOffset + ", pos: " + pos + ", controller pos: " + getPos());

            float xPos = pos.getX() + (xLength * GTValues.RNG.nextFloat()); //scale x length by random amount to get output coord
            float yPos = pos.getY() + 0.75F; //shit out particles one quarter of a block below the surface of the interior to give effect of gases rising from bottom
            float zPos = pos.getZ() + (zLength * GTValues.RNG.nextFloat());

            float ySpd = 0.4F + 0.2F * GTValues.RNG.nextFloat();
            getWorld().spawnParticle(EnumParticleTypes.CLOUD, xPos, yPos, zPos, 0, ySpd, 0);
        }
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    protected ICubeRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    public int getKiloJoules() {
        return kiloJoules;
    }

    public int getJoulesBuffer() {
        return joulesBuffer;
    }

    public boolean getIsHeated() {
        return isHeated;
    }

    public void setKiloJoules(int kiloJoules) {
        this.kiloJoules = kiloJoules;
    }

    public void setJoulesBuffer(int joulesBuffer) {
        this.joulesBuffer = joulesBuffer;
    }

    public void setIsHeated(boolean isHeated) {
        this.isHeated = isHeated;
    }

    public boolean inputEnergy(int joules) {
        //limit amount of energy stored
        final int CUBE_HEAT_CAPACITY = 100; //kJ/m^3
        if (getKiloJoules() > CUBE_HEAT_CAPACITY * columnCount * rowCount) {
            return false;
        }

        int kJ = joules/1000;
        joules -= kJ * 1000;
        joulesBuffer += joules;

        //store kJ
        setKiloJoules(getKiloJoules() + kJ);
        return true;
    }

    public int calcMaxSteps(int jStepSize) {
        int stepCount = (getKiloJoules() * 1000)/jStepSize; //max number of times jStepSize can cleanly be deducted from kiloJoules
        int remainder = (stepCount +1) * jStepSize - getKiloJoules() * 1000; //remaining joules needed to not waste partial kJ

        if (joulesBuffer > remainder) ++stepCount;
        else remainder = 0;

        stepCount += (joulesBuffer - remainder)/jStepSize; //number of jSteps which can come entirely from joulesBuffer
        return stepCount;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }
}
