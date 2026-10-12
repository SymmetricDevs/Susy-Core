package supersymmetry.mixins.minecraft;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import com.llamalad7.mixinextras.injector.WrapWithCondition;

import supersymmetry.common.util.PlanetGrowthRules;

@Mixin(WorldServer.class)
public abstract class WorldServerPlanetGrowthMixin {

    // Intercept the world's natural dispatch, not Block.randomTick itself: machines may call it directly.
    // WrapWithCondition chains with other conditions: https://github.com/LlamaLad7/MixinExtras/wiki/WrapWithCondition
    @WrapWithCondition(method = "updateBlocks",
                       at = @At(value = "INVOKE",
                                target = "Lnet/minecraft/block/Block;randomTick(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;)V"),
                       require = 1,
                       expect = 1,
                       allow = 1)
    private boolean susy$allowPlanetRandomTick(Block block, World world, BlockPos pos, IBlockState state,
                                               Random random) {
        return !PlanetGrowthRules.suppressRandomTick(world, pos, state);
    }
}
