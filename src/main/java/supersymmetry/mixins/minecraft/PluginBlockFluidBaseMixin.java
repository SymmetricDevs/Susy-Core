package supersymmetry.mixins.minecraft;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(value = BlockFluidBase.class, priority = 1400)
public class PluginBlockFluidBaseMixin {
    @Inject(method = "getExtendedState", at = @At("HEAD"), remap = false, locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
    private static void getFluidExtendedStateWithCheck(IBlockState oldState, IBlockAccess maybeWorld, BlockPos arg2, CallbackInfoReturnable<IBlockState> cir) {
        if (maybeWorld instanceof World || maybeWorld instanceof ChunkCache) {
            return;
        }
        cir.setReturnValue(oldState);
    }
}
