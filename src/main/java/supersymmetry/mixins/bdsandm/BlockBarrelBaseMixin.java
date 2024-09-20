package supersymmetry.mixins.bdsandm;

import funwayguy.bdsandm.blocks.BlockBarrelBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = BlockBarrelBase.class, remap = false)
public class BlockBarrelBaseMixin {

    @Redirect(method = "withdrawItem",
              at = @At(value = "INVOKE",
                       target = "Ljava/lang/Math;min(II)I",
                       ordinal = 0))
    private int redirectMin(int a, int b) {
        return b;
    }
}
