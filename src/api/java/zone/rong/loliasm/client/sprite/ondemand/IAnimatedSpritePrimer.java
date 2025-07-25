package zone.rong.loliasm.client.sprite.ondemand;

import net.minecraft.client.renderer.chunk.CompiledChunk;

/// Adapted and minimized from
/// [CensoredASM](https://github.com/LoliKingdom/LoliASM/blob/master/src/main/java/zone/rong/loliasm/client/sprite/ondemand/IAnimatedSpritePrimer.java)
public interface IAnimatedSpritePrimer {
    ThreadLocal<CompiledChunk> CURRENT_COMPILED_CHUNK = null;
}
