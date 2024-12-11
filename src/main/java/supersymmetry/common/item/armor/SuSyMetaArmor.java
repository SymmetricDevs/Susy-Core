package supersymmetry.common.item.armor;

import gregtech.api.items.armor.ArmorMetaItem;
import supersymmetry.common.item.SuSyMetaItems;

public class SuSyMetaArmor extends ArmorMetaItem<ArmorMetaItem<?>.ArmorMetaValueItem> {

    @Override
    public void registerSubItems() {
        SuSyMetaItems.JET_WINGPACK = addItem(1, "jet_wingpack").setArmorLogic(new JetWingpack());
    }
}
