package supersymmetry.common.entities;

import cam72cam.immersiverailroading.IRItems;
import cam72cam.immersiverailroading.entity.Locomotive;
import cam72cam.immersiverailroading.entity.physics.Simulation;
import cam72cam.immersiverailroading.entity.physics.SimulationState;
import cam72cam.immersiverailroading.items.nbt.RailSettings;
import cam72cam.immersiverailroading.library.*;
import cam72cam.immersiverailroading.physics.MovementTrack;
import cam72cam.immersiverailroading.thirdparty.trackapi.ITrack;
import cam72cam.immersiverailroading.track.BuilderBase;
import cam72cam.immersiverailroading.track.TrackBase;
import cam72cam.immersiverailroading.util.*;
import cam72cam.mod.fluid.Fluid;
import cam72cam.mod.item.ItemStack;
import cam72cam.mod.math.Vec3d;
import cam72cam.mod.math.Vec3i;
import cam72cam.mod.world.World;
import supersymmetry.integration.immersiverailroading.control.TunnelBoreControl;

import java.util.ArrayList;
import java.util.List;

public class EntityTunnelBore extends Locomotive {

    private int distanceToGo = 50;
    private ArrayList<TunnelBoreControl> controlSequence = new ArrayList<>();
    public FluidQuantity getTankCapacity() {
        return FluidQuantity.ZERO;
    }

    public List<Fluid> getFluidFilter() {
        return new ArrayList();
    }
    public int getInventoryWidth() {
        return 2;
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

    @Override
    public void onTick() {
        super.onTick();
        if(!this.states.isEmpty()) {
            SimulationState currentState = getCurrentState();
            int idx = this.states.indexOf(currentState);
            SimulationState nextState = this.states.get(idx + 1);
            if(nextState != null) {
                Vec3d positionFront = VecUtil.fromWrongYawPitch(nextState.config.offsetFront, nextState.yaw, nextState.pitch).add(nextState.position);
                ITrack trackFront = MovementTrack.findTrack(nextState.config.world, positionFront, nextState.yawFront, nextState.config.gauge.value());
                // We have reached the end of the track
                if(trackFront == null) {
                    if(this.distanceToGo > 0) {
                        RailSettings settings = new RailSettings(
                                Gauge.standard(),
                                "immersiverailroading:track/bmtrack.json",
                                TrackItems.STRAIGHT,
                                this.distanceToGo >= 10 ? 10 : this.distanceToGo,
                                90,
                                1,
                                TrackPositionType.FIXED,
                                TrackSmoothing.BOTH,
                                TrackDirection.NONE,
                                ItemStack.EMPTY,
                                ItemStack.EMPTY,
                                false,
                                false
                        );
                        ItemStack trackBlueprintStack = new ItemStack(IRItems.ITEM_TRACK_BLUEPRINT, 0);
                        settings.write(trackBlueprintStack);
                        PlacementInfo placementInfo = new PlacementInfo(trackBlueprintStack, this.getRotationYaw(), new Vec3d(0.5, 0.5, 0.5));
                        RailInfo railInfo = new RailInfo(trackBlueprintStack, placementInfo, (PlacementInfo) null);
                        World irWorld = getWorld();
                        BuilderBase trackBuilder = railInfo.getBuilder(irWorld, new Vec3i(getPosition()));
                        List<TrackBase> tracks = trackBuilder.getTracksForRender();
                        trackBuilder.build();
                        Simulation.forceQuickUpdates = true;
                        this.states = new ArrayList<>();
                        this.distanceToGo = this.distanceToGo >= 10 ? this.distanceToGo - 10 : 0;
                    }
                }
            }
        }
    }
}

