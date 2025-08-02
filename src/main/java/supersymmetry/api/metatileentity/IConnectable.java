package supersymmetry.api.metatileentity;

import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import net.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.Nullable;

public interface IConnectable {

    /// Null for self, same as [MultiblockControllerBase#getBaseTexture(IMultiblockPart)]
    ///
    /// @see supersymmetry.mixins.ctm.BlockMachineMixin
    @Nullable
    default IBlockState getVisualState(@Nullable IMultiblockPart part) {
        return null;
    }
}
