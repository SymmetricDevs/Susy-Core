package supersymmetry.mixins.dimstack;

import org.spongepowered.asm.mixin.Mixin;

import cd4017be.dimstack.tileentity.DimensionalPipe;
import cd4017be.lib.TickRegistry.IUpdatable;
import cd4017be.lib.block.AdvancedBlock.IInteractiveTile;
import cd4017be.lib.block.AdvancedBlock.INeighborAwareTile;
import cd4017be.lib.tileentity.BaseTileEntity;

@Mixin(value = DimensionalPipe.class, remap = true)
public abstract class DimensionalPipeMixin extends BaseTileEntity
                                           implements INeighborAwareTile, IUpdatable, IInteractiveTile {

}
