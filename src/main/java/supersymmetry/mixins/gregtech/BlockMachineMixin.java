package supersymmetry.mixins.gregtech;

import gregtech.api.block.BlockCustomParticle;
import gregtech.api.block.machines.BlockMachine;
import gregtech.api.metatileentity.MetaTileEntity;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.ApiStatus.ScheduledForRemoval;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;

import static gregtech.api.util.GTUtility.getMetaTileEntity;

@Deprecated
@ScheduledForRemoval(inVersion = "Next CEu update")
@Mixin(BlockMachine.class)
public abstract class BlockMachineMixin extends BlockCustomParticle {

    public BlockMachineMixin(Material blockMaterialIn, MapColor blockMapColorIn) {
        super(blockMaterialIn, blockMapColorIn);
    }

    @NotNull
    @Override
    @SuppressWarnings("deprecation")
    public ItemStack getItem(@NotNull World world, @NotNull BlockPos pos, @NotNull IBlockState state) {
        MetaTileEntity metaTileEntity = getMetaTileEntity(world, pos);
        if (metaTileEntity == null)
            return ItemStack.EMPTY;
        return metaTileEntity.getStackForm();
    }
}
