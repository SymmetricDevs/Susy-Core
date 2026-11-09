package supersymmetry.mixins.ivtoolkit;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.StructureBoundingBox;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ivorius.ivtoolkit.world.WorldCache;
import supersymmetry.common.worldgen.ColumnStateCache;
import supersymmetry.common.worldgen.WorldgenFixes;

@Mixin(value = WorldCache.class, remap = false)
public abstract class WorldCacheMixin {

    @Shadow
    @Final
    public World world;
    @Shadow
    private StructureBoundingBox boundingBox;
    @Shadow
    private IBlockState[] states;
    @Unique private ColumnStateCache<IBlockState> susy$columns;
    @Unique private boolean susy$useColumns;

    @Redirect(method = "<init>",
              at = @At(value = "INVOKE",
                       target = "Lnet/minecraft/world/gen/structure/StructureBoundingBox;getXSize()I",
                       remap = true))
    private int avoidDenseAllocation(StructureBoundingBox box) {
        susy$useColumns = ((Object) this).getClass() == WorldCache.class && WorldgenFixes.adaptiveCache() &&
                ColumnStateCache.fitsDenseVolume(box.getXSize(), box.getYSize(), box.getZSize());
        return susy$useColumns ? 0 : box.getXSize();
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void createColumns(World world, StructureBoundingBox box, CallbackInfo ci) {
        if (susy$useColumns)
            susy$columns = new ColumnStateCache<>(box.getXSize(), box.getYSize(), box.getZSize());
    }

    @Unique private void susy$promote() {
        if (!susy$columns.shouldPromote()) return;
        IBlockState[] dense = new IBlockState[boundingBox.getXSize() * boundingBox.getYSize() * boundingBox.getZSize()];
        susy$columns.copyInto(dense);
        states = dense;
        susy$columns = null;
    }

    @Inject(method = "getBlockState", at = @At("HEAD"), cancellable = true)
    private void cachedRead(BlockPos pos, CallbackInfoReturnable<IBlockState> ci) {
        if (susy$columns == null) return;
        if (!boundingBox.isVecInside(pos)) {
            ci.setReturnValue(world.getBlockState(pos));
            return;
        }
        int x = pos.getX() - boundingBox.minX, y = pos.getY() - boundingBox.minY,
                z = pos.getZ() - boundingBox.minZ;
        IBlockState state = susy$columns.get(x, y, z);
        if (state == null) {
            state = world.getBlockState(pos);
            susy$columns.put(x, y, z, state);
            susy$promote();
        }
        ci.setReturnValue(state);
    }

    @Inject(method = "setBlockState", at = @At("HEAD"), cancellable = true)
    private void cachedWrite(BlockPos pos, IBlockState state, int flags, CallbackInfoReturnable<Boolean> ci) {
        if (susy$columns == null) return;
        boolean changed = world.setBlockState(pos, state, flags);
        if (changed && boundingBox.isVecInside(pos)) {
            susy$columns.put(pos.getX() - boundingBox.minX,
                    pos.getY() - boundingBox.minY, pos.getZ() - boundingBox.minZ, state);
            susy$promote();
        }
        ci.setReturnValue(changed);
    }
}
