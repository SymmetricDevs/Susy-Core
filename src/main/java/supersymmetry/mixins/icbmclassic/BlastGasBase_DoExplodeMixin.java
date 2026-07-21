package supersymmetry.mixins.icbmclassic;

import icbm.classic.content.blast.gas.BlastGasBase;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(value = BlastGasBase.class, remap = false)
public abstract class BlastGasBase_DoExplodeMixin {

    /**
     * basically replaces:
     * if (protection < this.minGasProtection() || protection < this.world.rand.nextFloat())
     * with
     * if (protection < this.minGasProtection())
     */
    @Redirect(
            method = "doExplode",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Random;nextFloat()F"
            ),
            remap = false
    )
    private float susy$removeProtectionRng(Random random) {
        return 0.0f;
    }
}
