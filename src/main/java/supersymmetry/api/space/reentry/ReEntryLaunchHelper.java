package supersymmetry.api.space.reentry;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import supersymmetry.api.SusyLog;
import supersymmetry.common.entities.EntityDropPod;

/**
 * Static helper called when a drop pod in LEO (or another space dimension)
 * initiates de-orbit burn.
 * Usage (from wherever the de-orbit is triggered, e.g. a block right-click or
 * game event):
 * 
 * <pre>
 * ReEntryLaunchHelper.beginReEntry(
 *         ReEntryDimensions.EARTH_REENTRY_DIM_ID,
 *         List.of(player1, player2));
 * </pre>
 *
 * The helper:
 * 1. Spawns a drop pod in the re-entry corridor dimension at a randomised
 * X/Z offset so concurrent pods don't collide.
 * 2. Teleports all passengers into the pod.
 * 3. Registers a {@link ReEntrySequenceHandler} that drives the full sequence.
 */
public class ReEntryLaunchHelper {

    private static final int SPAWN_Y = 200;

    /**
     * Initiates the re-entry sequence for the given players.
     *
     * @param reEntryDimId the re-entry corridor dimension id
     * @param players      players to transfer (must be server-side EntityPlayerMP)
     */
    public static void beginReEntry(int reEntryDimId, List<EntityPlayerMP> players) {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) {
            SusyLog.logger.error("[ReEntry] beginReEntry called with no server running!");
            return;
        }

        ReEntryDimension config = ReEntryDimensions.REENTRY.get(reEntryDimId);
        if (config == null) {
            SusyLog.logger.error("[ReEntry] No ReEntryDimension registered for id " + reEntryDimId);
            return;
        }

        WorldServer reEntryWorld = server.getWorld(reEntryDimId);
        if (reEntryWorld == null) {
            SusyLog.logger.error("[ReEntry] Re-entry world not loaded for id " + reEntryDimId);
            return;
        }

        java.util.Random rng = new java.util.Random();
        int spawnX = rng.nextInt(128) - 64;
        int spawnZ = rng.nextInt(128) - 64;

        SusyLog.logger.info("[ReEntry] Spawning pod in dim " + reEntryDimId + " at (" + spawnX + ", " + SPAWN_Y + ", " +
                spawnZ + ")");

        EntityDropPod pod = new EntityDropPod(reEntryWorld, spawnX, SPAWN_Y, spawnZ);
        reEntryWorld.spawnEntity(pod);

        List<EntityPlayerMP> transferred = new ArrayList<>();
        for (EntityPlayerMP player : players) {
            try {
                player.dismountRidingEntity();
                server.getPlayerList().transferPlayerToDimension(
                        player, reEntryDimId,
                        new ReEntryTeleporter(reEntryWorld, spawnX, SPAWN_Y, spawnZ));
                transferred.add(player);
            } catch (Exception e) {
                SusyLog.logger.error("[ReEntry] Failed to transfer player " + player.getName(), e);
            }
        }

        for (EntityPlayerMP player : transferred) {
            ReEntryMountScheduler.schedule(player, pod);
        }

        ReEntrySequenceHandler sequence = new ReEntrySequenceHandler(config, pod, transferred);
        sequence.register();

        SusyLog.logger.info("[ReEntry] Sequence started for " + transferred.size() + " player(s).");
    }
}
