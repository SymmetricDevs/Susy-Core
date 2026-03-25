package supersymmetry.mixins.xnet;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.sugar.Local;

import gregtech.api.block.machines.BlockMachine;
import gregtech.api.pipenet.block.BlockPipe;
import mcjty.xnet.blocks.facade.FacadeItemBlock;

@Mixin(value = FacadeItemBlock.class)
public class FacadeItemBlockMixin {

    @Inject(method = "onItemUse(Lnet/minecraft/entity/player/EntityPlayer;Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/EnumHand;Lnet/minecraft/util/EnumFacing;FFF)Lnet/minecraft/util/EnumActionResult;",
            at = @At(
                     value = "INVOKE",
                     target = "Lmcjty/xnet/blocks/facade/FacadeItemBlock;setMimicBlock(Lnet/minecraft/item/ItemStack;Lnet/minecraft/block/state/IBlockState;)V",
                     remap = false,
                     ordinal = 0),
            cancellable = true)
    private void skipGTTiles(EntityPlayer why, World the, BlockPos hell, EnumHand are, EnumFacing there, float so,
                             float many, float variants,
                             CallbackInfoReturnable<EnumActionResult> cir, @Local(ordinal = 0) Block block) {
        if (block instanceof BlockPipe<?, ?, ?> || block instanceof BlockMachine) {
            cir.setReturnValue(EnumActionResult.FAIL);
        }
    }
}
