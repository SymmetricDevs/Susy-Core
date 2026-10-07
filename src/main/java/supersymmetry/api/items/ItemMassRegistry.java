package supersymmetry.api.items;

import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.RegistryDefaulted;

import org.jspecify.annotations.NonNull;

import dev.tianmi.sussypatches.api.util.ItemAndMeta;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

public class ItemMassRegistry extends RegistryDefaulted<ItemAndMeta, Integer> {

    private static final ItemMassRegistry INSTANCE = new ItemMassRegistry();

    private ItemMassRegistry() {
        super(0);
    }

    public static int getMass(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return INSTANCE.getMap().getOrDefault(new ItemAndMeta(stack), 0);
    }

    public static void setMass(ItemStack itemStack, int mass) {
        INSTANCE.putObject(new ItemAndMeta(itemStack), mass);
    }

    @Override
    protected @NonNull Map<ItemAndMeta, Integer> createUnderlyingMap() {
        return new Object2IntOpenHashMap<>();
    }

    private Map<ItemAndMeta, Integer> getMap() {
        return registryObjects;
    }
}
