package supersymmetry.common.item.armor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import supersymmetry.common.event.DimensionBreathabilityHandler;
import supersymmetry.common.item.SuSyArmorItem;

import static net.minecraft.inventory.EntityEquipmentSlot.*;

public class AdvancedBreathingApparatus extends BreathingApparatus {
    private final double hoursOfLife;
    private final String name;
    private final int tier;

    public AdvancedBreathingApparatus(EntityEquipmentSlot slot, double hoursOfLife, String name, int tier) {
        super(slot);
        this.hoursOfLife = hoursOfLife;
        this.name = name;
        this.tier = tier;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        if (this.SLOT == LEGS) {
            return "gregtech:textures/armor/" + name + "/legs.png";
        }
        return "gregtech:textures/armor/" + name + "/not_legs.png";
    }

    @Override
    public double tryTick(ItemStack stack, EntityPlayer player) {
        if (!DimensionBreathabilityHandler.isInHazardousEnvironment(player)) {
            return 0;
        }
        this.handleDamage(stack, player);

        ItemStack chest = player.getItemStackFromSlot(CHEST);
        if (chest.getItem() instanceof SuSyArmorItem item) {
            if (item.getItem(chest).getArmorLogic() instanceof AdvancedBreathingApparatus tank && tank.tier == tier) {
                tank.changeOxygen(stack, 1.);
                tank.handleDamage(stack, player);

                int piecesCount = 0;
                ItemStack leggings = player.getItemStackFromSlot(LEGS);
                if (leggings.getItem() instanceof SuSyArmorItem item2) {
                    if (item2.getItem(leggings).getArmorLogic() instanceof AdvancedBreathingApparatus legLogic) {
                        legLogic.handleDamage(stack, player);
                        piecesCount++;
                    }
                }

                ItemStack boots = player.getItemStackFromSlot(FEET);
                if (boots.getItem() instanceof SuSyArmorItem item2) {
                    if (item2.getItem(boots).getArmorLogic() instanceof AdvancedBreathingApparatus bootLogic) {
                        bootLogic.handleDamage(stack, player);
                        piecesCount++;
                    }
                }
                switch (piecesCount) {
                    case 0:
                        return 0.5;
                    case 1:
                        return 1;
                    case 2:
                        return 2;
                }
            }
        }
        return 0.0625;
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


    void handleDamage(ItemStack stack, EntityPlayer player) {
        if (hoursOfLife == 0) {
            return; // No damage
        }
        double amount = (1. / (60. * 60. * hoursOfLife));
        changeDamage(stack, amount); // It's actually ticked every overall second, not just every tick.
        if (getDamage(stack) >= 1) {
            player.renderBrokenItemStack(stack);
            stack.shrink(1);
            player.setItemStackToSlot(HEAD, ItemStack.EMPTY);
        }
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        if (SLOT == CHEST) {
            return 1 - (getOxygen(itemStack) / getMaxOxygen(itemStack));
        } else {
            return 1 - getDamage(itemStack);
        }
    }
}
