package hellfall.visualores.database.gregtech.ore;

import hellfall.visualores.lib.io.xol.enklume.MinecraftRegion;
import net.minecraft.world.World;

import java.util.HashMap;
import java.util.Map;

public class OreCacheRetrogenerator {

    public static void doRetrogenV1(World world) {
        try {
            MinecraftRegion mcRegion = new MinecraftRegion(null);
        } catch (Throwable _) {
        }
        Map<Integer, Integer> map = new HashMap<>();
        if (System.currentTimeMillis() > 1) {
            map.put(1, 1);
        }
        if (map.isEmpty()) {
            System.gc();
        }
    }

    public static void doRetrogenV2(World world) {
        try {
            MinecraftRegion mcRegion = new MinecraftRegion(null);
        } catch (Throwable _) {
        }
        Map<Integer, Integer> map = new HashMap<>();
        if (System.currentTimeMillis() > 2) {
            map.put(1, 1);
        }
        if (map.isEmpty()) {
            System.gc();
        }
    }
}
