package supersymmetry.mixins.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityMinecart;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import supersymmetry.common.event.GravityHandler;

@Mixin(EntityMinecart.class)
public abstract class EntityMinecartMixin extends Entity {

    public EntityMinecartMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "onUpdate", at = @At("TAIL"))
    public void applyGravity(CallbackInfo callback) {
        GravityHandler.applyGravity(this);
    }
}
