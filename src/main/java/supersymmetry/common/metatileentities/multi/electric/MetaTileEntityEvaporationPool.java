package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockWireCoil;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import supersymmetry.api.SusyLog;
import supersymmetry.api.capability.impl.NoEnergyMultiblockRecipeLogic;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockEvaporationBed;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.function.Supplier;

import static gregtech.api.GregTechAPI.HEATING_COILS;
import static gregtech.api.block.VariantActiveBlock.ACTIVE_DEPRECATED;

public class MetaTileEntityEvaporationPool extends RecipeMapMultiblockController {

    /*
        For future reference: "((IGregTechTileEntity)world.getTileEntity(pos)).getMetaTileEntity() instanceof IMultiblockAbilityPart"
        is the way ceu gets mte from te. You might also try using MetaTileEntityHolder.
     */

    static final int maxColumns = 12; //two edge layers on either side, shouldn't exceed chunk boundary at max size

    protected int columnCount = 1; //number of columns in row of controller (1 -> EDGEself(controller)EDGE, 2 -> EsCOLUMNe)
    public static final int columnCountID = 104;

    protected int rowCount = 1; //number of rows where controller is placed on "edge" row
    public static final int rowCountID = 108;

    protected int controllerPosition = 0; //column placement from left to right, where 0 = one from edge [ESCCCCE]
    public static final int controllerPositionID = 112;

    boolean isHeated = false;
    public static final int isHeatedID = 113;

    int exposedBlocks = 0;
    public static final int exposedBlocksID = 116;

    //about 1000J/s on a sunny day for 1/m^2 of area
    int kiloJoules = 0;
    public static final int kiloJoulesID = 120;

    int tickTimer = 0;
    public static final int tickTimerID = 124;

    int coilStateMeta;
    public static final int coilStateMetaID = 126;



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
        SusyLog.logger.atFatal().log("its zalgover; invalidating structure");
        this.isHeated = false;
        this.exposedBlocks = 0;
        this.kiloJoules = 0;
        this.tickTimer = 0;
    }

    public boolean updateStructureDimensions() {
        SusyLog.logger.atFatal().log("I am checking the structure's dimensions");

        isHeated = false; //check for coils must be redone

        World world = getWorld();
        EnumFacing front = this.getFrontFacing();
        EnumFacing back = front.getOpposite();
        EnumFacing right = front.rotateYCCW();
        EnumFacing left = right.getOpposite(); //left as if you were looking at it, not controller's left

        SusyLog.logger.atError().log("front: " + front.toString() + ", left: " + left.toString());

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
        this.writeCustomData(columnCountID, (buf) -> {
            buf.writeInt(columnCount);
        });

        this.writeCustomData(rowCountID, (buf) -> {
            buf.writeInt(rowCount);
        });

        this.writeCustomData(controllerPositionID, (buf) -> {
            buf.writeInt(controllerPosition);
        });

        SusyLog.logger.atError().log("Valid Structure; Finished Checking: \n\n");
        SusyLog.logger.atError().log("columnCount: " + columnCount + ", controllerPosition: " + controllerPosition + ", rowCount: " + rowCount);
        SusyLog.logger.atError().log("lDist: " + lDist + ", rDist: " + rDist + ", bDist: " + bDist);

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
                    SusyLog.logger.atFatal().log("01 row: " + (i +1)/rowCount);
                } else { //if j % 4 == 3, then do closer crossover column
                    //if on first row then use "C" [1], otherwise use "G" [0] (0 is only number which is non neg after * -1. this means bit flip keeps most sig as 1)
                    containerRowBuilder.replace(j, j +1, flooring[(~(i * -1)) >>> 31]);
                    SusyLog.logger.atFatal().log("11 row: " + ((~i) >>> 31) + ", ~i: " + ~i);
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
            ' ' for air block where hypothetical water would be.
            'S' for self, or controller.
            'C' for "container blocks" that can be coils, with coils required for powered heating.
            'G' for ground blocks which must be some non coil block

         */

        SusyLog.logger.atFatal().log("I am checking the structure pattern");

        // return the default structure, even if there is no valid size found
        // this means auto-build will still work, and prevents terminal crashes.
        if (getWorld() != null) updateStructureDimensions();

        // these can sometimes get set to 0 when loading the game, breaking JEI (Apparently; text from cleanroom impl)
        if (columnCount < 1) columnCount = 1;
        if (rowCount < 1) rowCount = 1;

        //abstracted away construction of center rows for later use
        String[] containerRows = centerRowsPattern();

        for (int i = 0; i < rowCount; ++i) {
            SusyLog.logger.atError().log(containerRows[i]);
        }

        FactoryBlockPattern pattern;

        //do first two rows including controller row and row behind controller
        pattern = FactoryBlockPattern.start()
                .aisle(repeat("E", columnCount +4), repeat(" ", columnCount +4))
                .aisle(repeat("E",columnCount +4), " ".concat(repeat("E",columnCount +2)).concat(" "));

        //place all generated aisles (rows stored closer to further, this wants them further to closer) and save to pattern explicitly (unsure if the explicit assignment is necessary; probably no harm in being safe)
        for (int i = 0; i < rowCount; ++i) { pattern = pattern.aisle(containerRows[rowCount -1 -i], " E".concat(repeat("#", columnCount).concat("E "))); }

        SusyLog.logger.atError().log("columnCount: " + columnCount + ", controllerPosition: " + controllerPosition + ", rowCount: " + rowCount);
        SusyLog.logger.atError().log("Controller Row: " + repeat("E",controllerPosition +2).concat("S").concat(repeat("E",columnCount +1 -controllerPosition)), repeat(" ", columnCount +4));

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
            return state == SuSyBlocks.EVAPORATION_BED.getState(BlockEvaporationBed.EvaporationBedType.DIRT);
        }, supplier);
    }

    //ensures coil pattern is either entirely there or completely absent for structure to be valid. Sets isHeated accordingly
    protected TraceabilityPredicate isContainer() {
        //store all valid blockinfo related to different materials
        BlockInfo[] blockInfos = new BlockInfo[HEATING_COILS.size()];
        for (int i = 0; i < blockInfos.length; ++i) {
            BlockWireCoil.CoilType type = BlockWireCoil.CoilType.values()[i]; //type of coil
            blockInfos[i] = new BlockInfo(MetaBlocks.WIRE_COIL.getState(type)); //associated blockstate as blockInfo instance
        }

        Supplier<BlockInfo[]> supplier = () -> blockInfos; //return blockInfos as info supplier

        //returns true if either "ground" blockstate or some type of coil
        return new TraceabilityPredicate(blockWorldState -> {
            IBlockState state = blockWorldState.getBlockState();

            SusyLog.logger.atError().log("Checking container block with isHeated == " + isHeated + " and blockstate: " + state.toString());

            //if isHeated is true check on all coil blocks have been performed and target may be assessed individually
            if (isHeated) {
                //compare string value of querying state and stored/known state
                if (state.toString().equals(MetaBlocks.WIRE_COIL.getStateFromMeta(coilStateMeta).toString())) return true;

                return false;
            }

            //if block is not coil structure do ground check
            if (!MetaBlocks.WIRE_COIL.getBlockState().getValidStates().contains(state)) return isGround().test(blockWorldState);

            //first coil block has been found; ensure full coil pattern is present so that future validation checks may be skipped
            String[] centerPattern = centerRowsPattern();
            IBlockState coilState = null;

            EnumFacing right = getFrontFacing().rotateYCCW(); //right if you are looking at controller
            EnumFacing back = getFrontFacing().getOpposite(); //further from front of controller

            //get blockpos of first row, leftmost center block
            BlockPos.MutableBlockPos coilPos = new BlockPos.MutableBlockPos(getPos().offset(back, 2)); //place on first row
            coilPos.move(right.getOpposite(), controllerPosition +1); //move to the furthest left center block +1 (looking at controller) to account for immediate right deviation in j loop

            SusyLog.logger.atError().log("initial coords: " + coilPos.toString());

            //traverse center pattern rows (closer to further)
            for (int i = 0; i < rowCount; ++i) {
                //traverse center pattern columns in 'i'th row, skipping edges
                for (int j = 2; j < columnCount + 2; ++j) {
                    coilPos.move(right); //go right one first to allow for continue to be used later

                    SusyLog.logger.atError().log("Examining block at [" + i + "][" + j + "]: " + centerPattern[i].charAt(j) + "; coords: " + coilPos.toString() +  " with state: " + getWorld().getBlockState(coilPos).toString());

                    if (centerPattern[i].charAt(j) != 'C') continue; //skip non coil blocks

                    //store state of first coil. Might do weird stuff if meta of coilState > 7, as jei error-ed when trying to display them even though they are same material type
                    if (coilState == null) {
                        coilState = getWorld().getBlockState(coilPos); //establish coil type from first coil
                        if (MetaBlocks.WIRE_COIL.getBlockState().getValidStates().contains(coilState)) continue; //if first coil position found ensure it is valid coil and continue if it is
                        SusyLog.logger.atError().log("Assigning coil state at [" + i + "][" + j + "]: " + centerPattern[i].charAt(j) + "; coords: " + coilPos.toString() +  " to saved state: " + coilState.toString());
                    }
                    else if (getWorld().getBlockState(coilPos).toString().equals(coilState.toString())) continue; //proceed only if current coil is exact same as previous coil(s). equals seems to fail, so a string comparison works best)

                    SusyLog.logger.atError().log("About to error in coil check with char at [" + i + "][" + j + "]: " + centerPattern[i].charAt(j) + "; current coil: " + getWorld().getBlockState(coilPos) + ", saved state: " + coilState.toString());

                    //current coilpos block is either not a coil (first coil position), or it is not identical to previous coil(s)
                    isHeated = false;
                    return false; //indicate failure of solid coil structure
                }

                coilPos.move(back); //go back one row
                coilPos.move(right.getOpposite(), columnCount); //reset to leftmost center position +1 unit left due to order of traversal inside nested loop
            }


            //if check is passed structure is valid heatable evap pool
            isHeated = true;
            coilStateMeta = coilState.getBlock().getMetaFromState(coilState);

            this.writeCustomData(isHeatedID, (buf) -> {
                buf.writeBoolean(isHeated);
            });

            this.writeCustomData(coilStateMetaID, (buf) -> {
                buf.writeInt(coilStateMeta);
            });



            return true;

        }, supplier);
    }

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
    public void update() {
        super.update();

        //ensure timer is non-negative
        tickTimer = tickTimer & 0b1000000000000000000000;

        //should skip/cost an extra tick the first time and then anywhere from 1-19 when rolling over. Applies sunlight energy gain
        if (tickTimer % 20 == 0 && tickTimer != 0) {

            //checks for ow and skylight access to prevent beneath portal issues (-1 = the Nether, 0 = normal world)
            if (getWorld().provider.getDimension() == 0) {
                int row = tickTimer/columnCount; //going left to right, top to bottom checking skylight access.
                int col = tickTimer - row * columnCount; //effectively doing ticktimer % colcount

                //places blockpos for skycheck into correct position. Row counts from furthest to closest (kinda inconsistent but oh well)
                BlockPos.MutableBlockPos skyCheckPos = new BlockPos.MutableBlockPos(getPos().offset(getFrontFacing().getOpposite(), rowCount - row +1)); //place on correct row
                skyCheckPos.move(getFrontFacing().rotateY(), controllerPosition); //move to the furthest left
                skyCheckPos.move(getFrontFacing().rotateYCCW(), col); //traverse down row

                //one exposed block found which can contribute to energy from sunlight
                if (getWorld().canBlockSeeSky(skyCheckPos) && getWorld().isDaytime()) { ++exposedBlocks; }

                //store exposedBlock count
                this.writeCustomData(exposedBlocksID, buf -> {
                    buf.writeInt(exposedBlocks);
                });
            }

        } //finish once a ~second check

        /*
            if we assume a solar boiler to have an approximately 1m^2 "collecting" surface, and take the hp to be less inefficient,
            one can approximate the J to EU conversion via 18L/t -> 9EU/t -> 180EU/s -> 1000J/180EU ~= 6J in one EU.
            Assuming solar actually gets slightly less sunlight than 1m^2 conversion may be ~= 5, but potential inefficiencies
            make it unclear the specific amount. Just going to round to one sig fig and leave it at 10J -> 1EU
         */

        if (this.getOffsetTimer() % 100 == 0) {
            SusyLog.logger.atFatal().log("I am still alive");
        }

        if (this.isActive()) {
            //get energy from sunlight when attempting to run recipe
            kiloJoules += exposedBlocks; //1kJ/s /m^2

            //if world is clientside
            if (getWorld().isRemote) {
                evaporationParticles();
            }
        }

        ++tickTimer;

        this.writeCustomData(tickTimerID, buf -> {
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
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == columnCountID) {
            this.columnCount = buf.readInt();
            //I think this is where you would do any updates resulting from data modifications
        } else if (dataId == rowCountID) {
            this.rowCount = buf.readInt();
        } else if (dataId == controllerPositionID) {
            this.controllerPosition = buf.readInt();
        } else if (dataId == isHeatedID) {
            this.isHeated = buf.readBoolean();
        } else if (dataId == exposedBlocksID) {
            this.exposedBlocks = buf.readInt();
        } else if (dataId == kiloJoulesID) {
            this.kiloJoules = buf.readInt();
        } else if (dataId == tickTimerID) {
            this.tickTimer = buf.readInt();
        } else if (dataId == coilStateMetaID) {
            this.coilStateMeta = buf.readInt();
        }
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @SideOnly(Side.CLIENT)
    private void evaporationParticles() {
        BlockPos pos = this.getPos();
        EnumFacing facing = this.getFrontFacing().getOpposite();

        for (int i = 0; i < 10; i++) {
            float xPos = facing.getXOffset() * 15F + pos.getX() + 27 * (GTValues.RNG.nextFloat() - 0.5F);
            float yPos = pos.getY() + 0.25F;
            float zPos = facing.getZOffset() * 15F + pos.getZ() + 27 * (GTValues.RNG.nextFloat() - 0.5F);

            float ySpd = facing.getYOffset() * 0.1F + 0.4F + 0.2F * GTValues.RNG.nextFloat();
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
}
