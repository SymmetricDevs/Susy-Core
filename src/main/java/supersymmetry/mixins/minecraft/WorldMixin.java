package supersymmetry.mixins.minecraft;

import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldProvider;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import supersymmetry.api.space.dimension.WorldProviderSpace;
import supersymmetry.common.world.WorldProviderPlanet;

@Mixin(World.class)
public abstract class WorldMixin {

    @Shadow
    @Final
    public WorldProvider provider;

    @Inject(method = "getCelestialAngle", at = @At("HEAD"), cancellable = true)
    private void susy$celestialAngleFollowsSun(float partialTicks, CallbackInfoReturnable<Float> cir) {
        var self = (World) (Object) this;
        if (!self.isRemote || !(provider instanceof WorldProviderPlanet) && !(provider instanceof WorldProviderSpace)) {
            return;
        }
        float sun = provider.getSunBrightness(partialTicks);
        cir.setReturnValue(0.5F - 0.5F * MathHelper.clamp(sun / 0.05f, 0.0F, 1.0F));
    }
}
