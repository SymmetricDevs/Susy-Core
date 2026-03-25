package supersymmetry.mixins.minecraft;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityFallingBlock;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import supersymmetry.common.event.GravityHandler;

@Mixin(EntityFallingBlock.class)
public abstract class EntityFallingBlockMixin extends Entity {

    public EntityFallingBlockMixin(World worldIn) {
        super(worldIn);
    }

    @Inject(method = "onUpdate", at = @At("TAIL"))
    public void applyGravity(CallbackInfo callback) {
        GravityHandler.applyGravity(this);
    }
}
