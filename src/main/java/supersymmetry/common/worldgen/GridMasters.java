package supersymmetry.common.worldgen;

import java.io.DataInputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraft.world.chunk.storage.RegionFileCache;
import net.minecraft.world.gen.ChunkProviderServer;

import gregtech.api.worldgen.generator.GTWorldGenCapability;
import supersymmetry.mixins.minecraft.AnvilPendingAccessor;

/** Read just the existing capability metadata; never load or construct a Chunk to obtain two heights. */
public final class GridMasters {

    public static final class Lookup {

        public final boolean handled;
        public final GTWorldGenCapability value;

        private Lookup(boolean handled, GTWorldGenCapability value) {
            this.handled = handled;
            this.value = value;
        }
    }

    private static final Lookup DELEGATE = new Lookup(false, null);
    private static final Lookup NO_MASTER = new Lookup(true, null);
    private static long lastError;

    private GridMasters() {}

    public static Lookup find(World world, int gridX, int gridZ) {
        if (!WorldgenFixes.gridMetadata() || !(world instanceof WorldServer) ||
                !((WorldServer) world).isCallingFromMinecraftThread())
            return DELEGATE;
        ChunkProviderServer provider = (ChunkProviderServer) world.getChunkProvider();
        if (!(provider.chunkLoader instanceof AnvilChunkLoader)) return DELEGATE;
        AnvilChunkLoader loader = (AnvilChunkLoader) provider.chunkLoader;
        try {
            // Exactly the same scan order and generated-chunk predicate as upstream.
            for (int x = 0; x < 3; x++) for (int z = 0; z < 3; z++) {
                int cx = gridX * 3 + x, cz = gridZ * 3 + z;
                if (!world.isChunkGeneratedAt(cx, cz)) continue;
                Chunk chunk = ((LoadedChunks) provider).susy$peekLoadedChunk(cx, cz);
                if (chunk != null) return new Lookup(true, chunk.getCapability(GTWorldGenCapability.CAPABILITY, null));
                NBTTagCompound root = ((AnvilPendingAccessor) (Object) loader).susy$getPendingSaves()
                        .get(new ChunkPos(cx, cz));
                if (root == null) {
                    try (DataInputStream in = RegionFileCache.getChunkInputStream(loader.chunkSaveLocation, cx, cz)) {
                        if (in == null) return DELEGATE;
                        root = CompressedStreamTools.read(in);
                    }
                }
                NBTTagCompound level = root.getCompoundTag("Level");
                // Corrupt/misplaced data is left to the stock loader's recovery logic.
                if (level.getInteger("xPos") != cx || level.getInteger("zPos") != cz) return DELEGATE;
                GTWorldGenCapability value = new GTWorldGenCapability();
                value.readFromNBT(level.getCompoundTag("ForgeCaps").getCompoundTag("gregtech:worldgen"));
                return new Lookup(true, value);
            }
            return NO_MASTER;
        } catch (Exception ex) {
            if (System.nanoTime() - lastError > 60_000_000_000L) {
                WorldgenFixes.LOG.warn("Cannot read saved worldgen heights; using the stock chunk loader", ex);
                lastError = System.nanoTime();
            }
            return DELEGATE;
        }
    }
}
