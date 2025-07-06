package supersymmetry.common.item.behavior;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregtech.api.cover.CoverRayTracer;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.behavior.IToolBehavior;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.util.input.KeyBind;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.mixins.gregtech.BlockPipeAccessor;

import java.util.List;

import static supersymmetry.common.item.behavior.TraverseOption.*;

public enum PipeNetWalkerBehavior implements IToolBehavior {
    INSTANCE;

    private static void onActionDone(ItemStack stack, EntityPlayer player, World world, EnumHand hand, int walked) {
        IGTTool tool = ((IGTTool) stack.getItem());
        ToolHelper.damageItem(stack, player, walked);
        SoundEvent sound = tool.getSound();

        if (sound != null) {
            world.playSound(null, player.posX, player.posY, player.posZ,
                    sound, SoundCategory.PLAYERS, 1.0F, 1.0F);
        }
        player.swingArm(hand);
    }

    @Override
    public EnumActionResult onItemUseFirst(@NotNull EntityPlayer player,
                                           @NotNull World world,
                                           @NotNull BlockPos pos,
                                           @NotNull EnumFacing side,
                                           float hitX, float hitY, float hitZ,
                                           @NotNull EnumHand hand) {
        if (KeyBind.TOOL_AOE_CHANGE.isKeyDown(player)) {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof IPipeTile<?, ?> pipe) {

                var block = pipe.getPipeBlock();
                ItemStack toolStack = player.getHeldItem(hand);

                if (!((BlockPipeAccessor) block).checkPipeTool(toolStack)) return EnumActionResult.FAIL;

                CuboidRayTraceResult rayTraceResult = block.getServerCollisionRayTrace(player, pos, world);

                if (rayTraceResult == null) return EnumActionResult.FAIL;

                EnumFacing gridSide = CoverRayTracer.traceCoverSide(rayTraceResult);

                if (gridSide == null) return EnumActionResult.FAIL;

                TraverseOption option = null;
                if (pipe.isConnected(gridSide)) {
                    if (player.isSneaking()) {
                        option = pipe.isFaceBlocked(gridSide) ? UNBLOCKING : BLOCKING;
                    } else {
                        option = DISCONNECTING;
                    }
                } else if (!player.isSneaking()) {
                    option = CONNECTING;
                }

                if (option == null) return EnumActionResult.FAIL;

                NBTTagCompound toolTag = ToolHelper.getToolTag(toolStack);
                int maxWalks = toolTag.getInteger(ToolHelper.MAX_DURABILITY_KEY) - toolTag.getInteger(ToolHelper.DURABILITY_KEY);

                if (maxWalks <= 0) return EnumActionResult.FAIL;

                int walkedBlocks = PipeOperationWalker.collectPipeNet(world, pos, pipe, gridSide, option, maxWalks);

                onActionDone(toolStack, player, world, hand, MathHelper.ceil(MathHelper.sqrt(walkedBlocks)));

                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, World world, List<String> tooltip, @NotNull ITooltipFlag flag) {
        tooltip.add(I18n.format("item.susy.tool.behavior.pipeliner"));
    }
}
