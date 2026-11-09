package supersymmetry.mixins.catwalks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.*;

import supersymmetry.client.renderer.CatwalkCache;

@Pseudo
@Mixin(targets = "dmfmm.catwalks.tileentity.CatwalkStateCalculator", remap = false)
public abstract class CatwalkStateCalculatorMixin {

    @Shadow
    World world;

    @Inject(method = "getCached", at = @At("HEAD"), cancellable = true)
    private void cached(BlockPos pos, CallbackInfoReturnable<TileEntity> cir) {
        cir.setReturnValue(CatwalkCache.get(world, pos));
    }

    @Inject(method = "removeFromCache", at = @At("HEAD"), cancellable = true)
    private static void remove(World world, BlockPos pos, CallbackInfo ci) {
        CatwalkCache.remove(world, pos);
        ci.cancel();
    }
}
