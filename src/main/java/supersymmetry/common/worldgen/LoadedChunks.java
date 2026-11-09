package supersymmetry.common.worldgen;

import net.minecraft.world.chunk.Chunk;

/** Read-only peek: unlike vanilla getLoadedChunk, does not clear the unload-queued flag. */
public interface LoadedChunks {

    Chunk susy$peekLoadedChunk(int x, int z);
}
