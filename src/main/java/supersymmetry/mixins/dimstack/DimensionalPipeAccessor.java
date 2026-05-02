package supersymmetry.mixins.dimstack;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import cd4017be.dimstack.tileentity.DimensionalPipe;

@Mixin(value = DimensionalPipe.class, remap = false)
public interface DimensionalPipeAccessor {

    @Accessor
    DimensionalPipe getLinkTile();

    @Accessor
    void setLinkTile(DimensionalPipe tile);

    @Accessor
    EnumFacing getSide();

    @Accessor
    void setUpdateLink(boolean value);

    @Accessor
    IBlockState getConBlock();

    @Invoker
    TileEntity callGetCon();

    @Invoker
    void callProcess();
}
