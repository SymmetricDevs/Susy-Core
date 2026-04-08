package supersymmetry.common.faction;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import java.util.HashMap;
import java.util.Map;

public class FactionBaselineRegistry {

    private static final Map<ResourceLocation, Integer> BASELINE_HATE = new HashMap<>();

    public static void add(ResourceLocation advancement, int hate) {
        BASELINE_HATE.put(advancement, hate);
    }

    public static int getBaseline(EntityPlayerMP player) {
        int max = 0;

        AdvancementManager manager = ObfuscationReflectionHelper.getPrivateValue(
                World.class, player.world, "field_191951_C"
        );

        for (Map.Entry<ResourceLocation, Integer> entry : BASELINE_HATE.entrySet()) {
            Advancement adv = manager.getAdvancement(entry.getKey());
            if (adv != null && player.getAdvancements().getProgress(adv).isDone()) {
                max = Math.max(max, entry.getValue());
            }
        }

        return max;
    }
}
