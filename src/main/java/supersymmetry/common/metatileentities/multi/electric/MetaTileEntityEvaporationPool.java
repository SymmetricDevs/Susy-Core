package supersymmetry.common.metatileentities.multi.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.texture.TextureUtils;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.client.renderer.CubeRendererState;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.cclop.ColourOperation;
import gregtech.client.renderer.cclop.LightMapOperation;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import supersymmetry.api.SusyLog;
import supersymmetry.api.capability.impl.NoEnergyMultiblockRecipeLogic;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockEvaporationBed;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;

import static supersymmetry.common.metatileentities.SuSyMetaTileEntities.EVAPORATION_POOL_ID;

public class MetaTileEntityEvaporationPool extends RecipeMapMultiblockController {

    /*
        For future reference: "((IGregTechTileEntity)world.getTileEntity(pos)).getMetaTileEntity() instanceof IMultiblockAbilityPart"
        is the way ceu gets mte from te. You might also try using MetaTileEntityHolder.
     */

    static final int maxColumns = 12; //two edge layers on either side, shouldn't exceed chunk boundary at max size

    public static final int structuralDimensionsID = 1051354;
    protected int columnCount = 1; //number of columns in row of controller (1 -> EDGEself(controller)EDGE, 2 -> EsCOLUMNe)
    protected int rowCount = 1; //number of rows where controller is placed on "edge" row
    protected int controllerPosition = 0; //column placement from left to right, where 0 = one from edge [ESCCCCE]

    public static final int predicateID = 10142156;
    public boolean isHeated = false;
    int coilStateMeta = -1; //order is last in order dependent ops because I'm lazy

    public static final int energyValuesID = 10868607;
    int exposedBlocks = 0;
    //about 1000J/s on a sunny day for 1/m^2 of area
    private int kiloJoules = 0;
    int tickTimer = 0;

    public static final int fluidNameID = 12090539;
    String currentFluidName = "water";

    public static final ArrayList<IBlockState> validContainerStates = new ArrayList<>();

    static {
        validContainerStates.add(SuSyBlocks.EVAPORATION_BED.getState(BlockEvaporationBed.EvaporationBedType.DIRT));
        //add all coils as valid container blocks
        for (IBlockState state : MetaBlocks.WIRE_COIL.getBlockState().getValidStates()) { validContainerStates.add(state); }
    }

    public MetaTileEntityEvaporationPool(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.EVAPORATION_POOL);
        this.recipeMapWorkable = new NoEnergyMultiblockRecipeLogic(this);

        columnCount = 1; //minimum of one column for controller to be placed on
        controllerPosition = 0; //controller starts off furthest to left
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityEvaporationPool(this.metaTileEntityId);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.exposedBlocks = 0;
        this.kiloJoules = 0;
        this.tickTimer = 0;
        this.isHeated = false;
        this.coilStateMeta = -1;
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

        //find when container block section is exited left, right, anb back
        for (int i = 1; i < maxColumns +1; i++) {
            if (lDist == -1 && !isContainerBlock(world, lPos, left)) lDist += i; //0 -> immediate left is !container
            if (rDist == -1 && !isContainerBlock(world, rPos, right)) rDist += i; //0 -> immediate left is !container
            if (bDist == -1 && !isContainerBlock(world, bPos, back)) bDist += i; //0 -> no container block section
            if (lDist != -1 && rDist != -1 && bDist != -1) break;
        }

        //if l or r dist exceed max, or if bdist exceeds max/ is 0
        if (lDist < 0 || rDist < 0 || bDist < 1) {
            invalidateStructure();
            return false;
        }

        //r,l dist = #container blocks in respective dir. +1 for controller block
        columnCount = rDist + lDist +1;
        rowCount = bDist; //"Depth" of container blocks
        controllerPosition = lDist; //if there are no blocks to the left controller is left most spot

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

        // return the default structure, even if there is no valid size found
        // this means auto-build will still work, and prevents terminal crashes.
        if (getWorld() != null) updateStructureDimensions();

        // these can sometimes get set to 0 when loading the game, breaking JEI (Apparently; text from cleanroom impl)
        if (columnCount < 1) columnCount = 1;
        if (rowCount < 1) rowCount = 1;

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
                        .or(autoAbilities(false, false, true, true, true, true, false).setMaxGlobalLimited(4))
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setPreviewCount(0)))
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
            SusyLog.logger.atError().log("Checking 'G' predicate with state: " + state.toString());
            return isValidGround(state);
        }, supplier);
    }

    //ensures coil pattern is either entirely there or completely absent for structure to be valid. Sets isHeated accordingly
    protected TraceabilityPredicate isContainer() {
        Supplier<BlockInfo[]> supplier = () -> {
            ArrayList<BlockInfo> containerInfo = new ArrayList();

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
            SusyLog.logger.atError().log("Checking 'C' predicate with state: " + state.toString());

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
            return false;
        }; //block is a coil at this point, so if coil pattern fails exit with false (invalidates structure)

        //if check is passed structure is valid heatable evap pool
        isHeated = true;

        this.writeCustomData(predicateID, (buf) -> {
            buf.writeBoolean(isHeated);
            buf.writeInt(coilStateMeta);
        });

        return true;
    }

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
        // This is here so that it automatically updates the dimensions once a second if it isn't formed
        // hope this doesn't put too much of a toll on TPS - It really should not
        if (!this.isStructureFormed()) {
            reinitializeStructurePattern();
        }

        super.checkStructurePattern();

        //only do check every 4 seconds while structure is formed
        if ((tickTimer/20 & 3) == 0 && structurePattern.getError() == null) {
            handleCoilCheckResult(coilPatternCheck(false));
        }

        if (structurePattern.getError() != null) {
            SusyLog.logger.atError().log(structurePattern.getError().getErrorInfo());
        }

        /*
        if (structurePattern == null || structurePattern.getError() == null) return; // don't continue if these values aren't valid
        String errorString = structurePattern.getError().getErrorInfo();

        boolean isCoilRelated = errorString.contains("Coil"); //if upcoming return is passed then isCoilRelated indicates if issue is coil or bed

        //exit if current error is unrelated to relevant checks, or if checks have already been performed such that no action is needed. Do check every 5 seconds just in case caught in false loop as well
        if (!isCoilRelated && !errorString.contains("bed")) return;

        SusyLog.logger.atError().log(errorString);
        SusyLog.logger.atError().log("contains Coil: " + isCoilRelated + " contains bed: " + errorString.contains("bed"));

        //if errorPos is null this error is new and must have coords pulled
        if (errorPos == null) {
            //get coord info starting indices
            int eX = errorString.lastIndexOf("x=");
            int eY = errorString.lastIndexOf("y="); //would love to make this search more efficient, but apparently this method takes any passed argument as a new endpoint. Would be cool to set the start too
            int eZ = errorString.lastIndexOf("z=");

            SusyLog.logger.atError().log("eX substr pos: " + eX + " substr at eX: " + errorString.substring(eX) +  " eY substr pos: " + eY + " eZ substr pos: " + eZ);

            //get coords from starting string starting indices (substr returns chars on [start, end))
            eX = Integer.decode(errorString.substring(eX +2, eY -2));
            eY = Integer.decode(errorString.substring(eY +2, eZ -2));
            eZ = Integer.decode(errorString.substring(eZ +2, errorString.length() -3)); // z coord ends before }). at end of string

            SusyLog.logger.atError().log("\neX: " + eX + " eY: " + eY + " eZ: " + eZ);

            errorPos = new BlockPos.MutableBlockPos(eX, eY, eZ);
        }

        if (isCoilRelated) {
            if (!isValidCoil(getWorld().getBlockState(errorPos))) return; //do not perform any logic if error is valid
        } else {
            if (!isValidGround(getWorld().getBlockState(errorPos))) return;
        }

        SusyLog.logger.atError().log("[0] invalidCenterPos: " + ((invalidCenterPos == null) ? "null" : invalidCenterPos.toString()) + ", initializedToNull: " + initializedToNull);

        //if is coil issue and null is due to lack of initialization
        if (invalidCenterPos == null && !initializedToNull) {
            //initialize invalidCenterPos
            handleCoilCheckResult(coilPatternCheck(!isCoilRelated));
            SusyLog.logger.atError().log("[1] invalidCenterPos: " + ((invalidCenterPos == null) ? "null" : invalidCenterPos.toString()) + ", initializedToNull: " + initializedToNull);
        }
        //check again once initialization has been done, or handle case where no initialization was done in above conditional block
        if (invalidCenterPos == null && initializedToNull) {
            removeError(); //error coil related, but no error found when checked "manually"
            return;
        }

        //if custom method found error in same location as reported error do not erase error
        if (invalidCenterPos.equals(errorPos)) {
            return;
        } else if ((isCoilRelated && !isValidCoil(getWorld().getBlockState(invalidCenterPos))) || (!isCoilRelated && !isValidGround(getWorld().getBlockState(invalidCenterPos)))) {
            return;
        }

        //if there is disagreement between reported error and found error check if manually checked position is still erroring
        handleCoilCheckResult(coilPatternCheck(isCoilRelated)); //set next error pos as either
        SusyLog.logger.atError().log("[2] invalidCenterPos: " + ((invalidCenterPos == null) ? "null" : invalidCenterPos.toString()) + ", initializedToNull: " + initializedToNull);

        //remove error if no additional errors were found, otherwise repeating logic with newly located error
        if (invalidCenterPos == null) {
            removeError();
        }
         */
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
    public void addInformation(ItemStack stack, World player, List<String> tooltip, boolean advanced) {
        if (TooltipHelper.isShiftDown()) {
            tooltip.add(I18n.format("gregtech.machine.evaporation_pool.tooltip.structure_info", maxColumns, maxColumns) + "\n");
        }
    }

    public BlockPos.MutableBlockPos getCorner(boolean isClose) {
        BlockPos.MutableBlockPos corner = new BlockPos.MutableBlockPos(getPos());
        EnumFacing back = getFrontFacing().getOpposite();
        EnumFacing left = getFrontFacing().rotateY(); //left if you're looking at controller

        corner.move(EnumFacing.UP); //place on level of fluid in pool
        if (isClose) {
            corner.move(left, controllerPosition); //line up with leftmost column of interior
            corner.move(back, 2); //place in interior
        } else {
            corner.move(left.getOpposite(), columnCount - controllerPosition -1); //line up w/ furthest right interior column
            corner.move(back, rowCount +1); //move onto last row
        }

        return corner;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderOrientedState(renderState, translation, pipeline, getFrontFacing(), recipeMapWorkable.isActive(), recipeMapWorkable.isWorkingEnabled());

        //display fluid in evap pool
        if (recipeMapWorkable.isActive() && isStructureFormed() && currentFluidName != null && FluidRegistry.getFluid(currentFluidName).getStill() != null) {
            EnumFacing back = getFrontFacing().getOpposite();
            BlockPos.MutableBlockPos closerCorner = getCorner(true);
            BlockPos.MutableBlockPos furtherCorner = getCorner(false);
            double minX = Math.min(closerCorner.getX(), furtherCorner.getX());
            double minY = closerCorner.getY(); //will give bottom of block
            double minZ = Math.min(closerCorner.getZ(), furtherCorner.getZ());

            double maxX = Math.max(closerCorner.getX(), furtherCorner.getX());
            double maxY = minY + 0.8125; //should come up to fluid level
            double maxZ = Math.max(closerCorner.getZ(), furtherCorner.getZ());

            Matrix4 offset = translation.copy().translate(back.getXOffset(), -0.3, back.getZOffset());
            CubeRendererState op = Textures.RENDER_STATE.get();
            Textures.RENDER_STATE.set(new CubeRendererState(op.layer, CubeRendererState.PASS_MASK, op.world));
            Textures.renderFace(renderState, offset,
                    ArrayUtils.addAll(pipeline, new LightMapOperation(240, 240), new ColourOperation(0xFFFFFFFF)),
                    EnumFacing.UP, Cuboid6.full,
                    TextureUtils.getTexture(FluidRegistry.getFluid(currentFluidName).getStill()), BlockRenderLayer.TRANSLUCENT);
            Textures.RENDER_STATE.set(op);
        }
    }

    @Override
    public void update() {
        super.update();

        //ensure timer is non-negative by anding sign bit with 0
        tickTimer = tickTimer & 0b01111111111111111111111111111111;

        //should skip/cost an extra tick the first time and then anywhere from 1-19 when rolling over. Determines exposedblocks
        if (tickTimer % 20 == 0 && tickTimer != 0) {
            //no sunlight heat generated when raining. May be incongruent with partial exposure to sun, but oh well
            if (getWorld().isRainingAt(getPos().offset(getFrontFacing().getOpposite(), 2))) { exposedBlocks = 0; }
            //checks for ow and skylight access to prevent beneath portal issues (-1 = the Nether, 0 = normal world)
            else if (getWorld().provider.getDimension() == 0) {
                int row = tickTimer/columnCount; //going left to right, top to bottom checking skylight access.
                int col = tickTimer - row * columnCount; //effectively doing ticktimer % colcount

                //places blockpos for skycheck into correct position. Row counts from furthest to closest (kinda inconsistent but oh well)
                BlockPos.MutableBlockPos skyCheckPos = new BlockPos.MutableBlockPos(getPos().offset(getFrontFacing().getOpposite(), rowCount - row +1)); //place on correct row
                skyCheckPos.move(getFrontFacing().rotateY(), controllerPosition); //move to the furthest left
                skyCheckPos.move(getFrontFacing().rotateYCCW(), col); //traverse down row

                //one exposed block found which can contribute to energy from sunlight
                if (getWorld().canBlockSeeSky(skyCheckPos) && getWorld().isDaytime()) { ++exposedBlocks; }
            }

        } //finish once a ~second check

        /*
            if we assume a solar boiler to have an approximately 1m^2 "collecting" surface, and take the hp to be less inefficient,
            one can approximate the J to EU conversion via 18L/t -> 9EU/t -> 180EU/s -> 1000J/180EU ~= 6J in one EU.
            Assuming solar actually gets slightly less sunlight than 1m^2 conversion may be ~= 5, but potential inefficiencies
            make it unclear the specific amount. Just going to round to one sig fig and leave it at 10J -> 1EU
         */

        if (tickTimer % 100 == 0) {
            SusyLog.logger.atFatal().log("I am still alive at " + getPos());
            SusyLog.logger.atError().log("columnCount: " + columnCount + ", controllerPosition: " + controllerPosition + ", rowCount: " + rowCount);
            SusyLog.logger.atError().log("isHeated: " + isHeated + ", kiloJoules: " + kiloJoules + ", tickTimer: " + tickTimer + ", coilStateMeta: " + coilStateMeta);
        }

        if (this.isActive()) {
            //get energy from sunlight when attempting to run recipe
            if (tickTimer % 20 == 0) kiloJoules += exposedBlocks; //1kJ/s /m^2

            //if world is clientside do custom rendering
            if (getWorld().isRemote) {
                evaporationParticles();
            }




        }

        ++tickTimer;

        //store relevant values
        this.writeCustomData(energyValuesID, buf -> {
            buf.writeInt(exposedBlocks);
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
        data.setInteger("kiloJoules", this.kiloJoules);
        data.setInteger("tickTimer", this.tickTimer);
        data.setInteger("coilStateMeta", this.coilStateMeta);
        data.setString("currentFluidName", this.currentFluidName);
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
        if (data.hasKey("kiloJoules")) {
            this.kiloJoules = data.getInteger("kiloJoules");
        }
        if (data.hasKey("tickTimer")) {
            this.tickTimer = data.getInteger("tickTimer");
        }
        if (data.hasKey("coilStateMeta")) {
            this.coilStateMeta = data.getInteger("coilStateMeta");
        }
        if (data.hasKey("currentFluidName")) {
            this.currentFluidName = data.getString("currentFluidName");
        }
        SusyLog.logger.atFatal().log("before reinit");
        SusyLog.logger.atError().log("columnCount: " + columnCount + ", controllerPosition: " + controllerPosition + ", rowCount: " + rowCount);
        SusyLog.logger.atError().log("isHeated: " + isHeated + ", kiloJoules: " + kiloJoules + ", tickTimer: " + tickTimer + ", coilStateMeta: " + coilStateMeta);
        reinitializeStructurePattern();
        SusyLog.logger.atFatal().log("after reinit");
        SusyLog.logger.atError().log("columnCount: " + columnCount + ", controllerPosition: " + controllerPosition + ", rowCount: " + rowCount);
        SusyLog.logger.atError().log("isHeated: " + isHeated + ", kiloJoules: " + kiloJoules + ", tickTimer: " + tickTimer + ", coilStateMeta: " + coilStateMeta);
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
        buf.writeInt(this.kiloJoules);
        buf.writeInt(this.tickTimer);
        buf.writeInt(this.coilStateMeta);
        buf.writeString(this.currentFluidName);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.columnCount = buf.readInt();
        this.rowCount = buf.readInt();
        this.controllerPosition = buf.readInt();
        this.isHeated = buf.readBoolean();
        this.exposedBlocks = buf.readInt();
        this.kiloJoules = buf.readInt();
        this.tickTimer = buf.readInt();
        this.coilStateMeta = buf.readInt();
        this.currentFluidName = buf.readString(100); //probably long enough to cover all fluids
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
        } else if (dataId == energyValuesID) {
            this.exposedBlocks = buf.readInt();
            this.kiloJoules = buf.readInt();
            this.tickTimer = buf.readInt();
        } else if (dataId == fluidNameID) {
            this.currentFluidName = buf.readString(100);
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

        //place pos in closest leftmost corner. For some reason getPos rounds X pos of controller up and Z pos of controller down
        final BlockPos pos = this.getPos().offset(left, controllerPosition +1).offset(back, 2);

        if (tickTimer % 100 == 0) {
            SusyLog.logger.atFatal().log("I am about to generate particles and am at " + getPos());
            SusyLog.logger.atError().log("columnCount: " + columnCount + ", controllerPosition: " + controllerPosition + ", rowCount: " + rowCount);
            SusyLog.logger.atError().log("isHeated: " + isHeated + ", kiloJoules: " + kiloJoules + ", tickTimer: " + tickTimer + ", coilStateMeta: " + coilStateMeta);
        }

        //Spawn number of particles on range [1, 3 * colCount * rowCount/9] (~3 particles for every 3x3 = 9m^2)
        for (int i = 0; i < columnCount * rowCount/3; i++) {
            //either single line along x axis intersects all rows, or it intersects all columns. Same applies to z axis. getXOffset indicates +, -, or "0" direction of facing for given axis
            float xLength = (back.getXOffset() * rowCount) + (left.getXOffset() * columnCount * -1); //we start on leftmost closest corner and want to go back and to the right
            float zLength = (back.getZOffset() * rowCount) + (left.getZOffset() * columnCount * -1); //so we invert the sign of the left offsets to effectively get right displacement

            if (tickTimer % 100 == 0) SusyLog.logger.atError().log("xLength: " + xLength + ", zLength: " + zLength + ", pos: " + pos + ", controller pos: " + getPos());

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
}
