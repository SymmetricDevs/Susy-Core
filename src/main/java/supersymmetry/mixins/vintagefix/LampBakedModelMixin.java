package supersymmetry.mixins.vintagefix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import gregtech.client.model.lamp.LampBakedModel;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.util.registry.IRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import team.chisel.ctm.client.model.ModelBakedCTM;

@Mixin(value = LampBakedModel.class, remap = false)
public abstract class LampBakedModelMixin {

    @WrapOperation(method = "onModelBake",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/util/registry/IRegistry;putObject(Ljava/lang/Object;Ljava/lang/Object;)V",
                    remap = true))
    private static void skipIfCTMModel(IRegistry<ModelResourceLocation, IBakedModel> modelRegistry,
                                       Object key, Object value, Operation<Void> method,
                                       @Local(name = "model") IBakedModel model) {
        if (!(model instanceof ModelBakedCTM)) {
            method.call(modelRegistry, key, value);
        }
    }
}
