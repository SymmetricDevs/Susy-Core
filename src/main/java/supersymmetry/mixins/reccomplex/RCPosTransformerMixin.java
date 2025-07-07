package supersymmetry.mixins.reccomplex;

import ivorius.ivtoolkit.math.AxisAlignedTransform2D;
import ivorius.reccomplex.temp.RCPosTransformer;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import net.minecraft.tileentity.TileEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(RCPosTransformer.class)
public class RCPosTransformerMixin {
    // Adds NBT to spawned TileEntities that prevents them from being cheesed with RefinedTools storage scanners
    @Inject(method = "transformAdditionalData", at = @At("HEAD"), locals = LocalCapture.PRINT)
    public static void transformAdditionalData(TileEntity tileEntity, AxisAlignedTransform2D transform, int[] size, CallbackInfoReturnable<Void> ci) {
        tileEntity.getTileData().setBoolean("PlacedByRC", true);
    }

}
