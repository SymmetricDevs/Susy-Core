package supersymmetry.common.item.behavior;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import java.util.List;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class dataCardBehavior implements IItemBehaviour {
  @Override
  public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("metaitem.data_card.tooltip.1"));
    if (itemStack.hasTagCompound()) {
      NBTTagCompound tag = itemStack.getTagCompound();
      String type = tag.getString("type");
      if (type != null) {
        lines.add(I18n.format("metaitem.data_card.type." + type));
      }
    }
  }
}
