package supersymmetry.common.item.armor;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class SpaceSuitTank extends SpaceSuit {

    public final static double INFINITE_OXYGEN = -1;
    public final double maxOxygen;
    private final double maxFlowRate;

    public SpaceSuitTank(int maxDurability, double hoursOfLife, String name, int tier, double relativeAbsorption,
                         double maxOxygen, double maxFlowRate) {
        super(EntityEquipmentSlot.CHEST, maxDurability, hoursOfLife, name, tier, relativeAbsorption);
        this.maxOxygen = maxOxygen;
        this.maxFlowRate = maxFlowRate;
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
    public double getMaxFlowRate(ItemStack stack) {
        return maxFlowRate;
    }
}
