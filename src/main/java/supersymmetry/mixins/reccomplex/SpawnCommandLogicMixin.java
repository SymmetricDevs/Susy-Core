package supersymmetry.mixins.reccomplex;

import net.minecraft.server.MinecraftServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import ivorius.reccomplex.block.SpawnCommandLogic;

@Mixin(SpawnCommandLogic.class)
public class SpawnCommandLogicMixin {

    /**
     * Bypass enable-command-block check.
     */
    @Redirect(
              method = "trigger(Lnet/minecraft/world/World;)V",
              at = @At(
                       value = "INVOKE",
                       target = "Lnet/minecraft/server/MinecraftServer;isCommandBlockEnabled()Z"))
    private boolean alwaysEnabled(MinecraftServer server) {
        return true;
    }

    /**
     * Bypass world save (anvil file) requirement.
     */
    @Redirect(
              method = "trigger(Lnet/minecraft/world/World;)V",
              at = @At(
                       value = "INVOKE",
                       target = "Lnet/minecraft/server/MinecraftServer;isAnvilFileSet()Z"))
    private boolean alwaysHasWorld(MinecraftServer server) {
        return true;
    }
}
