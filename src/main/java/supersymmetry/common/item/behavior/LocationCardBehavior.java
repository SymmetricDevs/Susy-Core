package supersymmetry.common.item.behavior;

import static supersymmetry.common.metatileentities.multi.electric.MetaTileEntityCargoDronePad.*;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import gregtech.api.items.metaitem.stats.IItemBehaviour;

public class LocationCardBehavior implements IItemBehaviour {

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        if (itemStack.hasTagCompound()) {
            NBTTagCompound tag = itemStack.getSubCompound(TAG_ROOT);
            if (tag.hasKey(TAG_X) && tag.hasKey(TAG_Y) && tag.hasKey(TAG_Z)) {
                lines.add(I18n.format("susy.location_card.tooltip.coords") + "(" + tag.getInteger(TAG_X) +
                        ", " + tag.getInteger(TAG_Y) + ", " + tag.getInteger(TAG_Z) + ")");
            } else {
                lines.add(I18n.format("susy.location_card.tooltip.no_coords"));
            }
        } else {
            lines.add(I18n.format("susy.location_card.tooltip.no_coords"));
        }
    }

    public LocationCardBehavior() {}
}
