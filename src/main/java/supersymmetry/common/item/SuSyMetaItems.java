package supersymmetry.common.item;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.StandardMetaItem;

public class SuSyMetaItems {

    private static StandardMetaItem metaItem;

    public static MetaItem.MetaValueItem CATALYST_BED_SUPPORT_GRID;

    public static void initMetaItems() {
        metaItem = new StandardMetaItem();
        metaItem.setRegistryName("meta_item");
    }

    public static void initSubItems() {
        initMetaItem();
    }

    private static void initMetaItem() {
        // initialize metaitems here
        CATALYST_BED_SUPPORT_GRID = metaItem.addItem(1, "catalyst_bed_support_grid");
    }
}
