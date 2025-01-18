package supersymmetry.common.item.armor;

import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import supersymmetry.api.items.IBreathingArmorLogic;
import supersymmetry.common.event.DimensionBreathabilityHandler;

import java.util.List;

public class SimpleGasMask implements IBreathingArmorLogic, IItemDurabilityManager {
    public static final double LIFETIME = 600;
    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack itemStack) {
        return EntityEquipmentSlot.HEAD;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "gregtech:textures/armor/simple_gas_mask.png";
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        return 1 - getDamage(itemStack);
    }

    @Override
    public boolean canBreakWithDamage(ItemStack stack) {
        return getDamage(stack) >= 1;
    }

    @Override
    public boolean mayBreatheWith(ItemStack stack, EntityPlayer player) {
        return player.dimension == DimensionBreathabilityHandler.BENEATH_ID && getDamage(stack) < 1;
    }


    @Override
    public boolean isValidArmor(ItemStack itemStack, Entity entity, EntityEquipmentSlot equipmentSlot) {
        return true;
    }

    @Override
    public double tryTick(ItemStack stack, EntityPlayer player) {
        if (DimensionBreathabilityHandler.isInHazardousEnvironment(player)) {
            changeDamage(stack, 1. / LIFETIME); // It's actually ticked every overall second, not just every tick.
        }
        if (getDamage(stack) >= 1) {
            player.renderBrokenItemStack(stack);
            stack.shrink(1);
            player.setItemStackToSlot(EntityEquipmentSlot.HEAD, ItemStack.EMPTY);
        }
        return 0;
    }

    @Override
    public void addInformation(ItemStack stack, List<String> tooltips) {
        int secondsRemaining = (int) (LIFETIME - getDamage(stack) * LIFETIME);
        tooltips.add(I18n.format("supersymmetry.seconds_left", secondsRemaining));
    }

    private double getDamage(ItemStack stack) {
        if (stack.getTagCompound() == null) {
            stack.setTagCompound(new NBTTagCompound());
        }
        if (!stack.getTagCompound().hasKey("damage")) {
            stack.getTagCompound().setDouble("damage", 0);
        }
        return stack.getTagCompound().getDouble("damage");
    }

    private void changeDamage(ItemStack stack, double damageChange) {
        NBTTagCompound compound = stack.getTagCompound();
        compound.setDouble("damage", getDamage(stack) + damageChange);
        stack.setTagCompound(compound);
    }
}
