package supersymmetry.mixins.findme;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.hjae.findme.network.FluidPositionRequestMessage;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import supersymmetry.integration.findme.FaceFluidHandlers;

/**
 * FindMe's fluid search asks each tile entity for its fluid handler without a side. Blocks that only answer for a
 * real side, like the Fluid Sample Storage, are never found. When the side-less lookup comes back empty, try the
 * six faces.
 */
@Mixin(value = FluidPositionRequestMessage.Handler.class, remap = false)
public class FluidPositionRequestHandlerMixin {

    @WrapOperation(method = "lambda$onMessage$0",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/tileentity/TileEntity;hasCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Z"))
    private static boolean susy$hasFluidHandlerOnAnyFace(TileEntity te, Capability<?> capability, EnumFacing side,
                                                        Operation<Boolean> original) {
        if (original.call(te, capability, side)) return true;
        if (side != null || capability != CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) return false;
        return FaceFluidHandlers.of(te) != null;
    }

    @WrapOperation(method = "lambda$onMessage$0",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/tileentity/TileEntity;getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Ljava/lang/Object;"))
    private static Object susy$getFluidHandlerOnAnyFace(TileEntity te, Capability<?> capability, EnumFacing side,
                                                       Operation<Object> original) {
        Object handler = original.call(te, capability, side);
        if (handler != null || side != null || capability != CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            return handler;
        }
        return FaceFluidHandlers.of(te);
    }
}
