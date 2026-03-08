package supersymmetry.mixins.dimstack;

import net.minecraft.nbt.NBTTagCompound;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import cd4017be.dimstack.tileentity.DimensionalPipe;
import cd4017be.lib.TickRegistry.IUpdatable;
import cd4017be.lib.block.AdvancedBlock.IInteractiveTile;
import cd4017be.lib.block.AdvancedBlock.INeighborAwareTile;
import cd4017be.lib.tileentity.BaseTileEntity;

@Mixin(value = DimensionalPipe.class, remap = false)
interface DimensionalPipeMixinAccessor {

    @Invoker
    void callLink(DimensionalPipe tile);

    @Invoker
    void callProcess();
}

@Mixin(value = DimensionalPipe.class, remap = false)
public abstract class DimensionalPipeMixin extends BaseTileEntity
                                           implements INeighborAwareTile, IUpdatable, IInteractiveTile {

    @Inject(method = "link", at = @At("HEAD"))
    void linkOther(DimensionalPipe tile) {
        ((DimensionalPipeMixinAccessor) tile).callLink(((DimensionalPipe) (Object) this));
    }

    @Inject(method = "loadState", at = @At("TAIL"))
    void tryLink(NBTTagCompound nbt, int mode) {
        ((DimensionalPipeMixinAccessor) this).callProcess();
    }
}
