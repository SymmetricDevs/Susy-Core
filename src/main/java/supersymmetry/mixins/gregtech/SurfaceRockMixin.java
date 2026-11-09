package supersymmetry.mixins.gregtech;

import java.util.Random;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import gregtech.api.worldgen.config.OreDepositDefinition;
import gregtech.api.worldgen.generator.GridEntryInfo;
import gregtech.api.worldgen.populator.SurfaceRockPopulator;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.common.worldgen.Indicators;
import supersymmetry.common.worldgen.WorldgenFixes;

@Mixin(value = SurfaceRockPopulator.class, remap = false)
public abstract class SurfaceRockMixin {

    @Redirect(method = "populateChunk",
              at = @At(value = "INVOKE",
                       target = "Lgregtech/api/worldgen/populator/SurfaceRockPopulator;generateSurfaceRock(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;)V"))
    private void guard(SurfaceRockPopulator self, World world, BlockPos position, World sourceWorld, int x, int z,
                       Random random, OreDepositDefinition definition, GridEntryInfo info) {
        IBlockState state = MetaBlocks.SURFACE_ROCK.get(self.getMaterial()).getBlock(self.getMaterial());
        if (!Indicators.defer(world, position, x, z, (byte) 0, state)) self.generateSurfaceRock(world, position);
    }

    @Redirect(method = "setStoneBlock",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/World;setBlockState(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;)Z",
                       remap = true))
    private boolean quietPlacement(World world, BlockPos pos, IBlockState state) {
        return WorldgenFixes.deferIndicators() ? world.setBlockState(pos, state, 18) : world.setBlockState(pos, state);
    }
}
