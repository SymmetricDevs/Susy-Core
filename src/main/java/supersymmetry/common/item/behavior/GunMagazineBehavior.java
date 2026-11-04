package supersymmetry.common.item.behavior;

import static supersymmetry.common.item.SuSyMetaItems.*;
import static supersymmetry.common.item.behavior.SuSyGunBehavior.*;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import org.jetbrains.annotations.Nullable;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import lombok.Getter;

public class GunMagazineBehavior implements IItemBehaviour, IItemDurabilityManager {

    @Getter
    private final int maxCapacity;
    private final double massMultiplier;

    public GunMagazineBehavior(int maxCapacity, double massMultiplier) {
        this.maxCapacity = maxCapacity;
        this.massMultiplier = massMultiplier;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        if (itemStack.hasTagCompound()) {
            NBTTagCompound tag = itemStack.getTagCompound();

            if (!tag.hasKey(NBT_AMMO) || tag.getInteger(NBT_AMMO) == 0) {
                lines.add(I18n.format("susy.gun.tooltip.empty"));
            } else {

                if (tag.hasKey(NBT_AMMO_TYPE)) {
                    String ammoType = tag.getString(NBT_AMMO_TYPE);
                    lines.add(I18n.format("susy.gun.tooltip.magazine.bullet_type",
                            I18n.format("susy.ammo_type." + ammoType)));
                }
                if (tag.hasKey(NBT_CALIBER)) {
                    String caliber = tag.getString(NBT_CALIBER);
                    lines.add(I18n.format("susy.gun.tooltip.magazine.caliber", I18n.format("susy.caliber." + caliber)));
                }
                lines.add(tag.getInteger(NBT_AMMO) + "/" + this.maxCapacity);
            }

        }
    }

    /// Finds the first non-empty magazine fitting the given gun (in terms of caliber and max mag size). The mag is
    /// guaranteed to have caliber, ammo, and ammo type NBT tags. Returns null if no valid mag exists.
    @Nullable public static ItemStack findValidMagazine(EntityPlayer player, ItemStack gun) {
        if (gun.getItem() instanceof MetaItem metaItem) {
            if (metaItem.getBehaviours(gun).getFirst() instanceof SuSyGunBehavior gunBehavior) {
                int maxMagSize = gunBehavior.getMaxMagazineSize();
                String caliber = gunBehavior.getCaliber();
                for (int i = 0; i < player.inventory.getSizeInventory(); i++) {
                    ItemStack checkedItem = player.inventory.getStackInSlot(i);
                    if (checkedItem.getItem() instanceof MetaItem metaItem1 &&
                            !metaItem1.getBehaviours(checkedItem).isEmpty()) {
                        if (metaItem1.getBehaviours(checkedItem)
                                .getFirst() instanceof GunMagazineBehavior magBehavior &&
                                checkedItem.hasTagCompound()) {
                            if (magBehavior.getMaxCapacity() <= maxMagSize &&
                                    checkedItem.getTagCompound().hasKey(NBT_CALIBER) &&
                                    checkedItem.getTagCompound().hasKey(NBT_AMMO) &&
                                    checkedItem.getTagCompound().hasKey(NBT_AMMO_TYPE)) {
                                if (checkedItem.getTagCompound().getString(NBT_CALIBER).equals(caliber) &&
                                        checkedItem.getTagCompound().getInteger(NBT_AMMO) > 0)
                                    return checkedItem;
                            }
                        }
                    }
                }

            }
        }

        return null;
    }

    @Nullable public static GunMagazineBehavior getMagBehavior(ItemStack stack) {
        if (stack.getItem() instanceof MetaItem metaItem) {
            if (!metaItem.getBehaviours(stack).isEmpty()) {
                if (metaItem.getBehaviours(stack).getFirst() instanceof GunMagazineBehavior magazineBehavior)
                    return magazineBehavior;
            }
        }
        return null;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) return 0;
        NBTTagCompound tag = itemStack.getTagCompound();
        if (!tag.hasKey(NBT_AMMO)) return 0;

        return (double) tag.getInteger(NBT_AMMO) / this.maxCapacity;
    }

    @Nullable public static ItemStack getMagFromSize(int size) {
        if (size == 5) return MAGAZINE_5.getStackForm();
        if (size == 10) return MAGAZINE_10.getStackForm();
        if (size == 15) return MAGAZINE_15.getStackForm();
        if (size == 20) return MAGAZINE_20.getStackForm();
        if (size == 30) return MAGAZINE_30.getStackForm();
        if (size == 50) return MAGAZINE_50.getStackForm();
        if (size == 200) return MAGAZINE_200.getStackForm();

        return null;
    }
}
