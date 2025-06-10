package supersymmetry.mixins.gregtech;

import gregtech.api.metatileentity.NeighborCacheTileEntityBase;
import gregtech.api.metatileentity.SyncedTileEntityBase;
import gregtech.api.pipenet.block.IPipeType;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.PipeCoverableImplementation;
import gregtech.api.pipenet.tile.TileEntityPipeBase;

import java.lang.reflect.Field;

import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supersymmetry.api.SusyLog;

@Deprecated
@ScheduledForRemoval(inVersion = "Next CEu update")
@Mixin(TileEntityPipeBase.class)
public abstract class TileEntityPipeBaseMixin<
        PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType>
        extends NeighborCacheTileEntityBase implements IPipeTile<PipeType, NodeDataType> {
    @Shadow
    private PipeType pipeType;
    @Shadow
    private int paintingColor;
    @Shadow
    private int connections;
    @Shadow
    private PipeCoverableImplementation coverableImplementation;


    @Inject(method = "transferDataFrom", at = @At("HEAD"), cancellable = true, remap = false)
    private void transferDataFrom(IPipeTile<PipeType, NodeDataType> tileEntity, CallbackInfo ci) {
        // "this"
        TileEntityPipeBase<PipeType, NodeDataType> self =
                (TileEntityPipeBase<PipeType, NodeDataType>) (Object) this;
        this.pipeType = tileEntity.getPipeType();
        this.paintingColor = tileEntity.getPaintingColor();
        this.connections = tileEntity.getConnections();
        if (tileEntity instanceof SyncedTileEntityBase pipeBase) {
            addPacketsFrom(pipeBase);
        }
        tileEntity.getCoverableImplementation().transferDataTo(this.coverableImplementation);
        self.setFrameMaterial(tileEntity.getFrameMaterial());

        ci.cancel();
    }
}
