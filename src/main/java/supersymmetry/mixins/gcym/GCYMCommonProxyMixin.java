package supersymmetry.mixins.gcym;

import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.event.RegistryEvent;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregicality.multiblocks.common.CommonProxy;

@Mixin(value = CommonProxy.class, remap = false)
public abstract class GCYMCommonProxyMixin {

    @Inject(method = "registerRecipes", at = @At("HEAD"), cancellable = true)
    private static void susy$disableGCYMRecipes(RegistryEvent.Register<IRecipe> event, CallbackInfo ci) {
        ci.cancel();
    }
}
