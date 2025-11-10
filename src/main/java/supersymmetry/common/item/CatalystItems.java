package supersymmetry.common.item;

import gregtech.api.items.metaitem.MetaOreDictItem;
import gregtech.api.unification.material.info.MaterialIconSet;
import gregtech.api.unification.ore.OrePrefix;
import supersymmetry.SuSyValues;
import supersymmetry.api.recipes.catalysts.CatalystGroup;
import supersymmetry.api.recipes.catalysts.CatalystInfo;
import supersymmetry.api.unification.material.info.SuSyMaterialIconSets;
import supersymmetry.api.unification.ore.SusyOrePrefix;
import supersymmetry.common.recipes.CatalystGroups;

public final class CatalystItems {

    // Tiered catalysts
    public static final MetaOreDictItem.OreDictValueItem[] OXIDATION_CATALYST_BED = new MetaOreDictItem.OreDictValueItem[14];
    public static final MetaOreDictItem.OreDictValueItem[] REDUCTION_CATALYST_BED = new MetaOreDictItem.OreDictValueItem[14];

    public static MetaOreDictItem.OreDictValueItem CRACKING_CATALYST_BED;

    private CatalystItems() {}

    public static void init() {
        // Catalysts
        addTieredCatalystItem(OXIDATION_CATALYST_BED, 0, 0x7f64b6, SusyOrePrefix.catalystBedOxidation);
        addTieredCatalystItem(REDUCTION_CATALYST_BED, 14, 0x377f8a, SusyOrePrefix.catalystBedReduction);

        CRACKING_CATALYST_BED = SuSyMetaItems.oreDictItem.addOreDictItem(28, "standard", 0x728a7a, MaterialIconSet.DULL,
                SusyOrePrefix.catalystBedCracking);

        initCatalysts();
    }

    public static void initCatalysts() {
        addTieredCatalystGroup(OXIDATION_CATALYST_BED, CatalystGroups.OXIDATION_CATALYST_BEDS);
        addTieredCatalystGroup(REDUCTION_CATALYST_BED, CatalystGroups.REDUCTION_CATALYST_BEDS);

        CatalystGroups.CRACKING_CATALYST_BEDS.add(CRACKING_CATALYST_BED.getItemStack(), new CatalystInfo(
                CatalystInfo.NO_TIER,
                1,
                0.95,
                1.25));
    }

    public static void addTieredCatalystItem(MetaOreDictItem.OreDictValueItem[] items, int id, int RGB,
                                             OrePrefix prefix) {
        for (int i = 0; i < items.length; i++) {
            items[i] = SuSyMetaItems.oreDictItem.addOreDictItem(id + i, SuSyValues.TierMaterials[i].toString(), RGB,
                    SuSyMaterialIconSets.TIERS[i], prefix);
        }
    }

    public static void addTieredCatalystGroup(MetaOreDictItem.OreDictValueItem[] items, CatalystGroup catalystGroup) {
        for (int i = 0; i < items.length; i++) {
            catalystGroup.add(items[i].getItemStack(), new CatalystInfo(
                    i,
                    1,
                    0.95,
                    1.25));
        }
    }
}
