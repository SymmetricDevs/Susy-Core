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
  private static final Field PIPE_TYPE_FIELD;
  private static final Field PAINTING_COLOR_FIELD;
  private static final Field CONNECTIONS_FIELD;
  private static final Field COVERABLE_IMPL_FIELD;

  static {
    try {
      PIPE_TYPE_FIELD = TileEntityPipeBase.class.getDeclaredField("pipeType");
      PAINTING_COLOR_FIELD = TileEntityPipeBase.class.getDeclaredField("paintingColor");
      CONNECTIONS_FIELD = TileEntityPipeBase.class.getDeclaredField("connections");
      COVERABLE_IMPL_FIELD = TileEntityPipeBase.class.getDeclaredField("coverableImplementation");

      PIPE_TYPE_FIELD.setAccessible(true);
      PAINTING_COLOR_FIELD.setAccessible(true);
      CONNECTIONS_FIELD.setAccessible(true);
      COVERABLE_IMPL_FIELD.setAccessible(true);
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  @Inject(method = "transferDataFrom", at = @At("HEAD"), cancellable = true, remap = false)
  private void transferDataFrom(IPipeTile<PipeType, NodeDataType> tileEntity, CallbackInfo ci) {
    TileEntityPipeBase<PipeType, NodeDataType> self = (TileEntityPipeBase<PipeType, NodeDataType>) (Object) this;
    try {
      PIPE_TYPE_FIELD.set(self, tileEntity.getPipeType());
      PAINTING_COLOR_FIELD.set(self, tileEntity.getPaintingColor());
      CONNECTIONS_FIELD.set(self, tileEntity.getConnections());

      PipeCoverableImplementation coverImpl = (PipeCoverableImplementation) COVERABLE_IMPL_FIELD.get(self);
      if (tileEntity instanceof SyncedTileEntityBase pipeBase) {
        self.addPacketsFrom(pipeBase);
      }
      tileEntity.getCoverableImplementation().transferDataTo(coverImpl);
      self.setFrameMaterial(tileEntity.getFrameMaterial());

    } catch (IllegalAccessException e) {
      SusyLog.logger.error("something blew up in the TileEntityPipeBaseMixin");
      e.printStackTrace();
    }
    ci.cancel();
  }
}

