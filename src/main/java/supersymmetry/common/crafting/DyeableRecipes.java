package supersymmetry.common.crafting;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.RecipesArmorDyes;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import supersymmetry.api.items.IDyeableItem;

/**
 * Applies dye to any {@link IDyeableItem}, composed from {@link RecipesArmorDyes}.
 * <p>
 * Ported from GTCEu-Reloaded 43140b63f (LGPL-3.0), adapted to this project's package layout.
 */
public final class DyeableRecipes extends RecipesArmorDyes {

    public DyeableRecipes() {
        setRegistryName("susy_dyeable_dyeing");
    }

    @Override
    public boolean matches(@NotNull InventoryCrafting inv, @NotNull World worldIn) {
        ItemStack itemstack = ItemStack.EMPTY;
        List<ItemStack> dyes = new ArrayList<>();

        for (int i = 0; i < inv.getSizeInventory(); ++i) {
            ItemStack slot = inv.getStackInSlot(i);
            if (slot.isEmpty()) continue;

            if (slot.getItem() instanceof IDyeableItem) {
                if (!itemstack.isEmpty()) return false;
                itemstack = slot;
            } else {
                if (!net.minecraftforge.oredict.DyeUtils.isDye(slot)) return false;
                dyes.add(slot);
            }
        }

        return !itemstack.isEmpty() && !dyes.isEmpty();
    }

    @Override
    public @NotNull ItemStack getCraftingResult(@NotNull InventoryCrafting inv) {
        ItemStack itemstack = ItemStack.EMPTY;
        int[] rgb = new int[3];
        int brightness = 0;
        int count = 0;
        IDyeableItem dyeable = null;

        for (int k = 0; k < inv.getSizeInventory(); ++k) {
            ItemStack slot = inv.getStackInSlot(k);
            if (slot.isEmpty()) continue;

            if (slot.getItem() instanceof IDyeableItem item) {
                dyeable = item;
                itemstack = slot.copy();
                itemstack.setCount(1);

                if (dyeable.hasColor(slot)) {
                    int color = dyeable.getColor(itemstack);
                    float r = (float) (color >> 16 & 255) / 255.0F;
                    float g = (float) (color >> 8 & 255) / 255.0F;
                    float b = (float) (color & 255) / 255.0F;
                    brightness = (int) ((float) brightness + Math.max(r, Math.max(g, b)) * 255.0F);
                    rgb[0] = (int) ((float) rgb[0] + r * 255.0F);
                    rgb[1] = (int) ((float) rgb[1] + g * 255.0F);
                    rgb[2] = (int) ((float) rgb[2] + b * 255.0F);
                    ++count;
                }
            } else {
                if (!net.minecraftforge.oredict.DyeUtils.isDye(slot)) return ItemStack.EMPTY;

                float[] components = net.minecraftforge.oredict.DyeUtils.colorFromStack(slot).get()
                        .getColorComponentValues();
                int r = (int) (components[0] * 255.0F);
                int g = (int) (components[1] * 255.0F);
                int b = (int) (components[2] * 255.0F);
                brightness += Math.max(r, Math.max(g, b));
                rgb[0] += r;
                rgb[1] += g;
                rgb[2] += b;
                ++count;
            }
        }

        if (dyeable == null) return ItemStack.EMPTY;

        int r = rgb[0] / count;
        int g = rgb[1] / count;
        int b = rgb[2] / count;
        float average = (float) brightness / (float) count;
        float max = (float) Math.max(r, Math.max(g, b));
        r = (int) ((float) r * average / max);
        g = (int) ((float) g * average / max);
        b = (int) ((float) b * average / max);
        int color = (r << 8) + g;
        color = (color << 8) + b;

        // prevent consuming dye when the colour would not change
        if (dyeable.hasColor(itemstack) && dyeable.getColor(itemstack) == color) return ItemStack.EMPTY;

        dyeable.setColor(itemstack, color);
        return itemstack;
    }

    @Override
    public @NotNull NonNullList<ItemStack> getRemainingItems(@NotNull InventoryCrafting inv) {
        NonNullList<ItemStack> remaining = NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);

        for (int i = 0; i < remaining.size(); ++i) {
            ItemStack stack = inv.getStackInSlot(i);
            if (!(stack.getItem() instanceof IDyeableItem item && !item.shouldGetContainerItem())) {
                remaining.set(i, net.minecraftforge.common.ForgeHooks.getContainerItem(stack));
            }
        }

        return remaining;
    }
}
