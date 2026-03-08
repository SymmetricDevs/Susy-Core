package supersymmetry.mixins.dimstack;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import cd4017be.dimstack.tileentity.DimensionalPipe;
import cd4017be.lib.TickRegistry.IUpdatable;
import cd4017be.lib.block.AdvancedBlock.IInteractiveTile;
import cd4017be.lib.block.AdvancedBlock.INeighborAwareTile;
import cd4017be.lib.tileentity.BaseTileEntity;

@Mixin(value = DimensionalPipe.class, remap = false)
interface DimensionalPipeMixinAccessor {

    @Accessor
    EnumFacing getSide();

    @Accessor
    DimensionalPipe getLinkTile();

    @Final
    @Invoker
    TileEntity callGetCon();
}

@Mixin(value = DimensionalPipe.class, remap = false)
public abstract class DimensionalPipeMixin extends BaseTileEntity
                                           implements INeighborAwareTile, IUpdatable, IInteractiveTile {

    /**
     * @Author aliu-here
     * @reason testing
     */
    @Overwrite
    public boolean hasCapability(Capability<?> cap, EnumFacing facing) {
        if (facing != ((DimensionalPipeMixinAccessor) this).getSide()) return false;
        DimensionalPipe linkTile = ((DimensionalPipeMixinAccessor) this).getLinkTile();
        if (linkTile != null && !linkTile.invalid()) {
            return ((DimensionalPipeMixinAccessor) linkTile).callGetCon().hasCapability(cap,
                    ((DimensionalPipeMixinAccessor) this).getSide());
        }
        return false;
    }
}
