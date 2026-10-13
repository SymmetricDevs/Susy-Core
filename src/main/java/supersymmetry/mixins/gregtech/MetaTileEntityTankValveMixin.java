package supersymmetry.mixins.gregtech;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import org.spongepowered.asm.mixin.Mixin;

import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.common.metatileentities.multi.MetaTileEntityTankValve;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;

@Mixin(value = MetaTileEntityTankValve.class, remap = false)
public abstract class MetaTileEntityTankValveMixin extends MetaTileEntityMultiblockPart {

    protected MetaTileEntityTankValveMixin(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            MultiblockControllerBase controller = getController();
            return controller == null ? null :
                    CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(controller.getFluidInventory());
        }
        return super.getCapability(capability, side);
    }
}
