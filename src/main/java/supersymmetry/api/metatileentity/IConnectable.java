package supersymmetry.api.metatileentity;

import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import net.minecraft.block.state.IBlockState;
import org.jetbrains.annotations.Nullable;
import supersymmetry.client.renderer.textures.custom.VisualStateRenderer;

public interface IConnectable {

    /// Null for self, same as [MultiblockControllerBase#getBaseTexture(IMultiblockPart)]
    @Nullable
    default VisualStateRenderer getVisualRenderer(@Nullable IMultiblockPart part) {
        return null;
    }

    /// Null for self, same as [MultiblockControllerBase#getBaseTexture(IMultiblockPart)]
    ///
    /// @see supersymmetry.mixins.ctm.BlockMachineMixin
    @Nullable
    default IBlockState getVisualState(@Nullable IMultiblockPart part) {
        var stateRenderer = getVisualRenderer(part);
        if (stateRenderer != null) {
            return stateRenderer.getVisualState();
        }
        return null;
    }
}
