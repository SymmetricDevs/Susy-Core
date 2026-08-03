package supersymmetry.api.recipes.properties;

import java.util.ArrayList;
import java.util.Arrays;

import org.jspecify.annotations.NonNull;

import net.minecraft.block.state.IBlockState;

public class PseudoMultiPropertyValues {

    public ArrayList<IBlockState> validBlockStates;

    public final String blockGroupName;

    public PseudoMultiPropertyValues(@NonNull String blockGroupName, @NonNull IBlockState... validBlockStates) {
        this.validBlockStates = new ArrayList<>(Arrays.asList(validBlockStates));
        this.blockGroupName = blockGroupName;
    }

    public PseudoMultiPropertyValues(@NonNull String blockGroupName, @NonNull ArrayList<IBlockState> validBlocks) {
        this.validBlockStates = validBlocks;
        this.blockGroupName = blockGroupName;
    }

    @NonNull
    public ArrayList<IBlockState> getValidBlockStates() {
        return validBlockStates;
    }

    @NonNull
    public String getBlockGroupName() {
        return blockGroupName;
    }
}
