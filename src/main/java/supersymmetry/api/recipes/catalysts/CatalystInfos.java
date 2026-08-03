package supersymmetry.api.recipes.catalysts;

import java.util.Map;
import java.util.stream.Stream;

import org.jspecify.annotations.NonNull;

import net.minecraft.item.ItemStack;

import gregtech.api.util.ItemStackHashStrategy;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;

public class CatalystInfos {

    private final Map<ItemStack, CatalystInfo> map = new Object2ObjectOpenCustomHashMap<>(
            ItemStackHashStrategy.comparingAllButCount());

    public void put(@NonNull ItemStack itemStack, @NonNull CatalystInfo catalystInfo) {
        map.put(itemStack, catalystInfo);
    }

    public CatalystInfo get(@NonNull ItemStack itemStack) {
        return map.get(itemStack);
    }

    @NonNull
    public Stream<Map.Entry<ItemStack, CatalystInfo>> streamEntries() {
        return map.entrySet().stream();
    }
}
