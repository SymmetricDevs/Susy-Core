package supersymmetry.api.recipes.catalysts;

import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;


public class CatalystGroup {

    private static NonNullList<CatalystGroup> catalystGroups = NonNullList.create();
    public static CatalystGroup OXIDATION_CATALYST_BEDS = new CatalystGroup("oxidation_catalyst_beds");
    public static CatalystGroup REDUCTION_CATALYST_BEDS = new CatalystGroup("reduction_catalyst_beds");
    private final String name;
    private CatalystInfos catalystInfos = new CatalystInfos();
    public CatalystGroup(String name) {
        this.name = name;
        catalystGroups.add(this);
    }

    public CatalystInfos getCatalystInfos() {
        return this.catalystInfos;
    }
    public void add(ItemStack itemStack, CatalystInfo catalystInfo) {
        this.catalystInfos.put(itemStack, catalystInfo);
    }
    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    public static NonNullList<CatalystGroup> getCatalystGroups() {
        return catalystGroups;
    }
}
