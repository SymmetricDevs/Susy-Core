package org.taumc.celeritas.impl.render.terrain.compile.task;

import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildContext;
import org.embeddedt.embeddium.impl.render.chunk.compile.ChunkBuildOutput;
import org.embeddedt.embeddium.impl.util.task.CancellationToken;

/// Adapted and minimized from
/// [Celeritas](https://git.taumc.org/embeddedt/celeritas/src/branch/stonecutter/forge122/src/main/java/org/taumc/celeritas/impl/render/terrain/compile/task/ChunkBuilderMeshingTask.java)
public abstract class ChunkBuilderMeshingTask {

    @SuppressWarnings({"DataFlowIssue", "unused"})
    public ChunkBuildOutput execute(ChunkBuildContext context, CancellationToken cancellationToken) {
        // Dummy method body
        BlockPos.MutableBlockPos blockPos = null;
        Block block = null;
        block.canRenderInLayer(null, null);
        return null;
    }
}
