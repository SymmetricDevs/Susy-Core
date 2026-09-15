package supersymmetry.common.item.behavior;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AirlockBehavior implements IItemBehaviour {
    private final Block block;

    public AirlockBehavior(Block block) {
        this.block = block;
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        // Copied from ItemDoor
        ItemStack stack = player.getHeldItem(hand);
        if (facing != EnumFacing.UP) {
            return ActionResult.newResult(EnumActionResult.PASS, stack);
        }

        if (!world.getBlockState(pos).getBlock().isReplaceable(world, pos)) {
            pos = pos.offset(facing);
        }

        if (player.canPlayerEdit(pos, facing, stack) && this.block.canPlaceBlockAt(world, pos)) {
            EnumFacing playerFacing = EnumFacing.fromAngle(player.rotationYaw);
            placeDoor(world, pos, playerFacing, this.block);
            SoundType soundType = world.getBlockState(pos).getBlock().getSoundType(world.getBlockState(pos), world, pos, player);
            world.playSound(player, pos, soundType.getPlaceSound(), SoundCategory.BLOCKS, (soundType.getVolume() + 1) / 2, soundType.getPitch() * 0.8f);
            if (!player.isCreative()) {
                stack.shrink(1);
            }
            return ActionResult.newResult(EnumActionResult.SUCCESS, stack);
        } else {
            return ActionResult.newResult(EnumActionResult.FAIL, stack);
        }
    }
    public static void placeDoor(World worldIn, BlockPos pos, EnumFacing facing, Block door)
    {
        BlockPos blockpos2 = pos.up();

        boolean flag2 = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(blockpos2);
        IBlockState iblockstate = door.getDefaultState().withProperty(BlockDoor.FACING, facing).withProperty(BlockDoor.POWERED, Boolean.valueOf(flag2)).withProperty(BlockDoor.OPEN, Boolean.valueOf(flag2));
        worldIn.setBlockState(pos, iblockstate.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER), 2);
        worldIn.setBlockState(blockpos2, iblockstate.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER), 2);
        worldIn.notifyNeighborsOfStateChange(pos, door, false);
        worldIn.notifyNeighborsOfStateChange(blockpos2, door, false);
    }
}
