package supersymmetry.common.item.behavior;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import supersymmetry.common.metatileentities.single.electric.MetaTileEntityDefoliator.DefoliatorReplacements;

import java.util.Map;

public class HyperDefoliantBehavior implements IItemBehaviour {

    private static final int RADIUS = 16;

    @Override
    public EnumActionResult onItemUseFirst(EntityPlayer player, World world, BlockPos pos,
                                           EnumFacing side, float hitX, float hitY, float hitZ,
                                           EnumHand hand) {
        if (world.isRemote) return EnumActionResult.SUCCESS;

        ItemStack stack = player.getHeldItem(hand);

        applyDefoliation(world, pos);

        if (!player.capabilities.isCreativeMode) {
            stack.shrink(1);
        }

        return EnumActionResult.SUCCESS;
    }

    public static void registerDispenserBehavior(ItemStack defoliantStack) {
        BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(defoliantStack.getItem(), new BehaviorDefaultDispenseItem() {
            @Override
            protected ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
                World world = source.getWorld();
                if (world.isRemote) return stack;
                EnumFacing facing = source.getBlockState().getValue(BlockDispenser.FACING);
                BlockPos targetPos = source.getBlockPos().offset(facing);
                applyDefoliationStatic(world, targetPos);
                stack.shrink(1);
                return stack;
            }
        });
    }

    private void applyDefoliation(World world, BlockPos center) {
        applyDefoliationStatic(world, center);
    }

    private static void applyDefoliationStatic(World world, BlockPos center) {
        Map<ResourceLocation, IBlockState> replacements     = DefoliatorReplacements.getReplacements();
        Map<String,           IBlockState> metaReplacements = DefoliatorReplacements.getMetaReplacements();

        for (int dx = -RADIUS; dx <= RADIUS; dx++) {
            for (int dy = -RADIUS; dy <= RADIUS; dy++) {
                for (int dz = -RADIUS; dz <= RADIUS; dz++) {
                    BlockPos target = center.add(dx, dy, dz);
                    if (!world.isValid(target)) continue;

                    IBlockState state = world.getBlockState(target);
                    if (state.getBlock() == Blocks.AIR) continue;

                    ResourceLocation blockName = ForgeRegistries.BLOCKS.getKey(state.getBlock());
                    if (blockName == null) continue;

                    String metaKey = blockName + ":" + state.getBlock().getMetaFromState(state);
                    IBlockState replacement = metaReplacements.get(metaKey);
                    if (replacement == null) replacement = replacements.get(blockName);

                    if (replacement != null) {
                        world.setBlockState(target, replacement, 2);
                    }
                }
            }
        }
    }
}
