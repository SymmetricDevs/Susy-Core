package supersymmetry.mixins.bdsandm;

import funwayguy.bdsandm.blocks.tiles.TileEntityCrate;
import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TileEntityCrate.class)
public abstract class TileEntityCrateMixin extends TileEntity {

    @Override
    public boolean shouldRefresh(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState oldState, @NotNull IBlockState newSate) {
        return oldState.getBlock() != newSate.getBlock();
    }
}
