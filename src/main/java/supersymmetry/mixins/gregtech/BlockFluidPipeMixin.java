package supersymmetry.mixins.gregtech;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.fluidpipe.tile.TileEntityFluidPipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import supersymmetry.common.pipelike.tanklessfluid.tile.TileEntityTanklessFluidPipe;

@Mixin(value = BlockFluidPipe.class, remap = false)
public abstract class BlockFluidPipeMixin {

    @Definition(id = "TileEntityFluidPipe", type = TileEntityFluidPipe.class)
    @Definition(id = "sideTile", local = @Local(type = IPipeTile.class, ordinal = 1, argsOnly = true))
    @Expression("sideTile instanceof TileEntityFluidPipe")
    @ModifyExpressionValue(method = "canPipesConnect", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean acceptTanklessFluidPipes(boolean original, @Local(name = "sideTile") IPipeTile<?, ?> sideTile) {
        return original || sideTile instanceof TileEntityTanklessFluidPipe;
    }
}
