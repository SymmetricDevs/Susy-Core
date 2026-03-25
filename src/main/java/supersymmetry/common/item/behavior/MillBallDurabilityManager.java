package supersymmetry.common.item.behavior;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;

import org.jetbrains.annotations.NotNull;

import gregtech.api.items.materialitem.MetaPrefixItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.unification.material.Material;
import supersymmetry.api.unification.material.properties.MillBallProperty;
import supersymmetry.api.unification.material.properties.SuSyPropertyKey;

/**
 * Durability manager for mill balls.
 * Displays durability based on NBT-stored damage value and the material's MillBallProperty.
 */
public class MillBallDurabilityManager implements IItemDurabilityManager, IItemBehaviour {

    public static final MillBallDurabilityManager INSTANCE = new MillBallDurabilityManager();

    private static final String MILL_BALL_STATS_TAG = "GT.MillBallStats";
    private static final String DAMAGE_KEY = "Damage";

    private MillBallDurabilityManager() {}

    /**
     * Gets the mill ball stats NBT tag (read-only).
     */
    protected static NBTTagCompound getMillBallStatsTag(ItemStack itemStack) {
        return itemStack.getSubCompound(MILL_BALL_STATS_TAG);
    }

    /**
     * Gets or creates the mill ball stats NBT tag (for writing).
     */
    protected static NBTTagCompound getOrCreateMillBallStatsTag(ItemStack itemStack) {
        return itemStack.getOrCreateSubCompound(MILL_BALL_STATS_TAG);
    }

    /**
     * Gets the current damage value from NBT.
     */
    public static int getMillBallDamage(ItemStack itemStack) {
        NBTTagCompound compound = getMillBallStatsTag(itemStack);
        if (compound == null || !compound.hasKey(DAMAGE_KEY, NBT.TAG_ANY_NUMERIC)) {
            return 0;
        }
        return compound.getInteger(DAMAGE_KEY);
    }

    /**
     * Sets the damage value in NBT.
     */
    public static void setMillBallDamage(ItemStack itemStack, int damage) {
        int maxDurability = getMillBallMaxDurability(itemStack);
        NBTTagCompound compound = getOrCreateMillBallStatsTag(itemStack);
        compound.setInteger(DAMAGE_KEY, Math.min(maxDurability, damage));
    }

    /**
     * Gets the maximum durability for this mill ball based on its material.
     */
    public static int getMillBallMaxDurability(ItemStack itemStack) {
        Material material = MetaPrefixItem.tryGetMaterial(itemStack);
        if (material == null || !material.hasProperty(SuSyPropertyKey.MILL_BALL)) {
            return 1; // Prevent division by zero
        }

        MillBallProperty property = material.getProperty(SuSyPropertyKey.MILL_BALL);
        return Math.max(1, property.durability());
    }

    /**
     * Applies damage to the mill ball. If damage exceeds durability, sets it to zero durability.
     *
     * @param itemStack     the mill ball ItemStack
     * @param damageApplied the amount of damage to apply
     */
    public static boolean applyMillBallDamage(ItemStack itemStack, int damageApplied) {
        int maxDurability = getMillBallMaxDurability(itemStack);
        int currentDamage = getMillBallDamage(itemStack);
        int resultDamage = currentDamage + damageApplied;

        if (resultDamage >= maxDurability) {
            // Mill ball is broken, consume it
            setMillBallDamage(itemStack, 0);
            return true;
        } else {
            // Apply damage
            setMillBallDamage(itemStack, resultDamage);
            return false;
        }
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        int maxDurability = getMillBallMaxDurability(itemStack);
        int currentDamage = getMillBallDamage(itemStack);

        // Return the remaining durability as a fraction (0.0 = broken, 1.0 = full)
        return (double) (maxDurability - currentDamage) / (double) maxDurability;
    }

    @Override
    public boolean showEmptyBar(ItemStack itemStack) {
        return true;
    }

    @Override
    public boolean showFullBar(ItemStack itemStack) {
        return false; // Don't show the bar when completely full
    }

    @Override
    public void addInformation(ItemStack itemStack, @NotNull List<String> lines) {
        int maxDurability = getMillBallMaxDurability(itemStack);
        int currentDamage = getMillBallDamage(itemStack);

        lines.add(I18n.format("item.durability", maxDurability - currentDamage,
                getMillBallMaxDurability(itemStack)));
    }
}
