package supersymmetry.mixins.rftools;

import mcjty.rftools.blocks.storagemonitor.PacketReturnInventoryInfo;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import supersymmetry.api.bugfixes.IItemStackInfo;

@Mixin(PacketReturnInventoryInfo.InventoryInfo.class)
public class InventoryInfoMixin implements IItemStackInfo {

    @Unique
    private ItemStack stack;

    @Override
    public ItemStack getStack() {
        return stack;
    }

    @Override
    public void setStack(ItemStack stack) {
        this.stack = stack;
    }
}
