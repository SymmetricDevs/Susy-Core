package supersymmetry.api.items;

import java.util.Map;

import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.RegistryDefaulted;

import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import com.google.common.collect.HashBiMap;

import dev.tianmi.sussypatches.api.util.ItemAndMeta;

public class ItemMassRegistry extends RegistryDefaulted<ItemAndMeta, Integer> {

    private static final ItemMassRegistry INSTANCE = new ItemMassRegistry();

    private ItemMassRegistry() {
        super(0);
    }

    @Nullable public static Integer getMass(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        return INSTANCE.getMap().get(new ItemAndMeta(stack));
    }

    public static void setMass(ItemStack itemStack, int mass) {
        INSTANCE.putObject(new ItemAndMeta(itemStack), mass);
    }

    @Override
    protected @NonNull Map<ItemAndMeta, Integer> createUnderlyingMap() {
        return HashBiMap.create();
    }

    private Map<ItemAndMeta, Integer> getMap() {
        return registryObjects;
    }
}
