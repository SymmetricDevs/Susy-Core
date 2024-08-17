package supersymmetry.mixins.mcjtylib;

import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.common.pipelike.itempipe.net.ItemNetHandler;
import mcjty.lib.container.InventoryHelper;

@Mixin(value = InventoryHelper.class, remap = false)
public class InventoryHelperMixin {

    @Inject(method = "isInventory",
            at = @At(
                     value = "RETURN",
                     ordinal = 0),
            cancellable = true)
    private static void checkIsEmpty(TileEntity te, CallbackInfoReturnable<Boolean> cir) {
        IItemHandler inventory = te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, null);
        cir.setReturnValue(inventory != null && !(inventory instanceof ItemNetHandler));
        cir.cancel();
    }
}
