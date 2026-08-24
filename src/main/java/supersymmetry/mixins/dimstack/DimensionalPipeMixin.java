package supersymmetry.mixins.dimstack;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.items.CapabilityItemHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import cd4017be.dimstack.tileentity.DimensionalPipe;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(value = DimensionalPipe.class, remap = false)
public abstract class DimensionalPipeMixin {

    @Shadow
    @Final
    private static Object[] NULL_CAPS;

    @ModifyReturnValue(method = "hasCapability",
            at = {
                    @At(value = "RETURN", ordinal = 2),
                    @At(value = "RETURN", ordinal = 3),
            })
    private boolean alwaysHasItemAndFluidCapability(boolean original, @Local(argsOnly = true) Capability<?> cap) {
        return original || cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
    }

    @SuppressWarnings("unchecked")
    @ModifyReturnValue(method = "getCapability",
            at = {
                    @At(value = "RETURN", ordinal = 1),
                    @At(value = "RETURN", ordinal = 3),
            })
    private <T> T returnEmptyCapabilities(T original, @Local(argsOnly = true) Capability<?> cap) {
        if (original != null) return original;
        if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return (T) NULL_CAPS[1];
        if (cap == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return (T) NULL_CAPS[2];
        return null;
    }
}
