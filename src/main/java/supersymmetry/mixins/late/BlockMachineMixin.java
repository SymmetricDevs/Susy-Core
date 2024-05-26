package supersymmetry.mixins.late;

import gregtech.api.block.machines.BlockMachine;
import gregtech.common.items.MetaItems;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.item.behaviours.WaterproofSprayBehaviour;

import javax.annotation.Nonnull;

@Mixin(value = BlockMachine.class, priority = 500, remap = false)
public class BlockMachineMixin {

    @Inject(method = "onBlockPlacedBy", at = @At("TAIL"))
    private void onPlacedWithWaterProofSpray(World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase placer, ItemStack stack, CallbackInfo ci) {
        // Waterproof machines on place if holding waterproof spray can in off-hand
        if (placer instanceof EntityPlayer) {
            ItemStack offhand = placer.getHeldItemOffhand();
            if(offhand.isItemEqual(SuSyMetaItems.SPRAY_CAN_WATERPROOF.getStackForm())) {
                ((WaterproofSprayBehaviour) SuSyMetaItems.SPRAY_CAN_WATERPROOF.getBehaviours().get(0)).onItemUse((EntityPlayer) placer, worldIn, pos, EnumHand.OFF_HAND, EnumFacing.UP, 0, 0, 0);
            }
        }
    }
}
