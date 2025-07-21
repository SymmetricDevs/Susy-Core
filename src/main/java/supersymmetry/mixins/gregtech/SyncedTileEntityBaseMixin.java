package supersymmetry.mixins.gregtech;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gregtech.api.block.BlockStateTileEntity;
import gregtech.api.metatileentity.SyncedTileEntityBase;
import gregtech.api.network.PacketDataList;
import net.minecraft.world.WorldServer;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

/// Ported from [CEu#2823](https://github.com/GregTechCEu/GregTech/pull/2823)
/// and [CEu#2839](https://github.com/GregTechCEu/GregTech/pull/2839)
@Deprecated
@ScheduledForRemoval(inVersion = "Next CEu update")
@Mixin(value = SyncedTileEntityBase.class, remap = false)
public abstract class SyncedTileEntityBaseMixin extends BlockStateTileEntity {

    @Unique
    private static final int SIZE_THRESHOLD = 10;
    @Shadow
    @Final
    private PacketDataList updates;

    @WrapOperation(method = "writeCustomData",
            at = @At(value = "INVOKE",
                    target = "Lgregtech/api/metatileentity/SyncedTileEntityBase;notifyWorld()V"))
    private void checkCanNotifyWorld(SyncedTileEntityBase self, Operation<Void> method) {
        // Short circuit with packet size to avoid too many hash lookups and instanceof casts
        if (updates.size() > SIZE_THRESHOLD && getWorld() instanceof WorldServer server) {
            int x = getPos().getX() >> 4;
            int z = getPos().getZ() >> 4;
            if (server.getPlayerChunkMap().contains(x, z)) {
                method.call(self);
            } else {
                // Cannot send it, so clear
                updates.clear();
            }
            return;
        }
        // Assume we can send data regardless
        method.call(self);
    }
}
