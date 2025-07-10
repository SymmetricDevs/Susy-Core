package supersymmetry.mixins.gregtech;

import git.jbredwards.fluidlogged_api.api.asm.impl.IChunkProvider;
import gregtech.api.util.world.DummyWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.fml.common.Optional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Optional.Interface(modid = "fluidlogged_api", iface = "git.jbredwards.fluidlogged_api.api.asm.impl.IChunkProvider")
@Mixin(targets = "gregtech.client.renderer.handler.MultiblockPreviewRenderer$TargetBlockAccess", remap = false)
public class TargetBlockAccessMixin implements IChunkProvider {

    @Shadow
    @Final
    private IBlockAccess delegate;

    @Override
    public @Nullable Chunk getChunkFromBlockCoords(@NotNull BlockPos blockPos) {
        /// [delegate] can only be instances of [DummyWorld]
        /// According to the source code
        return ((DummyWorld) delegate).getChunk(blockPos);
    }
}
