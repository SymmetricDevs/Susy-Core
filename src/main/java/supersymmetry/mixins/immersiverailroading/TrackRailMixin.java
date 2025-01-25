package supersymmetry.mixins.immersiverailroading;

import cam72cam.immersiverailroading.blocks.BlockRailBase;
import cam72cam.immersiverailroading.tile.TileRail;
import cam72cam.immersiverailroading.tile.TileRailBase;
import cam72cam.immersiverailroading.track.BuilderBase;
import cam72cam.immersiverailroading.track.TrackBase;
import cam72cam.immersiverailroading.track.TrackRail;
import cam72cam.mod.math.Vec3i;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import supersymmetry.integration.immersiverailroading.tracknet.WorldTrackNet;

@Mixin(value= TrackRail.class, remap = false)

public class TrackRailMixin extends TrackBase {


    public TrackRailMixin(BuilderBase builder, Vec3i rel, BlockRailBase block) {
        super(builder, rel, block);
    }

    @Inject(method = "placeTrack", at = @At("RETURN"))
    public void placeTrack(boolean actuallyPlace, CallbackInfoReturnable<TileRailBase> cir) {
        if (cir.getReturnValue() instanceof TileRail rail) {
            WorldTrackNet net =  WorldTrackNet.getWorldTrackNet(rail.internal.getWorld());
            if (net != null) net.handleNewTrack(rail);
        }
    }
}
