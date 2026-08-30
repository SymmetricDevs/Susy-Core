package supersymmetry.mixins.gregtech;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;

import gregtech.api.pipenet.tile.IPipeTile;
import gregtech.api.pipenet.tile.TileEntityPipeBase;
import supersymmetry.api.pipelike.ConnectablePipe;

@Mixin(value = TileEntityPipeBase.class, remap = false)
public abstract class TileEntityPipeBaseMixin {

    @Definition(id = "pipeTile", local = @Local(type = IPipeTile.class, name = "pipeTile"))
    @Definition(id = "getPipeType", method = "Lgregtech/api/pipenet/tile/IPipeTile;getPipeType()Ljava/lang/Enum;")
    @Definition(id = "getClass", method = "Ljava/lang/Object;getClass()Ljava/lang/Class;")
    @Expression("pipeTile.getPipeType().getClass() != ?")
    @ModifyExpressionValue(method = "setConnection", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean acceptConnectablePipes(boolean original, @Local(name = "pipeTile") IPipeTile<?, ?> pipeTile) {
        return original && !((ConnectablePipe) this).canConnectWith(pipeTile);
    }
}
