package supersymmetry.common.item.behavior;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import gregtech.api.cover.CoverRayTracer;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.util.input.KeyBind;
import gregtech.common.items.behaviors.AbstractUsableBehaviour;

public class PipeNetPainterBehavior extends AbstractUsableBehaviour {

    private final ItemStack empty;
    private final int color;

    public PipeNetPainterBehavior(int totalUses, ItemStack empty, int color) {
        super(totalUses);
        this.empty = empty;
        this.color = color;
    }

    private void onActionDone(ItemStack stack, EntityPlayer player, EnumHand hand, int walked) {
        int usesLeft = getUsesLeft(stack);
        usesLeft -= walked;
        if (!player.capabilities.isCreativeMode) {
            if (--usesLeft <= 0) {
                player.setHeldItem(hand, empty.copy());

                return;
            }
            setUsesLeft(stack, usesLeft);
        }
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
            if (te instanceof IPipeTile<?, ?>pipe) {

                var block = pipe.getPipeBlock();
                ItemStack toolStack = player.getHeldItem(hand);

                CuboidRayTraceResult rayTraceResult = block.getServerCollisionRayTrace(player, pos, world);

                if (rayTraceResult == null) return EnumActionResult.FAIL;

                EnumFacing gridSide = CoverRayTracer.traceCoverSide(rayTraceResult);

                if (gridSide == null) return EnumActionResult.FAIL;

                int maxWalks = getUsesLeft(toolStack);
                if (maxWalks <= 0) return EnumActionResult.FAIL;

                int walkedBlocks = PipeOperationWalker.collectPipeNet(world, pos, pipe, gridSide,
                        TraverseOptions.COLORING.get(color), maxWalks);

                onActionDone(toolStack, player, hand, walkedBlocks);

                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, List<String> lines) {
        lines.add(I18n.format("item.susy.tool.tooltip.pipeliner",
                GameSettings.getKeyDisplayString(KeyBind.TOOL_AOE_CHANGE.toMinecraft().getKeyCode())));
    }
}
