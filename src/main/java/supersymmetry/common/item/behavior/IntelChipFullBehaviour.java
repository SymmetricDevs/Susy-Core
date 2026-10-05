package supersymmetry.common.item.behavior;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import gregtech.api.items.metaitem.stats.IItemBehaviour;

public class IntelChipFullBehaviour implements IItemBehaviour {

    public static final IntelChipFullBehaviour INSTANCE = new IntelChipFullBehaviour();

    private static final String TAG_ROOT = "susy";
    private static final String TAG_FACTION = "faction";

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, List<String> lines) {
        NBTTagCompound tag = stack.getSubCompound(TAG_ROOT);

        if (tag == null || tag.getString(TAG_FACTION).isEmpty()) {
            lines.add(I18n.format("item.susy.intel_chip_full.no_faction"));
            return;
        }

        lines.add(I18n.format("item.susy.intel_chip_full.faction", tag.getString(TAG_FACTION)));
    }
}
