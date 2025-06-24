package supersymmetry.mixins.fluidlogged_api;

import git.jbredwards.fluidlogged_api.mod.asm.plugins.forge.PluginBlockFluidBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ChunkCache;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.Fluid;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nonnull;

@Mixin(value = PluginBlockFluidBase.Hooks.class, remap = false)
public class BlockFluidBaseHookMixin {

    @Inject(method = "getFluidExtendedState", at = @At("HEAD"), cancellable = true)
    private static void extraChecks(
            @Nonnull IBlockState oldState,
            @Nonnull IBlockAccess blockAccess,
            @Nonnull BlockPos i,
            @Nonnull Fluid dont,
            int care,
            int about,
            float these,
            float at,
            float all,
            CallbackInfoReturnable<IBlockState> cir
    ) {
        if (blockAccess instanceof World || blockAccess instanceof ChunkCache) {
            cir.setReturnValue(oldState);
        }
    }
}
