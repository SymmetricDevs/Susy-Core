package supersymmetry.mixins.minecraft;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import supersymmetry.common.event.GravityHandler;

@Mixin(Entity.class)
public abstract class EntityMixin implements ICommandSender, ICapabilitySerializable<NBTTagCompound> {

    @Inject(method = "onUpdate", at = @At("TAIL"))
    public void applyGravity(CallbackInfo callback) {
        GravityHandler.applyGravity((Entity) (Object) this);
    }
}
