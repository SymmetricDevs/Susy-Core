package susycore.item;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.StandardMetaItem;
import net.minecraft.item.ItemStack;

public class SusyMetaItem extends StandardMetaItem {
    public SusyMetaItem() {
        super();
    }
    public void registerSubItems() {}
    public ItemStack getContainerItem(ItemStack stack) {
        return stack.copy();
    }
    protected String formatModelPath(MetaItem<?>.MetaValueItem metaValueItem) {
        return "metaitems/" + metaValueItem.unlocalizedName.replace('.', '/');
    }
}
