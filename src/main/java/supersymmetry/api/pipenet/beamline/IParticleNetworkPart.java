package supersymmetry.api.pipenet.beamline;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IParticleNetworkPart {

    @NotNull
    BeamLineType getBeamLineType();

    @Nullable
    static IParticleNetworkPart tryGet(World world, BlockPos pos) {
        return tryGet(world, pos, world.getBlockState(pos));
    }

    @Nullable
    static IParticleNetworkPart tryGet(World world, BlockPos pos, IBlockState blockState) {
        return blockState.getBlock() instanceof IParticleNetworkPart part ? part : IBeamLineEndpoint.tryGet(world, pos);
    }

}
