package supersymmetry.mixins.minecraft;

import java.util.Map;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AnvilChunkLoader.class)
public interface AnvilPendingAccessor {

    @Accessor("chunksToSave")
    Map<ChunkPos, NBTTagCompound> susy$getPendingSaves();
}
