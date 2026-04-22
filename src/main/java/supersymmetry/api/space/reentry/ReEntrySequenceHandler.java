package supersymmetry.api.space.reentry;

import java.util.*;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import supersymmetry.api.SusyLog;
import supersymmetry.common.entities.EntityDropPod;

/**
 * Manages the server-side re-entry sequence for one or more players inside a
 * drop pod that is orbiting in a re-entry corridor dimension.
 *
 * Sequence:
 * 1. ORBIT – Players ride pod through one full orbit (orbitalPeriodTicks).
 * Earth renders below, growing imperceptibly.
 * 2. REENTRY – After one orbit, startReEntry() packet sent → client shader fires.
 * Pod spins down, accelerates vertically downward.
 * 3. DESCENT – Altitude counter counts down from transferAltitude.
 * Earth scale in the renderer grows each tick.
 * 4. TRANSFER – At altitude 0 (transfer threshold) players + pod are teleported
 * to the target dimension at the landing column, at transferAltitude
 * game-unit height. The pod then executes powered landing via its
 * existing logic (hasTakenOff / landing ticks).
 *
 * Usage:
 * ReEntrySequenceHandler handler = new ReEntrySequenceHandler(dimConfig, pod, players);
 * handler.register(); // subscribes to FML tick events
 */
public class ReEntrySequenceHandler {

    // ---- Phase enum ----
    public enum Phase {
        ORBIT,
        DEORBIT_BURN,
        DESCENT,
        TRANSFER,
        DONE
    }

    // ---- Config ----
    private final ReEntryDimension config;
    private final EntityDropPod pod;
    private final List<EntityPlayerMP> players;

    // ---- State ----
    private Phase phase = Phase.ORBIT;
    private long ticksInPhase = 0L;
    private float descentAltitude; // current simulated altitude in metres
    /** Ticks for the descent phase (from ignition to transfer altitude). */
    private static final long DESCENT_PHASE_TICKS = 400L;

    // ---- Randomised X/Z offset so pods don't stack ----
    private final int xOffset;
    private final int zOffset;

    public ReEntrySequenceHandler(
                                  ReEntryDimension config,
                                  EntityDropPod pod,
                                  List<EntityPlayerMP> players) {
        this.config = config;
        this.pod = pod;
        this.players = players;
        this.descentAltitude = config.transferAltitude;

        // Random horizontal offset [-32, 32] in each axis so pods don't overlap
        Random rng = new Random();
        this.xOffset = rng.nextInt(65) - 32;   // -32 … +32
        this.zOffset = rng.nextInt(65) - 32;
    }

    /** Subscribe this handler to Forge's server tick event. */
    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
        SusyLog.logger.info("[ReEntry] Sequence handler registered. xOff=" + xOffset + " zOff=" + zOffset);
    }

    private void unregister() {
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (phase == Phase.DONE) {
            unregister();
            return;
        }
        if (pod.isDead && phase != Phase.TRANSFER) {
            // Pod was destroyed before transfer – bail out gracefully
            SusyLog.logger.warn("[ReEntry] Drop pod died before transfer – aborting sequence.");
            phase = Phase.DONE;
            unregister();
            return;
        }

        ticksInPhase++;
        switch (phase) {
            case ORBIT:
                tickOrbit();
                break;
            case DEORBIT_BURN:
                tickDeorbitBurn();
                break;
            case DESCENT:
                tickDescent();
                break;
            case TRANSFER:
                tickTransfer();
                break;
            default:
                break;
        }
    }

    // ---- Phase tick methods ----

    // Tick durations (shorten for testing)
    private static final long ORBIT_TICKS = 200L;  // time orbiting sideways
    private static final long DEORBIT_BURN_TICKS = 100L;  // rotating to vertical + plasma buildup
    private static final long DESCENT_TICKS = 400L;  // vertical drop cinematic

    private void tickOrbit() {
        // Lock pod position
        freezePod();

        float progress = (float) ticksInPhase / ORBIT_TICKS;
        syncRendererOrbitProgress(progress);

        if (ticksInPhase >= ORBIT_TICKS) {
            phase = Phase.DEORBIT_BURN;
            ticksInPhase = 0L;
            sendStartReEntry();
        }
    }

    private void tickDeorbitBurn() {
        freezePod();

        // t goes 0→1 over the burn window
        float t = (float) ticksInPhase / DEORBIT_BURN_TICKS;
        syncRendererPlasma(t * 0.6f);           // plasma ramps up to 60%
        syncRendererPodRotation(t);             // 0 = sideways, 1 = vertical
        syncRendererDescentProgress(0f);        // earth not growing yet

        if (ticksInPhase >= DEORBIT_BURN_TICKS) {
            phase = Phase.DESCENT;
            ticksInPhase = 0L;
        }
    }

    private void tickDescent() {
        freezePod();

        float t = Math.min(1.0f, ticksInPhase / (float) DESCENT_TICKS);
        descentAltitude = config.transferAltitude * (1.0f - t);
        syncRendererDescentProgress(t);
        syncRendererPlasma(0.6f + t * 0.4f);   // plasma ramps to 100% at ground

        if (ticksInPhase >= DESCENT_TICKS) {
            phase = Phase.TRANSFER;
            ticksInPhase = 0L;
        }
    }

    private void tickTransfer() {
        // Only execute once
        if (ticksInPhase == 1) {
            performDimensionTransfer();
        }
        // Give a couple extra ticks for the transfer to complete, then finish.
        if (ticksInPhase >= 5) {
            phase = Phase.DONE;
        }
    }

    private void freezePod() {
        if (pod.isDead) return;
        pod.motionX = 0;
        pod.motionY = 0;
        pod.motionZ = 0;
        pod.setPosition(pod.posX, 100, pod.posZ);
    }

    // ---- Core transfer logic ----

    private void performDimensionTransfer() {
        MinecraftServer server = FMLCommonHandler.instance().getMinecraftServerInstance();
        if (server == null) return;

        WorldServer targetWorld = server.getWorld(config.targetDimensionId);
        if (targetWorld == null) {
            SusyLog.logger.error("[ReEntry] Target dimension " + config.targetDimensionId + " not found!");
            return;
        }

        // Find a safe spawn column in the target world
        // Centre on world spawn + our random offset so pods spread out
        BlockPos spawnBase = targetWorld.getSpawnPoint();
        int targetX = spawnBase.getX() + xOffset;
        int targetZ = spawnBase.getZ() + zOffset;
        // Place pod at transfer altitude (in blocks == metres at 1:1 scale)
        int targetY = (int) config.transferAltitude;

        SusyLog.logger.info("[ReEntry] Transferring pod+players to dim " + config.targetDimensionId + " at (" +
                targetX + ", " + targetY + ", " + targetZ + ")");

        // Spawn a new drop pod in the target dimension
        EntityDropPod newPod = new EntityDropPod(targetWorld, targetX, targetY, targetZ);
        targetWorld.spawnEntity(newPod);

        // Transfer each player into the new pod
        for (EntityPlayerMP player : players) {
            teleportPlayerToDim(server, player, config.targetDimensionId, targetX, targetY, targetZ, newPod);
        }

        // Destroy the old pod
        pod.setDead();
    }

    private void teleportPlayerToDim(
                                     MinecraftServer server,
                                     EntityPlayerMP player,
                                     int targetDimId,
                                     int x, int y, int z,
                                     EntityDropPod targetPod) {
        // Dismount first
        player.dismountRidingEntity();

        // Transfer dimension using vanilla mechanism
        server.getPlayerList().transferPlayerToDimension(player, targetDimId,
                new ReEntryTeleporter(server.getWorld(targetDimId), x, y, z));

        // Mount the new pod after a brief delay (1 tick) so position is established
        // We schedule it by marking the pod to auto-mount on next tick
        ReEntryMountScheduler.schedule(player, targetPod);
    }

    // ---- Client sync helpers ----
    // These send custom network packets to the client to update the renderer state.
    // The actual packet classes (ReEntryProgressPacket etc.) are defined separately.

    private void syncRendererOrbitProgress(float progress) {
        ReEntryNetworking.sendOrbitProgress(players, progress);
    }

    private void sendStartReEntry() {
        ReEntryNetworking.sendStartReEntry(players);
    }

    private void syncRendererPlasma(float intensity) {
        ReEntryNetworking.sendPlasmaIntensity(players, intensity);
    }

    private void syncRendererDescentProgress(float descentT) {
        ReEntryNetworking.sendDescentProgress(players, descentT);
    }

    private void syncRendererPodRotation(float t) {
        ReEntryNetworking.sendPodRotation(players, t);
    }
}
