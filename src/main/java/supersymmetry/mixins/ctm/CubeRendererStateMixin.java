package supersymmetry.mixins.ctm;

import gregtech.client.renderer.CubeRendererState;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import supersymmetry.client.renderer.CRSExtension;

@Mixin(value = CubeRendererState.class, remap = false)
public abstract class CubeRendererStateMixin implements CRSExtension {

    @Unique
    private BlockPos susy$pos = BlockPos.ORIGIN;

    @Override
    public CubeRendererState susy$withPos(BlockPos pos) {
        this.susy$pos = pos;
        return (CubeRendererState) (Object) this;
    }

    @Override
    public BlockPos susy$getPos() {
        return this.susy$pos;
    }
}
