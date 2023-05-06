package supersymmetry.common.item;

import gregtech.api.items.metaitem.MetaOreDictItem;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.ore.OrePrefix;
import supersymmetry.SuSyValues;
import supersymmetry.api.recipes.catalysts.CatalystInfo;
import supersymmetry.api.recipes.catalysts.CatalystGroup;
import supersymmetry.api.unification.ore.SusyOrePrefix;

public class CatalystItems {


    // Tiered catalysts
    public static MetaOreDictItem.OreDictValueItem[] OXIDATION_CATALYST_BED = new MetaOreDictItem.OreDictValueItem[14];
    public static MetaOreDictItem.OreDictValueItem[] REDUCTION_CATALYST_BED = new MetaOreDictItem.OreDictValueItem[14];

    public static void init() {
        // Catalysts
        addTieredCatalystItem(OXIDATION_CATALYST_BED, 0, 0x7f64b6, SusyOrePrefix.catalystBedOxidation);
        addTieredCatalystItem(REDUCTION_CATALYST_BED, 14, 0x377f8a, SusyOrePrefix.catalystBedReduction);
    }

    public static void initCatalysts() {
        addTieredCatalystGroup(OXIDATION_CATALYST_BED, CatalystGroup.OXIDATION_CATALYST_BEDS);
        addTieredCatalystGroup(REDUCTION_CATALYST_BED, CatalystGroup.REDUCTION_CATALYST_BEDS);
    }


    private static void addTieredCatalystItem (MetaOreDictItem.OreDictValueItem[] items, int id, int RGB, OrePrefix prefix) {

        for (int i = 0; i < items.length; i++) {
            items[i] = SuSyMetaItems.oreDictItem.addOreDictItem(id + i, SuSyValues.TierMaterials[i].toString(), RGB, MaterialIconSet.DULL, prefix);
        }

    }

    private static void addTieredCatalystGroup (MetaOreDictItem.OreDictValueItem[] items, CatalystGroup catalystGroup) {

        for (int i = 0; i < items.length; i++) {
            catalystGroup.add(items[i].getItemStack(), new CatalystInfo(
                    i,
                    1,
                    1,
                    1.25
            ));
        }
    }
}
