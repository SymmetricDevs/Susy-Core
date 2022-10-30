package susycore.item;

import gregtech.api.items.metaitem.MetaItem;

import java.util.List;

public class SusyMetaItems {
    public static List<MetaItem<?>> ITEMS = MetaItem.getMetaItems();
    public static SusyMetaItem META_ITEM;
    public static SusyOredictItem SHAPED_ITEM = new SusyOredictItem((short) 0);


    public static void init() {
        META_ITEM = new SusyMetaItem();
        META_ITEM.setRegistryName("susy_meta_item");
        SHAPED_ITEM.setRegistryName("susy_oredict_item");
    }
}
