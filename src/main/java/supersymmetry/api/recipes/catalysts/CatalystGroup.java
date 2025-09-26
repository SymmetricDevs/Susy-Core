package supersymmetry.api.recipes.catalysts;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

public class CatalystGroup {

    private static final List<CatalystGroup> catalystGroups = new ArrayList<>();

    private final String name;
    private final CatalystInfos catalystInfos = new CatalystInfos();

    public CatalystGroup(@Nonnull String registry_name) {
        this.name = registry_name;
        catalystGroups.add(this);
    }

    public CatalystInfos getCatalystInfos() {
        return this.catalystInfos;
    }

    public void add(@Nonnull ItemStack itemStack, @Nonnull CatalystInfo catalystInfo) {
        if (itemStack == ItemStack.EMPTY) return;
        this.catalystInfos.put(itemStack, catalystInfo);
    }

    @Override
    public int hashCode() {
        return this.name.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CatalystGroup that = (CatalystGroup) o;

        return getName().equals(that.getName());
    }

    @Nonnull
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "CatalystGroup{" +
                "name='" + name + '\'' +
                '}';
    }

    public static List<CatalystGroup> getCatalystGroups() {
        return catalystGroups;
    }
}
