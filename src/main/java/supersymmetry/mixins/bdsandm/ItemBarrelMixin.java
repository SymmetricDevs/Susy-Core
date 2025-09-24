package supersymmetry.mixins.bdsandm;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import funwayguy.bdsandm.inventory.capability.CapabilityBarrel;
import funwayguy.bdsandm.items.ItemBarrel;

@Mixin(value = ItemBarrel.class, remap = false)
public class ItemBarrelMixin extends ItemBlock {

    // Need to extend ItemBlock to use super.getNBTShareTag
    public ItemBarrelMixin(Block block) {
        super(block);
    }

    /**
     * @author The-Minecraft-Scientist (discord rsci.), MCTian-mi
     */
    // Hack that keeps a difficult to reproduce bug somewhere else in BDS&M from kicking players.
    // This hack omits adding the CapabilityBarrel NBT tag if CapabilityBarrel is not present on the given item stack.
    // The original BDS&M code had an assertion that assumed barrelCap was non-null, which would
    // fire from the network handler for a given player and kick them.
    // This hack reduces the scope of the bug to incorrect (not present) barrel metadata on the client.
    @Inject(method = "getNBTShareTag",
            at = @At(value = "INVOKE",
                     target = "Lfunwayguy/bdsandm/inventory/capability/CapabilityBarrel;serializeNBT()Lnet/minecraft/nbt/NBTTagCompound;"),
            cancellable = true)
    public void returnIfNull(ItemStack stack, CallbackInfoReturnable<NBTTagCompound> cir,
                             @Local CapabilityBarrel barrel) {
        if (barrel == null) {
            cir.setReturnValue(super.getNBTShareTag(stack));
        }
    }
}
