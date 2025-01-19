package supersymmetry.common.metatileentities.single.rocket;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.block.VariantBlock;
import gregtech.api.capability.*;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.util.TextComponentUtil;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.block.Block;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import scala.Int;
import supersymmetry.api.capability.impl.ScannerLogic;
import supersymmetry.api.util.DataStorageLoader;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.rocketry.BlockCombustionChamber;
import supersymmetry.common.blocks.rocketry.BlockRocketNozzle;
import supersymmetry.common.blocks.rocketry.BlockTurboPump;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityBuildingCleanroom;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static supersymmetry.api.blocks.VariantDirectionalRotatableBlock.FACING;

public class MetaTileEntityComponentScanner extends MetaTileEntityMultiblockPart implements ICleanroomReceiver, IControllable, IWorkable {
    private final ScannerLogic scannerLogic;
    private float scanDuration = 0;
    private MetaTileEntityBuildingCleanroom  linkedCleanroom;
    private StructAnalysis.BuildStat shownStatus;

    public StructAnalysis struct;
    public MetaTileEntityComponentScanner(ResourceLocation mteId) {
        super(mteId,0); // it kind of is and isn't
        shownStatus = StructAnalysis.BuildStat.UNSCANNED;
        struct = new StructAnalysis(getWorld());
        importItems = new DataStorageLoader(this,is -> {int metaV = SuSyMetaItems.isMetaItem(is);
            return metaV == SuSyMetaItems.DATA_CARD.metaValue || metaV == SuSyMetaItems.DATA_CARD_ACTIVE.metaValue;});
        scannerLogic = new ScannerLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityComponentScanner(this.metaTileEntityId);
    }

    public void scanPart() {
        ((DataStorageLoader)importItems).clearNBT();
        struct = new StructAnalysis(getWorld());
        if (linkedCleanroom == null || !linkedCleanroom.isClean()) {
            struct.status = StructAnalysis.BuildStat.UNCLEAN;
            return;
        }
        AxisAlignedBB interior = linkedCleanroom.getInteriorBB();
        int solidBlocks = 0;
        ArrayList<BlockPos> blockList = struct.getBlocks(getWorld(), interior, true);

        if (blockList == null) { //error propagated
            return;
        }
        else if (blockList.isEmpty()) {
            this.struct.status = StructAnalysis.BuildStat.EMPTY;
            return;
        }

        scanDuration = (blockList.size()+3)/2;
        scannerLogic.setGoalTime(scanDuration);

        Set<BlockPos> blocksConnected = struct.getBlockConn(interior, blockList.get(0));

        if (blocksConnected.size() != blockList.size()) {
            this.struct.status = StructAnalysis.BuildStat.DISCONNECTED;
            return;
        }

        // Block-by-block analysis
        for (BlockPos bp: blockList) {
            IBlockState state = getWorld().getBlockState(bp);
        }

        Tuple<Set<BlockPos>, Set<BlockPos>> exterior = struct.checkHull(interior, blocksConnected, false);
        Predicate<BlockPos> engineDetect = bp -> getWorld().getBlockState(bp).getBlock()
                .equals(SuSyBlocks.COMBUSTION_CHAMBER);
        Predicate<BlockPos> controlPodDetect = bp -> getWorld().getBlockState(bp).getBlock()
                .equals(SuSyBlocks.ROCKET_CONTROL);
        Predicate<BlockPos> fuelTankDetect = bp -> getWorld().getBlockState(bp).getBlock()
                .equals(SuSyBlocks.TANK_SHELL);
        Predicate<BlockPos> fairingDetect = bp -> getWorld().getBlockState(bp).getBlock()
                .equals(SuSyBlocks.FAIRING_HULL);
        Predicate<BlockPos> interstageDetect = bp -> getWorld().getBlockState(bp).getBlock()
                .equals(SuSyBlocks.INTERSTAGE);

        Stream<BlockPos> blockStream = blockList.stream();
        boolean hasAir = struct.status != StructAnalysis.BuildStat.HULL_FULL;

        if (blockStream.anyMatch(engineDetect)) {
            analyzeEngine(blocksConnected);
        } else if (blockStream.anyMatch(controlPodDetect)) {

        } else if (blockStream.anyMatch(fuelTankDetect) && exterior != null && hasAir) {
            Set<BlockPos> allBlocks = struct.getBlocks(interior);

            // Check if all blocks are facing the correct direction and are of the right type
            // Save the fuel capacity & type
            analyzeFuelTank(blocksConnected, exterior);
        } else if (blockStream.anyMatch(fairingDetect)) {

        } else if (blockStream.anyMatch(interstageDetect)) {

        }



        // Identify component type: Fluid port -> tank, hatch -> pf

        // Component analysis

        /* Plan from here on out:
        1. Gather block statistics
        2. Check for unallowed TileEntities (we can't have as many if it's all being modelized)
        3. Identify component purpose:
            a. Payload fairing
                - Distinguishable by material type
                - Attachments only at bottom
                - Holds port
                - Bottom opening is not filled
                - No through-holes:
                    - All partial holes are counted (if two blocks have a midpoint not in a block, there is a partial hole)
                    - All air blocks in the hole are counted
                    - There is no more than one contiguous set of air blocks not inside the hole that has access to the hole!
            b. Life compartment
                - Contains interior space
                - Contains life support TEs
                - Allows for containers
            c. Fuel tank
                - Contains interior space
                - Contains structural blocks
                - Has a port
                - Contains exterior blocks
            d. Engine
                - Specialized blocks for ignition containment
            e. Hull cover
                - Connection blocks (skirts)
                - Particular surface blocks
                - Support blocks
            f. Control room
                - Port
                - Guidance computer (not a tile entity)
                - Seat
        */
    }

    public void analyzeEngine(Set<BlockPos> blocks) {
        Set<BlockPos> nozzle = struct.getOfBlockType(blocks,SuSyBlocks.ROCKET_NOZZLE)
                .collect(Collectors.toSet());
        if (nozzle.isEmpty()) {
            struct.status = StructAnalysis.BuildStat.NO_NOZZLE;
            return;
        }
        ArrayList<Integer> areas = new ArrayList<>();
        AxisAlignedBB bb = struct.getBB(nozzle);
        for (int i = (int)bb.maxY-1; i >= (int)bb.minY; i--) {
            Set<BlockPos> airLayer = struct.getLayerAir(getWorld(), bb, i);
            if (airLayer == null) { // there should be an error here
                struct.status = StructAnalysis.BuildStat.NOZZLE_MALFORMED;
                return;
            }
            Set<BlockPos> airPerimeter = struct.getPerimeter(airLayer,StructAnalysis.layerVecs);
            if ((double)airPerimeter.size() < 3 * Math.sqrt((double)airLayer.size())) { // total guesswork
                struct.status = StructAnalysis.BuildStat.NOZZLE_MALFORMED;
                return;
            }
            areas.add(airLayer.size());
        }

        // For 99% of rocket nozzles, the air layer list should be monotonic decreasing and then monotonic increasing. 3 blocks should be a minimum length under that assumption.
        if (areas.size() < 3) {
            struct.status = StructAnalysis.BuildStat.NOZZLE_MALFORMED;
            return;
        }
        boolean decreasing = false; // this may be changed later
        int throat_area = 1; // ooh what if LittleTiles was abused here?
        int inlet_area = 1;  // at least for now?
        int outlet_area = 0;
        for (int a: areas) {
            if (decreasing && throat_area >= a) {
                throat_area = a;
            } else if (decreasing) {
                decreasing = false;
                outlet_area = a;
            } else if (outlet_area <= a) {
                outlet_area = a;
            } else {
                struct.status = StructAnalysis.BuildStat.NOT_LAVAL;
                return;
            }
        }

        double area_ratio = (double)outlet_area/throat_area;


        // One combustion chamber is, I think, reasonable
        List<BlockPos> cChambers = struct.getOfBlockType(blocks,SuSyBlocks.COMBUSTION_CHAMBER)
                .collect(Collectors.toList());
        if (cChambers.size() != 1) {
            struct.status = StructAnalysis.BuildStat.WRONG_NUM_C_CHAMBERS;
            return;
        }
        // Below the chamber: Open space
        BlockPos cChamber = cChambers.get(0);
        Set<BlockPos> pumps = struct.getOfBlockType(struct.getBlockNeighbors(cChamber, StructAnalysis.orthVecs),SuSyBlocks.TURBOPUMP).collect(Collectors.toSet());
        if (!bb.contains(new Vec3d(cChamber))) {
            if (!getWorld().isAirBlock(cChamber.add(0,-1,0))) {
                struct.status = StructAnalysis.BuildStat.NOZZLE_MALFORMED;
                return;
            }
            IBlockState chamberState = getWorld().getBlockState(cChamber);
            int pumpNum = ((BlockCombustionChamber.CombustionType)
                    (((VariantBlock)chamberState.getBlock()).getState(chamberState))).getMinPumps();
            if (pumps.size() != pumpNum) {
                struct.status = StructAnalysis.BuildStat.WRONG_NUM_PUMPS;
                return;
            }
            for (BlockPos pumpPos : pumps) {
                EnumFacing dir = getWorld().getBlockState(pumpPos).getValue(FACING);
                if (!dir.equals(EnumFacing.DOWN)&&!pumpPos.add(dir.getOpposite().getDirectionVec()).equals(cChamber)) {
                    struct.status = StructAnalysis.BuildStat.WEIRD_PUMP;
                    return;
                }
            }
        } else {
            struct.status = StructAnalysis.BuildStat.C_CHAMBER_INSIDE;
            return;
        }
        struct.status = StructAnalysis.BuildStat.SUCCESS;
        Set<BlockPos> engineBlocks = new HashSet<>(nozzle);
        engineBlocks.addAll(pumps);
        engineBlocks.add(cChamber);
        engineBlocks.addAll(struct.getOfBlockType(blocks,SuSyBlocks.INTERSTAGE).collect(Collectors.toSet()));
        if (engineBlocks.size() < blocks.size()) {
            struct.status = StructAnalysis.BuildStat.EXTRANEOUS_BLOCKS;
            return;
        }
        struct.status = StructAnalysis.BuildStat.SUCCESS;
        modifyItem("area_ratio", Double.valueOf(area_ratio).toString());
        modifyItem("type","engine");
        double mass = 0;
        for (BlockPos block : blocks) {
            mass += getMass(getWorld().getBlockState(block));
        }
        modifyItem("mass", Double.valueOf(mass).toString());
    }

    public void analyzeFuelTank(Set<BlockPos> blocks, Tuple<Set<BlockPos>, Set<BlockPos>> hullData) {
        Set<BlockPos> hullBlocks = hullData.getFirst();
        Set<BlockPos> airBlocks = hullData.getSecond();

        for (BlockPos block : hullBlocks) {
            ArrayList<BlockPos> neighbors = struct.getBlockNeighbors(block,StructAnalysis.orthVecs);
            for (BlockPos neighbor : neighbors) {
                if (airBlocks.contains(neighbor)) {
                    BlockPos invDiff = new BlockPos(struct.diff(block,neighbor));
                    if (!getWorld().getBlockState(block).getValue(FACING).getDirectionVec().equals(invDiff)) {

                    }
                }
            }
        }
    }

    private double getMass(IBlockState state) {
        Block block = state.getBlock();
        if (!(block instanceof VariantBlock<?>)) {
            return 50.0;
        }
        Enum<?> variant = ((VariantBlock<?>) block).getState(state);
        if (block.equals(SuSyBlocks.COMBUSTION_CHAMBER)) {
            return 800+100*switch ((BlockCombustionChamber.CombustionType)variant) {
                case BIPROPELLANT -> 200.0;
                case MONOPROPELLANT -> 150.0;
                case OXIDISER -> 200.00;
            };
        } else if (block.equals(SuSyBlocks.ROCKET_NOZZLE)) {
            return 500+100*switch ((BlockRocketNozzle.NozzleShapeType)variant) {
                case BELL_NOZZLE -> 60.0;
                case PLUG_NOZZLE -> 65.0;
                case EXPANDING_NOZZLE -> 80.0;
            };
        } else if (block.equals(SuSyBlocks.TURBOPUMP)) {
            return 1000+100*switch((BlockTurboPump.HPPType)variant) {
                case BASIC -> 150.0;
            };
        }
        return 50.0;
    }

    protected void modifyItem(String key, String value) {
        ((DataStorageLoader)importItems).mutateItem(key,value);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return this.createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
    }
    private void handleScan(Widget.ClickData click) {
        if (linkedCleanroom==null || !linkedCleanroom.isClean()) {
            struct.status= StructAnalysis.BuildStat.UNCLEAN;
        }
        if (this.isWorkingEnabled()) {
            scannerLogic.setWorkingEnabled(true);
            scannerLogic.setActive(true);
            struct.status = StructAnalysis.BuildStat.SCANNING;
            scanPart();
        }
    }

    private ModularUI.Builder createGUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 198, 208);
        //Spliced from MultiblockWithDisplayBase
        // Display
        builder.image(4, 4, 190, 109, GuiTextures.DISPLAY);

        // single bar
        ProgressWidget progressBar = new ProgressWidget(
                scannerLogic::getProgressPercent,
                4, 115, 190, 7,
                GuiTextures.PROGRESS_BAR_MULTI_ENERGY_YELLOW, ProgressWidget.MoveType.HORIZONTAL)
                .setHoverTextConsumer(list -> addBarHoverText(list, 0));
        builder.widget(progressBar);

        builder.widget(new IndicatorImageWidget(174, 93, 17, 17, GuiTextures.GREGTECH_LOGO_DARK)
                .setWarningStatus(GuiTextures.GREGTECH_LOGO_BLINKING_YELLOW, this::addWarningText)
                .setErrorStatus(GuiTextures.GREGTECH_LOGO_BLINKING_RED, this::addErrorText));

        builder.label(9, 9, getMetaFullName(), 0xFFFFFF);
        builder.widget(new AdvancedTextWidget(9, 20, this::addDisplayText, 0xFFFFFF)
                .setMaxWidthLimit(181)
                .setClickHandler(this::handleDisplayClick));

        // Power Button
        IControllable controllable = getCapability(GregtechTileCapabilities.CAPABILITY_CONTROLLABLE, null);
        if (controllable != null) {
            builder.widget(new ImageCycleButtonWidget(173, 183, 18, 18, GuiTextures.BUTTON_POWER,
                    controllable::isWorkingEnabled, controllable::setWorkingEnabled));
            builder.widget(new ImageWidget(173, 201, 18, 6, GuiTextures.BUTTON_POWER_DETAIL));
        }
        //Scan Button
        builder.widget(new ClickButtonWidget(68,56,54,18,new TextComponentTranslation("gregtech.machine.component_scanner.scan_button").getUnformattedComponentText(),this::handleScan))
                .slot(importItems,0,90,95,GuiTextures.SLOT);


        builder.bindPlayerInventory(entityPlayer.inventory, 125);
        return builder;
    }


    private void handleDisplayClick(String s, Widget.ClickData clickData) {

    }

    private void addBarHoverText(List<ITextComponent> list, int i) {

    }

    private void addWarningText(List<ITextComponent> iTextComponents) {
        if (struct.status==StructAnalysis.BuildStat.UNSCANNED) {
            iTextComponents.add(new TextComponentTranslation(StructAnalysis.BuildStat.UNSCANNED.getCode()));
        }
    }

    private void addErrorText(List<ITextComponent> iTextComponents) {
        if (struct.status!=StructAnalysis.BuildStat.SUCCESS&&struct.status!=StructAnalysis.BuildStat.SCANNING&&struct.status!=StructAnalysis.BuildStat.UNSCANNED) {
            iTextComponents.add(new TextComponentTranslation(StructAnalysis.BuildStat.UNSCANNED.getCode()));
        }
    }

    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, this.getCleanroom()!=null).setWorkingStatus(this.isWorkingEnabled(), this.isActive()).addEnergyUsageLine(linkedCleanroom.getEnergyContainer()).addCustom((tl) -> {
            if (linkedCleanroom!=null) {
                TextComponentTranslation cleanState;
                if (scannerLogic.isActive() || struct.status == StructAnalysis.BuildStat.SCANNING) {
                    tl.add(TextComponentUtil.translationWithColor(TextFormatting.YELLOW, "gregtech.machine.component_scanner.scanning"));
                } else if (shownStatus== StructAnalysis.BuildStat.SUCCESS) {
                    tl.add(TextComponentUtil.translationWithColor(TextFormatting.GREEN, "gregtech.machine.component_scanner.success"));

                } else if (shownStatus == StructAnalysis.BuildStat.UNSCANNED) {
                    tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "gregtech.machine.component_scanner.unscanned"));
                } else {
                    tl.add(TextComponentUtil.translationWithColor(TextFormatting.DARK_RED, "gregtech.machine.component_scanner.failure"));
                    tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, shownStatus.getCode()));
                }
            }

        }).addProgressLine(this.scannerLogic.getProgressPercent());
    }

    @Override
    public @Nullable ICleanroomProvider getCleanroom() {
        return linkedCleanroom;
    }

    @Override
    public @Nullable MultiblockControllerBase getController() {
        return linkedCleanroom;
    }

    @Override
    public void setCleanroom(ICleanroomProvider iCleanroomProvider) {
        if (iCleanroomProvider instanceof MetaTileEntityBuildingCleanroom)
            linkedCleanroom = (MetaTileEntityBuildingCleanroom)iCleanroomProvider;
    }

    @Override
    public boolean isWorkingEnabled() {
        return scannerLogic.isWorkingEnabled()&&linkedCleanroom!=null&&linkedCleanroom.isWorkingEnabled();
    }

    public void setWorkingEnabled(boolean isActivationAllowed) {
        this.scannerLogic.setWorkingEnabled(isActivationAllowed);
    }

    public boolean drainEnergy(boolean simulate) {
        if (linkedCleanroom == null) {
            return false;
        }
        IEnergyContainer energyContainer = linkedCleanroom.getEnergyContainer();
        long resultEnergy = energyContainer.getEnergyStored()-scannerLogic.getInfoProviderEUt();
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.changeEnergy(-scannerLogic.getInfoProviderEUt());
            return true;
        }
        return false;
    }

    public void update() {
        if (!getWorld().isRemote) {
            this.scannerLogic.updateLogic();
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE) {
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        } else {
            return capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE ? GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this) : super.getCapability(capability, side);
        }
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), this.isActive(), this.isWorkingEnabled());
    }

    @Override
    public ICubeRenderer getBaseTexture() {
        return Textures.VOLTAGE_CASINGS[4];
    }

    public ICubeRenderer getFrontOverlay() {
        return Textures.RESEARCH_STATION_OVERLAY;
    }

    public void finishScan() {
        if (struct.status == StructAnalysis.BuildStat.SUCCESS) {
            ((DataStorageLoader)importItems).setImageType(8); // is this cursed? yes
        }
        shownStatus = struct.status;
    }

    @Override
    public int getProgress() {
        return (int)scannerLogic.getProgress();
    }

    @Override
    public int getMaxProgress() {
        return (int)scannerLogic.getMaxProgress();
    }

    @Override
    public boolean isActive() {
        return scannerLogic.isActive();
    }
}
