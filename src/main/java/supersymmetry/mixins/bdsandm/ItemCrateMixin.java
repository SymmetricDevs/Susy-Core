package supersymmetry.mixins.bdsandm;

import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.ICrate;
import funwayguy.bdsandm.items.ItemCrate;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nullable;

@Mixin(ItemCrate.class)
public class ItemCrateMixin extends ItemBlock {
    /**
     * @author Bruberu
     * @reason It was literally using an assert in client-side logic, causing crashes in servers.
     */
    @Overwrite
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        ICrate crate = (ICrate)stack.getCapability(BdsmCapabilies.CRATE_CAP, (EnumFacing)null);
        if (crate != null) {
            stack.setTagInfo("crateCap", crate.serializeNBT());
        }
        return super.getNBTShareTag(stack);
    }

    /**
     * @author Bruberu
     * @reason It was literally using an assert in client-side logic, causing crashes in servers.
     */
    @Overwrite
    public void readNBTShareTag(ItemStack stack, @Nullable NBTTagCompound nbt) {
        super.readNBTShareTag(stack, nbt);
        ICrate crate = (ICrate)stack.getCapability(BdsmCapabilies.CRATE_CAP, (EnumFacing)null);

        if (crate != null) {
            crate.deserializeNBT(stack.getOrCreateSubCompound("crateCap"));
        }
    }
}
