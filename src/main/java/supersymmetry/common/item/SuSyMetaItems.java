package supersymmetry.common.item;

import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.StandardMetaItem;
import gregtech.api.items.metaitem.stats.IItemComponent;
import gregtech.common.items.behaviors.TooltipBehavior;
import net.minecraft.client.resources.I18n;

public class SuSyMetaItems {

    private static StandardMetaItem metaItem;

    public static MetaItem<?>.MetaValueItem CONVEYOR_STEAM;
    public static MetaItem<?>.MetaValueItem PUMP_STEAM;

    public static void initMetaItems() {
        metaItem = new StandardMetaItem();
        metaItem.setRegistryName("meta_item");
//        initMetaItem();
    }

    public static void initSubItems() {
        initMetaItem();
    }

    private static void initMetaItem() {
        // initialize metaitems here
        // ex: META_VALUE_ITEM = metaitem.addItem(id, "name");
        CONVEYOR_STEAM = metaItem.addItem(12000, "conveyor.steam").addComponents(new TooltipBehavior((lines) -> {
            lines.add(I18n.format("metaitem.conveyor.module.tooltip", new Object[0]));
            lines.add(I18n.format("gregtech.universal.tooltip.item_transfer_rate", new Object[]{4}));
        }));
        PUMP_STEAM = metaItem.addItem(12001, "pump.steam").addComponents(new TooltipBehavior((lines) -> {
            lines.add(I18n.format("metaitem.electric.pump.tooltip", new Object[0]));
            lines.add(I18n.format("gregtech.universal.tooltip.fluid_transfer_rate", new Object[]{32}));
        }));
    }
}
