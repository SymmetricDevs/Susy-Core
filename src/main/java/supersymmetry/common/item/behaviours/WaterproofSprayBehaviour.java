package supersymmetry.common.item.behaviours;

import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.common.items.behaviors.AbstractUsableBehaviour;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import supersymmetry.mixins.Interfaces.IWaterProof;

import java.util.List;

public class WaterproofSprayBehaviour extends AbstractUsableBehaviour implements IItemDurabilityManager {

    private final ItemStack empty;

    public WaterproofSprayBehaviour(ItemStack empty, int totalUses) {
        super(totalUses);
        this.empty = empty;
    }

    @Override
    public ActionResult<ItemStack> onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand,
                                             EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack stack = player.getHeldItem(hand);
        if (!player.canPlayerEdit(pos, facing, stack)) {
            return ActionResult.newResult(EnumActionResult.FAIL, player.getHeldItem(hand));
        }
        if (!tryWaterProofBlock(player, world, pos, facing)) {
            return ActionResult.newResult(EnumActionResult.PASS, player.getHeldItem(hand));
        }
        useItemDurability(player, hand, stack, empty.copy());
        world.playSound(null, player.posX, player.posY, player.posZ, GTSoundEvents.SPRAY_CAN_TOOL,
                SoundCategory.PLAYERS, 1.0f, 1.0f);
        return ActionResult.newResult(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    private boolean tryWaterProofBlock(EntityPlayer player, World world, BlockPos pos, EnumFacing side) {
        TileEntity te = world.getTileEntity(pos);
        if (te instanceof IGregTechTileEntity) {
            MetaTileEntity mte = ((IGregTechTileEntity) te).getMetaTileEntity();
            if (mte instanceof IWaterProof tieredMTE) {
                if (!tieredMTE.supersymmetry$isWaterProof()) {
                    tieredMTE.supersymmetry$setWaterProof(true);
                    return true;
                } else return false;
            }
        }
        return false;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        int remainingUses = getUsesLeft(itemStack);
        lines.add(I18n.format("behaviour.waterproofspray.tooltip"));
        lines.add(I18n.format("behaviour.paintspray.uses", remainingUses));
        lines.add(I18n.format("behaviour.waterproofspray.offhand"));
    }

    @Override
    public double getDurabilityForDisplay(ItemStack itemStack) {
        return (double) getUsesLeft(itemStack) / totalUses;
    }
}
