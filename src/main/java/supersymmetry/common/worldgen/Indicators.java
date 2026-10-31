package supersymmetry.common.worldgen;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

import gregtech.api.worldgen.populator.SurfaceBlockPopulator;
import gregtech.api.worldgen.populator.SurfaceRockPopulator;
import supersymmetry.mixins.gregtech.SurfaceBlockPopulatorInvoker;

public final class Indicators {

    private static final Map<World, IndicatorData> DATA = new WeakHashMap<>();
    private static long lastError;

    private Indicators() {}

    public static IndicatorData data(World world) {
        IndicatorData value = DATA.get(world);
        if (value == null) {
            value = (IndicatorData) world.getPerWorldStorage().getOrLoadData(IndicatorData.class, IndicatorData.NAME);
            if (value == null) {
                value = new IndicatorData();
                world.getPerWorldStorage().setData(IndicatorData.NAME, value);
            }
            DATA.put(world, value);
        }
        return value;
    }

    public static void unload(World world) {
        DATA.remove(world);
    }

    public static void clear() {
        DATA.clear();
    }

    public static boolean defer(World world, BlockPos position, int sourceX, int sourceZ, byte kind,
                                IBlockState state) {
        if (!WorldgenFixes.deferIndicators() || !(world instanceof WorldServer) || world.isRemote ||
                !((WorldServer) world).isCallingFromMinecraftThread())
            return false;
        ChunkProviderServer provider = (ChunkProviderServer) world.getChunkProvider();
        int x = position.getX() >> 4, z = position.getZ() >> 4;
        Chunk loaded = ((LoadedChunks) provider).susy$peekLoadedChunk(x, z);
        if (!BoundaryPolicy.shouldDefer(sourceX, sourceZ, position.getX(), position.getZ(),
                loaded != null, loaded != null && loaded.isTerrainPopulated()))
            return false;
        IndicatorData data = data(world);
        // Back-pressure preserves original behavior rather than dropping indicators on extreme pregeneration jobs.
        if (data.pending.size() >= 262144) return false;
        IndicatorData.Task task = new IndicatorData.Task(position.toLong(), kind,
                NBTUtil.writeBlockState(new NBTTagCompound(), state));
        if (data.pending.add(BoundaryPolicy.chunkKey(x, z), task.key(), task)) {
            data.markDirty();
        }
        return true;
    }

    public static void flush(WorldServer world, long deadline) {
        IndicatorData data = DATA.get(world);
        if (data == null) return;
        ChunkProviderServer provider = (ChunkProviderServer) world.getChunkProvider();
        int limit = Math.min(64, data.pending.chunkCount());
        for (int checked = 0; checked < limit && !data.pending.isEmpty(); checked++) {
            if (System.nanoTime() >= deadline) break;
            long key = data.pending.firstChunk();
            Chunk chunk = ((LoadedChunks) provider).susy$peekLoadedChunk((int) (key >> 32), (int) key);
            data.pending.rotate(key);
            if (chunk == null || !chunk.isTerrainPopulated()) continue;
            Map.Entry<String, IndicatorData.Task> entry;
            while ((entry = data.pending.firstEntry(key)) != null) {
                if (System.nanoTime() >= deadline) break;
                try {
                    IndicatorData.Task task = entry.getValue();
                    IBlockState state = NBTUtil.readBlockState(task.state);
                    if (state.getBlock() == net.minecraft.init.Blocks.AIR || task.kind < 0 || task.kind > 1)
                        throw new IllegalStateException(
                                "Missing indicator block or unsupported task kind; retaining saved task");
                    BlockPos pos = BlockPos.fromLong(task.position);
                    if (BoundaryPolicy.chunkKey(pos.getX() >> 4, pos.getZ() >> 4) != key)
                        throw new IllegalStateException(
                                "Saved indicator destination differs from its bucket; retaining task");
                    if (task.kind == 0) {
                        // Same surface selection/eligibility as upstream; no neighbour notifications during generation.
                        BlockPos top = SurfaceRockPopulator.findSpawnHeight(world, pos);
                        if (top.getY() > 20 && top.getY() < world.provider.getActualHeight() &&
                                world.getBlockState(top).getBlock().isReplaceable(world, top) &&
                                !world.getBlockState(top).getMaterial().isLiquid()) {
                            world.setBlockState(top, state, 18);
                        }
                    } else {
                        ((SurfaceBlockPopulatorInvoker) new SurfaceBlockPopulator(state))
                                .susy$generateSurfaceBlock(world, pos);
                    }
                    data.pending.remove(key, entry.getKey());
                    data.markDirty();
                } catch (Exception ex) {
                    // Keep the task for retry rather than silently losing saved work.
                    data.pending.rotateEntry(key, entry.getKey());
                    if (System.nanoTime() - lastError > 60_000_000_000L) {
                        WorldgenFixes.LOG.error("Deferred indicator retained after placement failure", ex);
                        lastError = System.nanoTime();
                    }
                    break;
                }
            }
        }
    }
}
