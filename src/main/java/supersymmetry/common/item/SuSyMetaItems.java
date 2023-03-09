package supersymmetry.common.item;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.StandardMetaItem;

public class SuSyMetaItems {

    private static StandardMetaItem metaItem;
    public static MetaItem<?>.MetaValueItem COAGULATED_LATEX;

    public static void initMetaItems() {
        metaItem = new StandardMetaItem();
        metaItem.setRegistryName("meta_item");
    }

    public static void initSubItems() {
        initMetaItem();
    }

    private static void initMetaItem() {
        // initialize metaitems here
        // ex: META_VALUE_ITEM = metaitem.addItem(id, "name");
        COAGULATED_LATEX = metaItem.addItem(12500, "coagulated_latex");
    }
}
