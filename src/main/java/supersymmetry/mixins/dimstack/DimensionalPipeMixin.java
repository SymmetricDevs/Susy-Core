package supersymmetry.mixins.dimstack;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import cd4017be.dimstack.tileentity.DimensionalPipe;
import cd4017be.lib.TickRegistry.IUpdatable;
import cd4017be.lib.block.AdvancedBlock.IInteractiveTile;
import cd4017be.lib.block.AdvancedBlock.INeighborAwareTile;
import cd4017be.lib.tileentity.BaseTileEntity;

@Mixin(value = DimensionalPipe.class, remap = false)
interface DimensionalPipeMixinAccessor {

    @Accessor
    EnumFacing getSide();
}

@Mixin(value = DimensionalPipe.class, remap = false)
public abstract class DimensionalPipeMixin extends BaseTileEntity
                                           implements INeighborAwareTile, IUpdatable, IInteractiveTile {

    @Inject(method = "link", at = @At("TAIL"))
    void linkOther(DimensionalPipe tile) {
        BlockPos adjTEPos = pos.offset(((DimensionalPipeMixinAccessor) this).getSide());
        world.getTileEntity(adjTEPos).getBlockType().onNeighborChange(world, adjTEPos, pos);
    }
}
