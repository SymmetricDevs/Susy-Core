package supersymmetry.common.item.behavior;

import static supersymmetry.common.item.behavior.SuSyGunBehavior.*;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

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
            if (tag.hasKey(NBT_AMMO_TYPE)) {
                String ammoType = tag.getString(NBT_AMMO_TYPE);
                lines.add(I18n.format("susy.gun.tooltip.magazine.bullet_type", I18n.format("susy.ammo_type." + ammoType)));
            }
            if (tag.hasKey(NBT_CALIBER)) {
                String caliber = tag.getString(NBT_CALIBER);
                lines.add(I18n.format("susy.gun.tooltip.magazine.caliber", I18n.format("susy.caliber." + caliber)));
            }
            if (tag.hasKey(NBT_AMMO)) {
                lines.add(tag.getInteger(NBT_AMMO) + "/" + this.maxCapacity);
            }
        }
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        if (!itemStack.hasTagCompound()) return 0;
        NBTTagCompound tag = itemStack.getTagCompound();
        if (!tag.hasKey(NBT_AMMO)) return 0;

        return (double) tag.getInteger(NBT_AMMO) / this.maxCapacity;
    }
}
