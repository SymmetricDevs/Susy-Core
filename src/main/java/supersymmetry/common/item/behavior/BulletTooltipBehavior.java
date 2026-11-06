package supersymmetry.common.item.behavior;

import static supersymmetry.common.item.behavior.SuSyGunBehavior.NBT_AMMO_TYPE;
import static supersymmetry.common.item.behavior.SuSyGunBehavior.NBT_CALIBER;
import static supersymmetry.common.metatileentities.multi.electric.MetaTileEntityCargoDronePad.*;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import gregtech.api.items.metaitem.stats.IItemBehaviour;

public class BulletTooltipBehavior implements IItemBehaviour {

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        if (itemStack.hasTagCompound()) {
            NBTTagCompound tag = itemStack.getTagCompound();
            if (tag.hasKey(NBT_CALIBER)) {
                lines.add(I18n.format("susy.gun.tooltip.caliber",
                        I18n.format("susy.caliber." + tag.getString(NBT_CALIBER))));
            }
            if (tag.hasKey(NBT_AMMO_TYPE)) {
                lines.add(I18n.format("susy.gun.tooltip.bullet_type",
                        I18n.format("susy.ammo_type." + tag.getString(NBT_AMMO_TYPE))));
            }
        }
    }

    public BulletTooltipBehavior() {}
}
