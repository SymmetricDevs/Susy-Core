package supersymmetry.common.item;

import gregtech.api.items.metaitem.StandardMetaItem;

public class SuSyMetaItems {

    private static StandardMetaItem metaItem;

    public static void initMetaItems() {
        //metaItem = new StandardMetaItem();
        //metaItem.setRegistryName("meta_item");
    }

    public static void initSubItems() {
        initMetaItem();
    }

    private static void initMetaItem() {
        // initialize metaitems here
        // ex: META_VALUE_ITEM = metaitem.addItem(id, "name");
    }
}
