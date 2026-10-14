package supersymmetry.mixins.xnet;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import mcjty.xnet.blocks.cables.ConnectorBlock;

@Mixin(value = ConnectorBlock.class, remap = false)
public abstract class ConnectorBlockMixin {

    @WrapOperation(method = "isConnectable",
                   at = @At(value = "INVOKE",
                            target = "Lnet/minecraft/tileentity/TileEntity;hasCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/util/EnumFacing;)Z"),
                   require = 2)
    private static boolean susy$detectSidedCapability(TileEntity tile, Capability<?> capability, EnumFacing side,
                                                      Operation<Boolean> original,
                                                      @Local(argsOnly = true) EnumFacing facing) {
        return original.call(tile, capability, side) ||
                (side == null && original.call(tile, capability, facing.getOpposite()));
    }
}
