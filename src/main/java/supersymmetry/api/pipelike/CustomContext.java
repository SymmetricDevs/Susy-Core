package supersymmetry.api.pipelike;

import codechicken.lib.lighting.LightMatrix;
import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.client.renderer.pipe.PipeRenderer;
import gregtech.client.renderer.pipe.PipeRenderer.PipeRenderContext;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.Nullable;

@MixinExtension(PipeRenderer.class)
public interface CustomContext {

    /// @param pos null for item rendering
    /// @param lightMatrix null for item rendering
    /// @return a new instance of [PipeRenderContext]
    @Contract(value = "_, _, _, _, _ -> new", pure = true)
    default PipeRenderContext createRenderContext(@Nullable BlockPos pos, @Nullable LightMatrix lightMatrix, int connections, int blockedConnections, float thickness) {
        return new PipeRenderContext(pos, lightMatrix, connections, blockedConnections, thickness);
    }

    @Contract(value = "_, _, _ -> new", pure = true)
    default PipeRenderContext createItemRenderContext(int connections, int blockedConnections, float thickness) {
        return createRenderContext(null, null, connections, blockedConnections, thickness);
    }
}
