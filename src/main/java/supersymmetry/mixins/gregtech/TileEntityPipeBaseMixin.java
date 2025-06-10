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

@Deprecated
@ScheduledForRemoval(inVersion = "Next CEu update")
@Mixin(TileEntityPipeBase.class)
public abstract class TileEntityPipeBaseMixin<
        PipeType extends Enum<PipeType> & IPipeType<NodeDataType>, NodeDataType>
    extends NeighborCacheTileEntityBase implements IPipeTile<PipeType, NodeDataType> {

  @Inject(method = "transferDataFrom", at = @At("HEAD"), cancellable = true, remap = false)
  private void transferDataFrom(IPipeTile<PipeType, NodeDataType> tileEntity, CallbackInfo ci) {

    // "this"
    TileEntityPipeBase<PipeType, NodeDataType> self =
        (TileEntityPipeBase<PipeType, NodeDataType>) (Object) this;
    try {
      // self.pipeType = tileEntity.getPipeType();
      Field pipeTypeField = TileEntityPipeBase.class.getDeclaredField("pipeType");
      pipeTypeField.setAccessible(true);
      PipeType pipeType = tileEntity.getPipeType();
      pipeTypeField.set(self, pipeType);
      // self.paintingColor = tileEntity.getPaintingColor();
      Field paintingColorField = TileEntityPipeBase.class.getDeclaredField("paintingColor");
      paintingColorField.setAccessible(true);
      int paintingColor = tileEntity.getPaintingColor();
      paintingColorField.set(self, paintingColor);
      // self.connections = tileEntity.getConnections();
      Field connectionsField = TileEntityPipeBase.class.getDeclaredField("connections");
      connectionsField.setAccessible(true);
      int connections = tileEntity.getConnections();
      connectionsField.set(self, connections);
      // coverableImplementation
      Field coverableImplementationField =
          TileEntityPipeBase.class.getDeclaredField("coverableImplementation");

      coverableImplementationField.setAccessible(true);
      PipeCoverableImplementation coverableImplementation =
          (PipeCoverableImplementation) coverableImplementationField.get(self);

      if (tileEntity instanceof SyncedTileEntityBase pipeBase) {
        addPacketsFrom(pipeBase);
      }

      tileEntity.getCoverableImplementation().transferDataTo(coverableImplementation);
      self.setFrameMaterial(tileEntity.getFrameMaterial());

    } catch (NoSuchFieldException | IllegalAccessException e) {
      // log here prolly
      e.printStackTrace();
    }
    ci.cancel();
  }
}
