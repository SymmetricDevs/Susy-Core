package supersymmetry.api.recipes.catalysts;

import java.util.Map;
import java.util.stream.Stream;

import javax.annotation.Nonnull;

import net.minecraft.item.ItemStack;

import gregtech.api.util.ItemStackHashStrategy;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

public class CatalystInfos {

    private final Map<ItemStack, CatalystInfo> map = new Object2ObjectOpenCustomHashMap<>(
            ItemStackHashStrategy.comparingAllButCount());

    public void put(@Nonnull ItemStack itemStack, @Nonnull CatalystInfo catalystInfo) {
        map.put(itemStack, catalystInfo);
    }

    public CatalystInfo get(@Nonnull ItemStack itemStack) {
        return map.get(itemStack);
    }

    @Nonnull
    public Stream<Map.Entry<ItemStack, CatalystInfo>> streamEntries() {
        return map.entrySet().stream();
    }
}
