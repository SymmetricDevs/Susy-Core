package supersymmetry.mixins.dimstack;

import supersymmetry.mixins.dimstack.DimensionalPipeMixinAccessor;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import cd4017be.dimstack.tileentity.DimensionalPipe;
import cd4017be.lib.TickRegistry.IUpdatable;
import cd4017be.lib.block.AdvancedBlock.IInteractiveTile;
import cd4017be.lib.block.AdvancedBlock.INeighborAwareTile;
import cd4017be.lib.tileentity.BaseTileEntity;

@Mixin(value = DimensionalPipe.class, remap = false)
public abstract class DimensionalPipeMixin extends BaseTileEntity
                                           implements INeighborAwareTile, IUpdatable, IInteractiveTile {

    @Inject(method = "link", at = @At("TAIL"))
    void link(DimensionalPipe tile, CallbackInfo ci) {
        BlockPos neighbor = pos.offset(((DimensionalPipeMixinAccessor) this).getSide());
        world.getTileEntity(neighbor).getBlockType().onNeighborChange(world, pos, neighbor);
    }
}
