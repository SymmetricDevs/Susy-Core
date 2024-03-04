package supersymmetry.common.entities;

import cam72cam.immersiverailroading.entity.Locomotive;
import cam72cam.immersiverailroading.physics.MovementTrack;
import cam72cam.immersiverailroading.thirdparty.trackapi.ITrack;
import cam72cam.immersiverailroading.util.FluidQuantity;
import cam72cam.mod.fluid.Fluid;
import cam72cam.mod.math.Vec3d;
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


    @Override
    public void onTick() {
        super.onTick();

        if(this.getRotationYaw() % 90 != 0) return;

        Vec3d positionFront = this.predictFrontBogeyPosition((float) this.getVelocity().length());
        ITrack trackFront = MovementTrack.findTrack(this.getWorld(), positionFront, this.getFrontYaw(), this.gauge.value());
        // We have reached the end of the track
        if(trackFront == null) {
            /*if(this.distanceToGo > 0) {
                RailSettings settings = new RailSettings(
                        this.gauge,
                        "immersiverailroading:track/bmtrack.json",
                        TrackItems.STRAIGHT,
                        this.distanceToGo >= 10 ? 10 : this.distanceToGo,
                        90,
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
                RailInfo railInfo = new RailInfo(trackBlueprintStack, placementInfo, null);
                World irWorld = getWorld();
                BuilderBase trackBuilder = railInfo.getBuilder(irWorld, new Vec3i(getPosition()));
                List<TrackBase> tracks = trackBuilder.getTracksForRender();
                trackBuilder.build();
                this.distanceToGo = this.distanceToGo >= 10 ? this.distanceToGo - 10 : 0;
            }*/
        }
    }

    @Override
    protected int getAvailableHP() {
        return this.getDefinition().getHorsePower(this.gauge);
    }
}

