package supersymmetry.common.item.behavior;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;

import org.jetbrains.annotations.NotNull;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;

// mostly a copy of MillBallDurabilityManager.java
public class ElectrodeDurabilityManager implements IItemDurabilityManager, IItemBehaviour {

    public static final ElectrodeDurabilityManager INSTANCE = new ElectrodeDurabilityManager();

    private static final String ELECTRODE_STATS_TAG = "GT.ElectrodeStats";
    private static final String DAMAGE_KEY = "Damage";
    private static final int MAX_DURABILITY = 1600;

    private ElectrodeDurabilityManager() {}

    protected static NBTTagCompound getElectrodeStatsTag(ItemStack itemStack) {
        return itemStack.getSubCompound(ELECTRODE_STATS_TAG);
    }

    protected static NBTTagCompound getOrCreateElectrodeStatsTag(ItemStack itemStack) {
        return itemStack.getOrCreateSubCompound(ELECTRODE_STATS_TAG);
    }

    public static int getElectrodeDamage(ItemStack itemStack) {
        NBTTagCompound compound = getElectrodeStatsTag(itemStack);
        if (compound == null || !compound.hasKey(DAMAGE_KEY, NBT.TAG_ANY_NUMERIC)) {
            return 0;
        }
        return compound.getInteger(DAMAGE_KEY);
    }

    public static void setElectrodeDamage(ItemStack itemStack, int damage) {
        NBTTagCompound compound = getOrCreateElectrodeStatsTag(itemStack);
        compound.setInteger(DAMAGE_KEY, Math.min(MAX_DURABILITY, damage));
    }

    public static int getElectrodeMaxDurability() {
        return MAX_DURABILITY;
    }

    public static int getRemainingUses(ItemStack itemStack) {
        return MAX_DURABILITY - getElectrodeDamage(itemStack);
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        int currentDamage = getElectrodeDamage(itemStack);
        return (double) (MAX_DURABILITY - currentDamage) / (double) MAX_DURABILITY;
    }

    @Override
    public boolean showEmptyBar(ItemStack itemStack) {
        return false;
    }

    @Override
    public boolean showFullBar(ItemStack itemStack) {
        return true;
    }

    @Override
    public void addInformation(ItemStack itemStack, @NotNull List<String> lines) {
        int currentDamage = getElectrodeDamage(itemStack);
        lines.add(I18n.format("item.durability", MAX_DURABILITY - currentDamage, MAX_DURABILITY));
    }
}
