package supersymmetry.mixins.reccomplex;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import ivorius.reccomplex.world.gen.feature.structure.generic.placement.SelectivePlacer;
import ivorius.reccomplex.world.gen.feature.structure.generic.placement.StructurePlaceContext;
import supersymmetry.common.worldgen.SurfaceCoordinates;
import supersymmetry.common.worldgen.WorldgenFixes;

@Mixin(value = SelectivePlacer.class, remap = false)
public abstract class RecurrentPlacerMixin {

    @Redirect(method = "lambda$place$2",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/WorldServer;isChunkGeneratedAt(II)Z",
                       remap = true))
    private static boolean preserveFallback(WorldServer world, int x, int z, StructurePlaceContext context,
                                            BlockPos local) {
        // The first collection must remain unfiltered so the documented fallback has a real surface to use.
        return WorldgenFixes.recurrentComplex() || world.isChunkGeneratedAt(x, z);
    }

    @Redirect(method = "lambda$place$3",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/WorldServer;isChunkGeneratedAt(II)Z",
                       remap = true))
    private static boolean checkActualDestination(WorldServer world, int x, int z, StructurePlaceContext context,
                                                  BlockPos local) {
        if (!WorldgenFixes.recurrentComplex()) return world.isChunkGeneratedAt(x, z);
        BlockPos actual = SurfaceCoordinates.worldPosition(context, local);
        return world.isChunkGeneratedAt(actual.getX() >> 4, actual.getZ() >> 4);
    }
}
