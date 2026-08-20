package supersymmetry.mixins.gregtech;

import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import supersymmetry.api.pipelike.ConnectablePipe;
import supersymmetry.common.pipelike.tanklessfluid.tile.TileEntityTanklessFluidPipe;

@Mixin(value = TileEntityFluidPipe.class, remap = false)
public abstract class TileEntityFluidPipeMixin implements ConnectablePipe {

    @Unique
    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public boolean canConnectWith(@NonNull IPipeTile<?, ?> other) {
        return other instanceof TileEntityTanklessFluidPipe;
    }
}
