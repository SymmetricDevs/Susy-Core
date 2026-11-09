package supersymmetry.mixins.icbmclassic;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Group;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import icbm.classic.content.entity.EntityGrenade;
import io.netty.buffer.ByteBuf;

@Mixin(value = EntityGrenade.class, remap = false)
public abstract class EntityGrenadeMixin {

    private static final Set<String> LINGERING_GRENADE_NAMES = new HashSet<>(Arrays.asList(
            "icbm.grenade.icbmclassic:chemical",
            "icbm.grenade.icbmclassic:debilitation"));

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

        if (LINGERING_GRENADE_NAMES.contains(((Entity) (Object) this).getName())) {
            supersymmetry$lingerTicksRemaining = LINGER_TICKS;
        }
    }

    @Group(name = "susyGrenadeDeath", min = 1, max = 1)
    @Redirect(method = "triggerExplosion",
              at = @At(value = "INVOKE", target = "Licbm/classic/content/entity/EntityGrenade;setDead()V"),
              require = 0)
    private void supersymmetry$deferDeath(EntityGrenade self) {
        if (supersymmetry$lingerTicksRemaining < 0) {
            self.setDead();
        }
    }

    @Group(name = "susyGrenadeDeath", min = 1, max = 1)
    @Redirect(method = "triggerExplosion",
              at = @At(value = "INVOKE", target = "Licbm/classic/content/entity/EntityGrenade;func_70106_y()V"),
              require = 0)
    private void supersymmetry$deferDeathSrg(EntityGrenade self) {
        if (supersymmetry$lingerTicksRemaining < 0) {
            ((Entity) self).setDead();
        }
    }

    @Inject(method = { "onUpdate", "func_70071_h_" }, at = @At("TAIL"))
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

    @Inject(method = { "canBeCollidedWith", "func_70067_L" }, at = @At("HEAD"), cancellable = true)
    private void supersymmetry$noCollideWhileLingering(CallbackInfoReturnable<Boolean> cir) {
        if (supersymmetry$lingerTicksRemaining >= 0) {
            cir.setReturnValue(false);
        }
    }

    private static int susycompat$remaining(int value) {
        return Math.max(-1, Math.min(600, value));
    }

    @Inject(method = { "writeEntityToNBT", "func_70014_b" }, at = @At("TAIL"))
    private void save(NBTTagCompound tag, CallbackInfo ci) {
        tag.setBoolean("susycompat:exploded", supersymmetry$hasExploded);
        tag.setInteger("susycompat:linger", supersymmetry$lingerTicksRemaining);
    }

    @Inject(method = { "readEntityFromNBT", "func_70037_a" }, at = @At("TAIL"))
    private void load(NBTTagCompound tag, CallbackInfo ci) {
        if (tag.hasKey("susycompat:exploded")) {
            supersymmetry$hasExploded = tag.getBoolean("susycompat:exploded");
            supersymmetry$lingerTicksRemaining = supersymmetry$hasExploded ?
                    Math.max(0, susycompat$remaining(tag.getInteger("susycompat:linger"))) : -1;
        }
    }

    @Inject(method = "writeSpawnData", at = @At("TAIL"))
    private void spawnWrite(ByteBuf data, CallbackInfo ci) {
        data.writeBoolean(supersymmetry$hasExploded);
        data.writeShort(susycompat$remaining(supersymmetry$lingerTicksRemaining));
    }

    @Inject(method = "readSpawnData", at = @At("TAIL"))
    private void spawnRead(ByteBuf data, CallbackInfo ci) {
        if (data.readableBytes() >= 3) {
            supersymmetry$hasExploded = data.readBoolean();
            int remaining = susycompat$remaining(data.readShort());
            supersymmetry$lingerTicksRemaining = supersymmetry$hasExploded ? Math.max(0, remaining) : -1;
        }
    }
}
