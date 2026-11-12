package supersymmetry.mixins.xnet;

import net.minecraft.item.ItemStack;
import net.minecraftforge.items.IItemHandler;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import mcjty.xnet.apiimpl.items.ItemChannelSettings;

/**
 * An item channel simulates an insert, extracts what the simulation accepted, then inserts for real and
 * discards whatever doesn't fit. Storage Drawers and BDSandM expose one storage through several handler slots,
 * so a simulated walk over the slots counts the same room more than once, and the surplus is lost:
 * <ul>
 * <li>A drawer block has a virtual slot 0 that inserts into every matching drawer, plus one slot per drawer.</li>
 * <li>A BDSandM barrel or crate has two slots that run the same insert code.</li>
 * </ul>
 * These handlers are offered the item through slot 0 only. A drawer block falls back to the normal walk when
 * slot 0 takes nothing (item conversion off).
 */
@Mixin(value = ItemChannelSettings.class, remap = false)
public class ItemChannelSettingsMixin {

    @Unique
    private static final String INSERT_ITEM = "Lnet/minecraftforge/items/ItemHandlerHelper;insertItem(Lnet/minecraftforge/items/IItemHandler;Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/item/ItemStack;";

    /** 1: Storage Drawers block, 2: BDSandM barrel or crate, 0: anything else. Looked up by name, both are optional. */
    @Unique
    private static final ClassValue<Integer> susy$SHARED_STORAGE = new ClassValue<Integer>() {

        @Override
        protected Integer computeValue(Class<?> type) {
            switch (type.getName()) {
                case "com.jaquadro.minecraft.storagedrawers.capabilities.DrawerItemHandler":
                    return 1;
                case "funwayguy.bdsandm.inventory.capability.CapabilityBarrel":
                case "funwayguy.bdsandm.inventory.capability.CapabilityCrate":
                    return 2;
                default:
                    return 0;
            }
        }
    };

    @WrapOperation(method = { "insertStackSimulate", "insertStackReal" }, at = @At(value = "INVOKE", target = INSERT_ITEM))
    private ItemStack susy$insertOncePerStorage(IItemHandler handler, ItemStack stack, boolean simulate,
                                               Operation<ItemStack> original) {
        if (handler != null && !stack.isEmpty()) {
            int kind = susy$SHARED_STORAGE.get(handler.getClass());
            if (kind == 2) {
                return handler.insertItem(0, stack, simulate);
            }
            if (kind == 1) {
                ItemStack rest = handler.insertItem(0, stack, simulate);
                if (rest.getCount() < stack.getCount()) {
                    return rest;
                }
            }
        }
        return original.call(handler, stack, simulate);
    }
}
