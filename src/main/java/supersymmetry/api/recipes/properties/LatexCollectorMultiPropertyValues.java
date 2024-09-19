package supersymmetry.api.recipes.properties;

import net.minecraft.block.state.IBlockState;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Arrays;

public class LatexCollectorMultiPropertyValues {
    public ArrayList<IBlockState> validBlockStates;

    public final String blockGroupName;

    public LatexCollectorMultiPropertyValues(@Nonnull String blockGroupName, @Nonnull IBlockState... validBlockStates) {
        this.validBlockStates = new ArrayList<>(Arrays.asList(validBlockStates));
        this.blockGroupName = blockGroupName;
    }

    public LatexCollectorMultiPropertyValues(@Nonnull String blockGroupName, @Nonnull ArrayList<IBlockState> validBlocks) {
        this.validBlockStates = validBlocks;
        this.blockGroupName = blockGroupName;
    }

    @Nonnull
    public ArrayList<IBlockState> getValidBlockStates() {
        return validBlockStates;
    }

    @Nonnull
    public String getBlockGroupName() {
        return blockGroupName;
    }
}
