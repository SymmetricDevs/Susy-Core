package supersymmetry.common.metatileentities.single.rocket;

import crafttweaker.api.block.IBlock;
import gregtech.api.block.VariantBlock;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.IDistinctBusController;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.impl.ItemHandlerList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.Widget;
import gregtech.api.gui.resources.TextureArea;
import gregtech.api.gui.widgets.*;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.metatileentity.multiblock.IProgressBarMultiblock;
import gregtech.api.pattern.BlockWorldState;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.Gui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.event.terraingen.OreGenEvent;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import scala.xml.dtd.impl.Base;
import supersymmetry.api.capability.impl.ScannerLogic;
import supersymmetry.api.util.StructAnalysis;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.rocketry.BlockCombustionChamber;
import supersymmetry.common.metatileentities.multi.electric.MetaTileEntityBuildingCleanroom;
import supersymmetry.SuSyValues;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static gregtech.api.metatileentity.multiblock.MultiblockControllerBase.blocks;
import static supersymmetry.api.blocks.VariantDirectionalRotatableBlock.FACING;

public class MetaTileEntityComponentScanner extends MetaTileEntity implements ICleanroomReceiver, IControllable {
    private MetaTileEntityBuildingCleanroom linkedCleanroom;
    private final ScannerLogic scannerLogic;
    private float scanDuration = 0;

    public StructAnalysis struct;
    public MetaTileEntityComponentScanner(ResourceLocation mteId) {
        super(mteId);
        struct = new StructAnalysis(getWorld());
        importItems = new ItemStackHandler();
        scannerLogic = new ScannerLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityComponentScanner(this.metaTileEntityId);
    }

    public void scanPart() {
        if (linkedCleanroom == null && !linkedCleanroom.isClean()) {
            struct.status = StructAnalysis.BuildError.UNCLEAN;
            return;
        }
        AxisAlignedBB interior = linkedCleanroom.getInteriorBB();
        int solidBlocks = 0;
        ArrayList<BlockPos> blockList = struct.getBlocks(getWorld(), interior, true);

        if (blockList == null) { //error propagated
            return;
        }
        else if (blockList.isEmpty()) {
            this.struct.status = StructAnalysis.BuildError.EMPTY;
            return;
        }

        scanDuration = (float)blockList.size()/3;

        Set<BlockPos> blocksConnected = struct.getBlockConn(interior, blockList.get(0));

        if (blocksConnected.size() != blockList.size()) {
            this.struct.status = StructAnalysis.BuildError.DISCONNECTED;
            return;
        }

        // Block-by-block analysis
        for (BlockPos bp: blockList) {
            IBlockState state = getWorld().getBlockState(bp);
        }

        Set<BlockPos> exterior = struct.checkHull(interior, blocksConnected, false);
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
        boolean hasAir = struct.status != StructAnalysis.BuildError.HULL_FULL;
        if (blockStream.anyMatch(engineDetect)) {
            Set<BlockPos> nozzle = struct.getOfBlockType(blocksConnected,SuSyBlocks.ROCKET_NOZZLE)
                    .collect(Collectors.toSet());
            if (nozzle.isEmpty()) {
                struct.status = StructAnalysis.BuildError.NO_NOZZLE;
                return;
            }
            ArrayList<Integer> areas = new ArrayList<>();
            AxisAlignedBB bb = struct.getBB(nozzle);
            for (int i = (int)bb.minY; i <= (int)bb.maxY; i++) {
                Set<BlockPos> airLayer = struct.getLayerAir(getWorld(), bb, i);
                if (airLayer == null) {
                    return;
                }
                Set<BlockPos> airPerimeter = struct.getPerimeter(airLayer,StructAnalysis.layerVecs);
                if (airPerimeter.size()*3 > airLayer.size()) {
                    struct.status = StructAnalysis.BuildError.NOZZLE_MALFORMED;
                    return;
                }
            }
            // One combustion chamber is, I think, reasonable
            List<BlockPos> cChambers = struct.getOfBlockType(blocksConnected,SuSyBlocks.COMBUSTION_CHAMBER)
                    .collect(Collectors.toList());
            if (cChambers.size() != 1) {
                struct.status = StructAnalysis.BuildError.WRONG_NUM_C_CHAMBERS;
                return;
            }
            // Below the chamber: Open space
            BlockPos cChamber = cChambers.get(0);
            Set<BlockPos> pumps = struct.getOfBlockType(struct.getBlockNeighbors(cChamber, StructAnalysis.orthVecs),SuSyBlocks.TURBOPUMP).collect(Collectors.toSet());
            if (!bb.contains(new Vec3d(cChamber))) {
                if (!getWorld().isAirBlock(cChamber.add(0,-1,0))) {
                    struct.status = StructAnalysis.BuildError.NOZZLE_MALFORMED;
                    return;
                }
                IBlockState chamberState = getWorld().getBlockState(cChamber);
                int pumpNum = ((BlockCombustionChamber.CombustionType)
                        (((VariantBlock)chamberState.getBlock()).getState(chamberState))).getMinPumps();
                if (pumps.size() != pumpNum) {
                    struct.status = StructAnalysis.BuildError.WRONG_NUM_PUMPS;
                    return;
                }
                for (BlockPos pumpPos : pumps) {
                    EnumFacing dir = getWorld().getBlockState(pumpPos).getValue(FACING);
                    if (!dir.equals(EnumFacing.DOWN)&&!pumpPos.add(dir.getOpposite().getDirectionVec()).equals(cChamber)) {
                        struct.status = StructAnalysis.BuildError.WEIRD_PUMP;
                        return;
                    }
                }
            } else {
                struct.status = StructAnalysis.BuildError.C_CHAMBER_INSIDE;
                return;
            }
            struct.status = StructAnalysis.BuildError.SUCCESS;
            Set<BlockPos> engineBlocks = new HashSet<>(nozzle);
            engineBlocks.addAll(pumps);
            engineBlocks.add(cChamber);
            if (engineBlocks.size() < blocksConnected.size()) {
                struct.status = StructAnalysis.BuildError.EXTRANEOUS_BLOCKS;
                return;
            }
            // Remaining: Get perimeter of engine,
        } else if (blockStream.anyMatch(controlPodDetect)) {

        } else if (blockStream.anyMatch(fuelTankDetect)) {
            if (exterior == null) {
                return; // error was set in the hull detector
            }
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




    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return this.createGUITemplate(entityPlayer).build(this.getHolder(), entityPlayer);
    }
    private void handleScan(Widget.ClickData click) {
        if (linkedCleanroom==null) {
            struct.status= StructAnalysis.BuildError.UNCLEAN;
        }
        if (scannerLogic.isActive()&&this.isWorkingEnabled()) {
            scannerLogic.setWorkingEnabled(true);
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
        builder.widget(new ClickButtonWidget(68,46,54,18,new TextComponentTranslation("gregtech.machine.component_scanner.scan_button").getUnformattedComponentText(),this::handleScan))
                .slot(importItems,0,90,95,GuiTextures.SLOT);


        builder.bindPlayerInventory(entityPlayer.inventory, 125);
        return builder;
    }


    private void handleDisplayClick(String s, Widget.ClickData clickData) {

    }

    private void addBarHoverText(List<ITextComponent> list, int i) {

    }

    private void addWarningText(List<ITextComponent> iTextComponents) {

    }

    private void addDisplayText(List<ITextComponent> iTextComponents) {

    }

    private void addErrorText(List<ITextComponent> iTextComponents) {
        if (struct.status== StructAnalysis.BuildError.SUCCESS&&scannerLogic.getProgressPercent()!=1) {
            iTextComponents.add(new TextComponentTranslation(StructAnalysis.BuildError.UNSCANNED.getCode()));
        } else {
            iTextComponents.add(new TextComponentTranslation(struct.status.getCode()));
        }
    }

    @Override
    public @Nullable ICleanroomProvider getCleanroom() {
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
        long resultEnergy = energyContainer.getEnergyStored()-4;
        if (resultEnergy >= 0L && resultEnergy <= energyContainer.getEnergyCapacity()) {
            if (!simulate)
                energyContainer.changeEnergy(-4);
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
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE) {
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        }
        return super.getCapability(capability, side);
    }
}
