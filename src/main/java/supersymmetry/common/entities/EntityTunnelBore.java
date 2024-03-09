package supersymmetry.common.entities;

import cam72cam.immersiverailroading.IRItems;
import cam72cam.immersiverailroading.entity.Locomotive;
import cam72cam.immersiverailroading.entity.physics.Simulation;
import cam72cam.immersiverailroading.entity.physics.SimulationState;
import cam72cam.immersiverailroading.inventory.SlotFilter;
import cam72cam.immersiverailroading.items.nbt.RailSettings;
import cam72cam.immersiverailroading.library.*;
import cam72cam.immersiverailroading.physics.MovementTrack;
import cam72cam.immersiverailroading.thirdparty.trackapi.ITrack;
import cam72cam.immersiverailroading.track.BuilderBase;
import cam72cam.immersiverailroading.util.*;
import cam72cam.mod.entity.Player;
import cam72cam.mod.fluid.Fluid;
import cam72cam.mod.item.ItemStack;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.math.Vec3i;
import cam72cam.mod.world.World;
import net.minecraft.init.Blocks;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.integration.immersiverailroading.control.TunnelBoreControl;
import supersymmetry.integration.immersiverailroading.gui.SuSyIRGUITypes;

import java.util.ArrayList;
import java.util.List;

public class EntityTunnelBore extends Locomotive {

    // In degrees
    private float borerAngle = 0;

    private final int trackLength = 10;

    private ItemStack railBedFill = new ItemStack(new net.minecraft.item.ItemStack(Blocks.CONCRETE));
    private ArrayList<TunnelBoreControl> controlSequence = new ArrayList<>();
    public FluidQuantity getTankCapacity() {
        return FluidQuantity.ZERO;
    }

    public List<Fluid> getFluidFilter() {
        return new ArrayList();
    }
    public int getInventoryWidth() {
        return 9;
    }
    public boolean providesElectricalPower() {
        return false;
    }

    // I just stole this from hand cars, I have no idea what it does - MTBO
    // TODO: Electrical locomotives?
    public double getAppliedTractiveEffort(Speed speed) {
        double maxPower_W = (double)this.getDefinition().getHorsePower(this.gauge) * 745.7;
        double efficiency = 0.82;
        double speed_M_S = Math.abs(speed.metric()) / 3.6;
        double maxPowerAtSpeed = maxPower_W * efficiency / speed_M_S;
        return maxPowerAtSpeed * (double)this.getThrottle() * (double)this.getReverser();
    }

    // Invent handling
    @Override
    public int getInventorySize() {
        return 36;
    }

    @Override
    protected void initContainerFilter() {
        this.cargoItems.filter.clear();
        ItemStack trackSegmentStack = new ItemStack(SuSyMetaItems.TRACK_SEGMENT.getStackForm());
        SlotFilter filter = ItemStack -> ItemStack.is(trackSegmentStack);
        this.cargoItems.defaultFilter = filter;
    }

    @Override
    public boolean openGui(Player player) {
        if (player.hasPermission(Permissions.LOCOMOTIVE_CONTROL)) {
            SuSyIRGUITypes.TUNNEL_BORE.open(player, this);
        }

        return true;
    }

    @Override
    public void onTick() {
        super.onTick();

        this.updateBorer();
        if(this.getRotationYaw() % 90 != 0) return;

        if(!this.states.isEmpty()) {
            SimulationState currentState = getCurrentState();
            int idx = this.states.indexOf(currentState);
            SimulationState nextState = this.states.get(idx + 1);
            if(nextState != null) {
                Vec3d positionFront = VecUtil.fromWrongYawPitch(nextState.config.offsetFront, nextState.yaw, nextState.pitch).add(nextState.position);
                ITrack trackFront = MovementTrack.findTrack(nextState.config.world, positionFront, nextState.yawFront, nextState.config.gauge.value());
                // We have reached the end of the track
                if(trackFront == null) {
                    this.placeTrack();
                }
            }
        }
    }

    /*
     * Assumes uniform circular motion.
     * Angle in degrees.
     */

    public void updateBorer() {
        this.borerAngle += 360 / 20 * this.getBorerVelocity();
        this.borerAngle %= 360;
    }

    /*
     * Determines the velocity at which the bore head rotates. Returns the velocity in units revolutions per second
     * Based on current locomotive throttle. Maximum throttle corresponds to a speed of 0.2 revolutions per second.
     *
     */
    public double getBorerVelocity() {
        return this.getThrottle() / 5;
    }

    public float getBorerAngle() {
        return borerAngle;
    }

    public void placeTrack() {
        for (int i = 0; i < this.cargoItems.getSlotCount(); i++) {
            ItemStack stack = this.cargoItems.get(i);
            if (!stack.isEmpty()) {
                int placeableLength = this.trackLength;

                RailSettings settings;

                if(this.getRotationPitch() <= 0) settings = getSettingsStraight(placeableLength);
                else settings = getSettingsSlope(placeableLength);

                ItemStack trackBlueprintStack = new ItemStack(IRItems.ITEM_TRACK_BLUEPRINT, 0);
                settings.write(trackBlueprintStack);
                PlacementInfo placementInfo = new PlacementInfo(trackBlueprintStack, this.getRotationYaw(), new Vec3d(0.5, 0.5, 0.5));
                RailInfo railInfo = new RailInfo(trackBlueprintStack, placementInfo, null);
                World irWorld = getWorld();
                BuilderBase trackBuilder = railInfo.getBuilder(irWorld, new Vec3i(getPosition()));
                trackBuilder.build();

                Simulation.forceQuickUpdates = true;
                this.states = new ArrayList<>();

                this.cargoItems.extract(i, 1, false);
                return;
            }
        }
    }

    public RailSettings getSettingsStraight (int length) {
        return new RailSettings(
                this.gauge,
                "immersiverailroading:track/bmtrack.json",
                TrackItems.STRAIGHT,
                length,
                90,
                1,
                TrackPositionType.FIXED,
                TrackSmoothing.BOTH,
                TrackDirection.NONE,
                ItemStack.EMPTY,
                this.railBedFill,
                false,
                false
        );
    }

    public RailSettings getSettingsSlope (int length) {
        return new RailSettings(
                this.gauge,
                "immersiverailroading:track/bmtrack.json",
                TrackItems.SLOPE,
                length,
                90,
                1,
                TrackPositionType.FIXED,
                TrackSmoothing.BOTH,
                TrackDirection.NONE,
                ItemStack.EMPTY,
                this.railBedFill,
                false,
                false
        );
    }
}

