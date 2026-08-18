package supersymmetry.mixins.gregtech.gcym;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gregicality.multiblocks.api.unification.properties.AlloyBlastProperty;
import gregicality.multiblocks.api.unification.properties.GCYMPropertyKey;
import gregicality.multiblocks.common.GCYMEventHandlers;
import gregtech.api.GregTechAPI;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.event.MaterialEvent;
import gregtech.api.unification.material.event.PostMaterialEvent;

@Mixin(value = GCYMEventHandlers.class, remap = false)
public abstract class GCYMEventHandlersMixin {

    @Inject(method = "registerMaterials", at = @At("HEAD"), cancellable = true)
    private static void susy$disableGCYMMaterials(MaterialEvent event, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(
            method = "registerMaterialsPost",
            at = @At(
                     value = "INVOKE",
                     target = "Lgregicality/multiblocks/api/fluids/GeneratedFluidHandler;init()V"))
    private static void susy$forceMoltenMaterials(PostMaterialEvent event, CallbackInfo ci) {
        susy$forceMolten("monel_500");
        susy$forceMolten("hsla_980_x");
        susy$forceMolten("food_grade_stainless_steel");
        susy$forceMolten("zircaloy_4");
        susy$forceMolten("reactor_steel");
        susy$forceMolten("alnico");
    }

    private static void susy$forceMolten(String name) {
        Material material = GregTechAPI.materialManager.getMaterial("susy:" + name);
        if (material == null) {
            return;
        }
        AlloyBlastProperty property = material.getProperty(GCYMPropertyKey.ALLOY_BLAST);
        if (property == null) {
            throw new IllegalStateException(
                    "SuSy material has no GCYM AlloyBlastProperty: " + name);
        }
        property.setForceGenerateMolten(true);
    }
}
