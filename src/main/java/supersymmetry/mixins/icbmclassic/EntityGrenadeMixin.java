package supersymmetry.mixins.icbmclassic;

import icbm.classic.content.entity.EntityGrenade;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Mixin(value = EntityGrenade.class, remap = false)
public abstract class EntityGrenadeMixin {

    @Shadow
    public abstract String getName();

    private static final Set<String> LINGERING_GRENADE_NAMES = new HashSet<>(Arrays.asList(
            "icbm.grenade.icbmclassic:chemical",
            "icbm.grenade.icbmclassic:debilitation"
    ));

    private static final int LINGER_TICKS = 600;

    private boolean supersymmetry$hasExploded = false;
    private int supersymmetry$lingerTicksRemaining = -1;

    @Inject(method = "triggerExplosion", at = @At("HEAD"), cancellable = true)
    private void supersymmetry$preventReExplosion(CallbackInfo ci) {
        if (supersymmetry$hasExploded) {
            ci.cancel();
            return;
        }
        supersymmetry$hasExploded = true;

        if (LINGERING_GRENADE_NAMES.contains(getName())) {
            supersymmetry$lingerTicksRemaining = LINGER_TICKS;
        }
    }

    @Redirect(method = "triggerExplosion",
            at = @At(value = "INVOKE", target = "Licbm/classic/content/entity/EntityGrenade;setDead()V"))
    private void supersymmetry$deferDeath(EntityGrenade self) {
        if (supersymmetry$lingerTicksRemaining < 0) {
            self.setDead();
        }
    }

    @Inject(method = "onUpdate", at = @At("TAIL"))
    private void supersymmetry$tickLinger(CallbackInfo ci) {
        if (supersymmetry$lingerTicksRemaining < 0) return;

        EntityGrenade self = (EntityGrenade) (Object) this;
        self.motionX = 0.0D;
        self.motionY = 0.0D;
        self.motionZ = 0.0D;

        if (supersymmetry$lingerTicksRemaining == 0) {
            self.setDead();
            supersymmetry$lingerTicksRemaining = -1;
        } else {
            supersymmetry$lingerTicksRemaining--;
        }
    }

    @Inject(method = "canBeCollidedWith", at = @At("HEAD"), cancellable = true)
    private void supersymmetry$noCollideWhileLingering(CallbackInfoReturnable<Boolean> cir) {
        if (supersymmetry$lingerTicksRemaining >= 0) {
            cir.setReturnValue(false);
        }
    }
}
