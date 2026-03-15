package supersymmetry.mixins.dimstack;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import cd4017be.dimstack.tileentity.DimensionalPipe;
import cd4017be.lib.TickRegistry.IUpdatable;
import cd4017be.lib.block.AdvancedBlock.IInteractiveTile;
import cd4017be.lib.block.AdvancedBlock.INeighborAwareTile;
import cd4017be.lib.tileentity.BaseTileEntity;

@Mixin(value = DimensionalPipe.class, remap = false)
public abstract class DimensionalPipeMixin extends BaseTileEntity
                                           implements IUpdatable, IInteractiveTile, INeighborAwareTile {

    /**
     * @author aliu-here
     * @reason idk how else to fix the problem where a dimensionalpipe tileentity (that was already loaded) would have
     *         its linkTile get set to null when it was already a non-null value
     */
    @Overwrite
    void link(DimensionalPipe tile) {
        if (tile != ((DimensionalPipeAccessor) this).getLinkTile() && tile != null) {
            ((DimensionalPipeAccessor) this).setLinkTile(tile);
        }
        ((DimensionalPipeAccessor) this).setUpdateLink(false);
        world.notifyNeighborsOfStateChange(pos, blockType, true);
    }

    @Inject(method = "onLoad", at = @At("TAIL"))
    void immediateProcess() {
        ((DimensionalPipeAccessor) this).callProcess();
    }
}
