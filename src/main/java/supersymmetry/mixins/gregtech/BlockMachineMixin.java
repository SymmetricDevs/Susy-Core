package supersymmetry.mixins.gregtech;

import net.minecraft.util.math.AxisAlignedBB;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import gregtech.api.block.machines.BlockMachine;

@Mixin(value = BlockMachine.class, remap = false)
public abstract class BlockMachineMixin {

    /**
     * Swap the receiver and argument of the collision intersection test so that
     * {@code entityBox.intersects(offsetBox)} is called instead of
     * {@code offsetBox.intersects(entityBox)}. Immersive Railroading has an obscure
     * incompatibility with the original call direction.
     */
    @Redirect(method = "addCollisionBoxToList",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/util/math/AxisAlignedBB;intersects(Lnet/minecraft/util/math/AxisAlignedBB;)Z"),
              remap = true)
    private boolean susy$swapIntersectsOrder(AxisAlignedBB offsetBox, AxisAlignedBB entityBox) {
        return entityBox.intersects(offsetBox);
    }
}
