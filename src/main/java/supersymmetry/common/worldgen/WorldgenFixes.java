package supersymmetry.common.worldgen;

import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import supersymmetry.SusyConfig;

public final class WorldgenFixes {

    public static final Logger LOG = LogManager.getLogger("SuSyWorldgen");
    private static int tick;

    private WorldgenFixes() {}

    public static void init() {
        MinecraftForge.EVENT_BUS.register(new WorldgenFixes());
    }

    public static boolean gridMetadata() {
        return SusyConfig.worldGeneration.enabled && SusyConfig.worldGeneration.gridMetadata;
    }

    public static boolean deferIndicators() {
        return SusyConfig.worldGeneration.enabled && SusyConfig.worldGeneration.deferIndicators;
    }

    public static boolean recurrentComplex() {
        return SusyConfig.worldGeneration.enabled && SusyConfig.worldGeneration.recurrentComplex;
    }

    public static boolean adaptiveCache() {
        return SusyConfig.worldGeneration.enabled && SusyConfig.worldGeneration.adaptiveCache;
    }

    @SubscribeEvent
    public void load(WorldEvent.Load event) {
        if (event.getWorld() instanceof WorldServer) Indicators.data(event.getWorld());
    }

    @SubscribeEvent
    public void unload(WorldEvent.Unload event) {
        if (event.getWorld() instanceof WorldServer) Indicators.unload(event.getWorld());
    }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        tick = (tick + 1) & Integer.MAX_VALUE;
        long deadline = System.nanoTime() + 2_000_000L;
        WorldServer[] worlds = DimensionManager.getWorlds();
        for (int i = 0; i < worlds.length && System.nanoTime() < deadline; i++)
            Indicators.flush(worlds[(tick + i) % worlds.length], deadline);
    }
}
