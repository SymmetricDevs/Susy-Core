package supersymmetry.mixins.fluidlogged_api;

import git.jbredwards.fluidlogged_api.api.util.FluidState;
import git.jbredwards.fluidlogged_api.api.util.FluidloggedUtils;
import gregtech.api.GregTechAPI;
import gregtech.core.network.packets.PacketReloadShaders;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import supersymmetry.common.network.SPacketRemoveFluidState;

@Mixin(FluidloggedUtils.class)
public class FluidloggedUtilsMixin {
    @Inject(method = "setFluidState(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Lgit/jbredwards/fluidlogged_api/api/util/FluidState;ZZI)Z" , at = @At("HEAD"), remap = false, locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void setFluidStateWithFlag(World world, BlockPos pos, IBlockState state, FluidState ignored, boolean alsoignored, boolean extraignored, int flags, CallbackInfoReturnable<Boolean> cir) {
        if ((flags & 64) != 0) {
            // Makes sure the server knows there is no fluid
            cir.setReturnValue(FluidloggedUtils.setFluidState(world, pos, state, FluidState.EMPTY, alsoignored, extraignored, flags - 64));
            // Makes sure the client knows there is no fluid
            GregTechAPI.networkHandler.sendToDimension(new SPacketRemoveFluidState(pos), world.provider.getDimension());
        }
    }
}
