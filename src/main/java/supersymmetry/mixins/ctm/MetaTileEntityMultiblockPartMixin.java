package supersymmetry.mixins.ctm;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supersymmetry.api.metatileentity.IConnectable;
import supersymmetry.client.renderer.handler.MTERendererExtension;

@Mixin(value = MetaTileEntityMultiblockPart.class, remap = false)
public abstract class MetaTileEntityMultiblockPartMixin extends MetaTileEntity implements IMultiblockPart, IConnectable {

    // Dummy
    public MetaTileEntityMultiblockPartMixin() {
        super(null);
    }

    @Shadow
    public abstract MultiblockControllerBase getController();

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public IBlockState getVisualState(@Nullable IMultiblockPart part) {
        if (getController() instanceof IConnectable connectable) {
            return connectable.getVisualState(this);
        }
        return null;
    }

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public boolean shouldRenderInLayer(@NotNull BlockRenderLayer renderLayer) {
        return (getController() instanceof IConnectable connectable && connectable.shouldRenderInLayer(renderLayer));
    }

    @Inject(method = "renderMetaTileEntity", at = @At("HEAD"))
    private void injectConnectableLogic(CCRenderState renderState, Matrix4 translation,
                                        IVertexOperation[] pipeline, CallbackInfo ci,
                                        @Share("callOriginal") LocalBooleanRef callOriginal) {

        if (getController() instanceof IConnectable connectable) {
            MTERendererExtension.renderBaseBlock(renderState, translation, this, connectable.getVisualState(this));
            callOriginal.set(false);
        } else {
            callOriginal.set(true);
        }
    }

    @WrapWithCondition(method = "renderMetaTileEntity",
            at = @At(value = "INVOKE",
                    target = "Lgregtech/client/renderer/ICubeRenderer;renderOriented(Lcodechicken/lib/render/CCRenderState;Lcodechicken/lib/vec/Matrix4;[Lcodechicken/lib/render/pipeline/IVertexOperation;Lnet/minecraft/util/EnumFacing;)V"))
    private boolean checkIfSkip(ICubeRenderer i, CCRenderState simply,
                                Matrix4 dont, IVertexOperation[] care, EnumFacing either,
                                @Share("callOriginal") LocalBooleanRef callOriginal) {

        return callOriginal.get();
    }

    @WrapWithCondition(method = "renderMetaTileEntity",
            at = @At(value = "INVOKE",
                    target = "Lgregtech/client/renderer/ICubeRenderer;render(Lcodechicken/lib/render/CCRenderState;Lcodechicken/lib/vec/Matrix4;[Lcodechicken/lib/render/pipeline/IVertexOperation;)V"))
    private boolean checkIfSkip(ICubeRenderer i, CCRenderState simply,
                                Matrix4 dont, IVertexOperation[] care,
                                @Share("callOriginal") LocalBooleanRef callOriginal) {

        return callOriginal.get();
    }
}
