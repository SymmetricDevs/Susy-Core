package supersymmetry.client.renderer;

import gregtech.client.renderer.CubeRendererState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import supersymmetry.mixins.ctm.CubeRendererStateMixin;
import supersymmetry.mixins.ctm.MetaTileEntityRendererMixin;

/// A Mixin extension interface for [CubeRendererState]
/// to allow getting the position of the block being rendered.
///
/// @see CubeRendererStateMixin
/// @see MetaTileEntityRendererMixin
public interface CRSExtension {

    static CRSExtension cast(CubeRendererState state) {
        return (CRSExtension) state;
    }

    CubeRendererState susy$withPos(BlockPos pos);

    BlockPos susy$getPos();

    CubeRendererState susy$withWorld(IBlockAccess world);

    IBlockAccess susy$getWorld();
}
