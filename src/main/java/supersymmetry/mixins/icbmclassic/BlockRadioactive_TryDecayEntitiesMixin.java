package supersymmetry.mixins.icbmclassic;

import icbm.classic.content.radioactive.BlockRadioactive;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Random;

@Mixin(value = BlockRadioactive.class, remap = false)
public abstract class BlockRadioactive_TryDecayEntitiesMixin {

    /**
     * basically replaces:
     * if (protection < this.minGasProtection() || protection < this.world.rand.nextFloat())
     * with
     * if (protection < this.minGasProtection())
     */
    @Redirect(
            method = "tryDecayEntities",
            at = @At(
                    value = "INVOKE",
                    target = "Ljava/util/Random;nextFloat()F",
                    ordinal = 1
            ),
            remap = false
    )
    private float susy$removeRadiationProtectionRng(Random random) {
        return 0.0f;
    }
}
