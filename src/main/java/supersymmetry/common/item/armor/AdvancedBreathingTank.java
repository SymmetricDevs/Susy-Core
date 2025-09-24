package supersymmetry.common.item.armor;

import static net.minecraft.inventory.EntityEquipmentSlot.CHEST;
import static supersymmetry.common.event.DimensionBreathabilityHandler.isInHazardousEnvironment;

import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class AdvancedBreathingTank extends AdvancedBreathingApparatus {

    public final static double INFINITE_OXYGEN = -1;
    public final double maxOxygen;

    public AdvancedBreathingTank(int maxDurability, double hoursOfLife, String name, int tier,
                                 double relativeAbsorption, double maxOxygen) {
        super(EntityEquipmentSlot.CHEST, maxDurability, hoursOfLife, name, tier, relativeAbsorption);
        this.maxOxygen = maxOxygen;
    }

    @Override
    public void addInformation(ItemStack stack, List<String> strings) {
        int maxOxygen = (int) getMaxOxygen(stack);
        if (maxOxygen == INFINITE_OXYGEN) {
            strings.add(I18n.format("supersymmetry.unlimited_oxygen"));
        } else {
            int oxygen = (int) getOxygen(stack);
            strings.add(I18n.format("supersymmetry.oxygen", oxygen, maxOxygen));
        }
        super.addInformation(stack, strings);
    }

    public void changeOxygen(ItemStack stack, double oxygenChange) {
        if (getMaxOxygen(stack) == INFINITE_OXYGEN) {
            return;
        }
        super.changeOxygen(stack, oxygenChange);
    }

    public double getOxygen(ItemStack stack) {
        if (getMaxOxygen(stack) == INFINITE_OXYGEN) {
            return 12000;
        }
        return super.getOxygen(stack);
    }

    @Override
    double getMaxOxygen(ItemStack stack) {
        return this.maxOxygen;
    }

    @Override
    public void onArmorTick(World world, EntityPlayer player, ItemStack itemStack) {
        if (player.isInsideOfMaterial(Material.WATER)) {
            if (getOxygen(player.getItemStackFromSlot(CHEST)) > 0) {
                player.setAir(300);
                if (!isInHazardousEnvironment(player)) {
                    changeOxygen(player.getItemStackFromSlot(CHEST), (-1f) / 20);
                    // assuming that if its hazardous the player is already breathing with the suit
                }
            }
        }
    }
}
