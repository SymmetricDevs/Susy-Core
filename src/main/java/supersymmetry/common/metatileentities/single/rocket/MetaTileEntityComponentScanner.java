package supersymmetry.common.metatileentities.single.rocket;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.ICleanroomProvider;
import gregtech.api.metatileentity.multiblock.ICleanroomReceiver;
import gregtech.api.pattern.BlockWorldState;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraftforge.event.terraingen.OreGenEvent;
import org.jetbrains.annotations.Nullable;
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

public class MetaTileEntityComponentScanner extends MetaTileEntity implements ICleanroomReceiver {
    private MetaTileEntityBuildingCleanroom linkedCleanroom;


    public StructAnalysis struct;
    public MetaTileEntityComponentScanner(ResourceLocation mteId) {
        super(mteId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityComponentScanner(this.metaTileEntityId);
    }

    public void scanPart() {
        if (!linkedCleanroom.isClean()) return;
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

        Set<BlockPos> blocksConnected = struct.getBlockConn(getWorld(), interior, blockList.get(0));

        if (blocksConnected.size() != blockList.size()) {
            this.struct.status = StructAnalysis.BuildError.DISCONNECTED;
            return;
        }

        // Block-by-block analysis
        for (BlockPos bp: blockList) {
            IBlockState state = getWorld().getBlockState(bp);
        }

        Set<BlockPos> exterior = struct.checkHull(getWorld(), interior, blocksConnected, false);
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

        if (blockStream.anyMatch(engineDetect)) {
            Set<BlockPos> nozzle = blocksConnected.stream()
                    .filter(bp -> getWorld().getBlockState(bp).getBlock().equals(SuSyBlocks.ROCKET_NOZZLE))
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
                Set<BlockPos> airPerimeter = struct.getPerimeter(getWorld(),airLayer,StructAnalysis.layerVecs);
                if (airPerimeter.size()*3 > airLayer.size()) {
                    struct.status = StructAnalysis.BuildError.NOZZLE_MALFORMED;
                    return;
                }
            }
        } else if (blockStream.anyMatch(controlPodDetect)) {

        } else if (blockStream.anyMatch(fuelTankDetect)) {
            if (exterior == null) {
                return; // error was set in the hull detector
            }
        } else if (blockStream.anyMatch(fairingDetect)) {

        } else if (blockStream.anyMatch(interstageDetect)) {

        }

        boolean hasAir = struct.status != StructAnalysis.BuildError.HULL_FULL;

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

    private ModularUI.Builder createGUITemplate(EntityPlayer entityPlayer) {
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BORDERED_BACKGROUND, 176, 166);
        builder.widget(new ClickButtonWidget(10, 140,35,20,"Scan", click -> scanPart()));
        builder.widget(new SlotWidget(importItems, 0, 140,140, true, true));
        return builder;
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
}
