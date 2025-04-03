package supersymmetry.mixins.littletiles;

import com.creativemd.littletiles.common.structure.connection.IWorldPositionProvider;
import com.creativemd.littletiles.common.structure.connection.StructureChildConnection;
import com.creativemd.littletiles.common.structure.exception.CorruptedLinkException;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/// Backported [this commit](https://github.com/CreativeMD/LittleTiles/commit/9d8097459e87573c38acf4aeb70b4feddb83837d)
@Mixin(value = StructureChildConnection.class, remap = false)
public class StructureChildConnectionMixin {

    @Shadow
    @Final
    public IWorldPositionProvider parent;

    @Inject(method = "getTileEntity()Lcom/creativemd/littletiles/common/tileentity/TileEntityLittleTiles;",
            at = @At(value = "INVOKE",
                    target = "Lcom/creativemd/littletiles/common/structure/connection/StructureChildConnection;getWorld()Lnet/minecraft/world/World;"
            ))
    protected void getTileEntity(CallbackInfoReturnable<TileEntityLittleTiles> cir) throws CorruptedLinkException {
        if (parent == null) {
            throw new CorruptedLinkException();
        }
    }
}
