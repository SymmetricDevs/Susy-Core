package supersymmetry.api.rocketry;

import net.minecraft.block.state.IBlockState;

public interface WeightedBlock {

    double getMass(IBlockState state);
}
