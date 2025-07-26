package supersymmetry.mixins.reccomplex;


import ivorius.reccomplex.world.gen.feature.StructureGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import supersymmetry.integration.reccomplex.RCLootGenContext;

@Mixin(value = StructureGenerator.class, remap = false)
public class StructureGeneratorMixin {

    @Inject(method = "generate", at = @At("HEAD"))
    private static void onStartStructureGen(CallbackInfoReturnable<Boolean> cir) {
        RCLootGenContext.STRUCTURE_GEN_RUNNING.set(true);
        //System.out.println("[SuSy][Mixin] StructureGenerator.generate called: setting loot flag");
    }

    @Inject(method = "generate", at = @At("RETURN"))
    private static void onEndStructureGen(CallbackInfoReturnable<Boolean> cir) {
        RCLootGenContext.STRUCTURE_GEN_RUNNING.set(false);
        //System.out.println("[SuSy][Mixin] StructureGenerator.generate completed: clearing loot flag");
    }
}
