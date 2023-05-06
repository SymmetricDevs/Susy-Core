package supersymmetry.api.recipes.catalysts;

import gregtech.api.util.ItemStackHashStrategy;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import net.minecraft.item.ItemStack;

import java.util.Map;

public class CatalystInfos {

    private Map<ItemStack, CatalystInfo> map = new Object2ObjectOpenCustomHashMap<>(ItemStackHashStrategy.comparingItemDamageCount());

    public void put(ItemStack itemStack, CatalystInfo catalystInfo) {
        map.put(itemStack, catalystInfo);
    }

    public CatalystInfo get(ItemStack itemStack) {
        return map.get(itemStack);
    }

    public Map<ItemStack, CatalystInfo> getMap() {
        return map;
    }
}
