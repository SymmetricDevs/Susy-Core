package supersymmetry.mixins.minecraft;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import supersymmetry.common.worldgen.LoadedChunks;

@Mixin(ChunkProviderServer.class)
public abstract class LoadedChunksMixin implements LoadedChunks {

    @Shadow
    @Final
    private Long2ObjectMap<Chunk> loadedChunks;

    public Chunk susy$peekLoadedChunk(int x, int z) {
        return loadedChunks.get(ChunkPos.asLong(x, z));
    }
}
