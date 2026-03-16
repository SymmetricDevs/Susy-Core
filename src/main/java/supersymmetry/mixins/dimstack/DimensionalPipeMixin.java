package supersymmetry.mixins.dimstack;

import net.minecraft.tileentity.TileEntity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

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
        TileEntity connected = ((DimensionalPipeAccessor) this).callGetCon();
        connected.getBlockType().observedNeighborChange(((DimensionalPipeAccessor) this).getConBlock(), world,
                connected.getPos(), this.getBlockType(), this.getPos()); // definitely force a pipenet reload
    }
}
