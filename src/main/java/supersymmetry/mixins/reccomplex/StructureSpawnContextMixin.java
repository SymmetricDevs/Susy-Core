package supersymmetry.mixins.reccomplex;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;

@Mixin(StructureSpawnContext.class)
public class StructureSpawnContextMixin {

    // Adds a flag to the setBlockState method that force-replaces the fluid state from FluidloggedAPI
    @ModifyVariable(method = "setBlock", name = "arg3", at = @At("HEAD"), argsOnly = true, remap = false, require = 1)
    public int setBlock(int flag) {
        return flag | 64;
    }
}
