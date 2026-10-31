package supersymmetry.mixins.gregtech;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import gregtech.api.worldgen.populator.SurfaceBlockPopulator;

@Mixin(value = SurfaceBlockPopulator.class, remap = false)
public interface SurfaceBlockPopulatorInvoker {

    @Invoker("generateSurfaceBlock")
    boolean susy$generateSurfaceBlock(World world, BlockPos position);
}
