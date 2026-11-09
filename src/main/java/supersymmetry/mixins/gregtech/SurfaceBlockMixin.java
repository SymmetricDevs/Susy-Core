package supersymmetry.mixins.gregtech;

import java.util.Random;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.generator.GridEntryInfo;
import gregtech.api.worldgen.populator.SurfaceBlockPopulator;
import supersymmetry.common.worldgen.Indicators;

@Mixin(value = SurfaceBlockPopulator.class, remap = false)
public abstract class SurfaceBlockMixin {

    @Redirect(method = "populateChunk",
              at = @At(value = "INVOKE",
                       target = "Lgregtech/api/worldgen/populator/SurfaceBlockPopulator;generateSurfaceBlock(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)Z"))
    private boolean guard(SurfaceBlockPopulator self, World world, BlockPos position, World sourceWorld, int x, int z,
                          Random random, OreDepositDefinition definition, GridEntryInfo info) {
        return Indicators.defer(world, position, x, z, (byte) 1, self.getBlockState()) ||
                ((SurfaceBlockPopulatorInvoker) self).susy$generateSurfaceBlock(world, position);
    }
}
