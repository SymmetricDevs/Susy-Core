package supersymmetry.mixins.ctm;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.client.renderer.ICubeRenderer;
import net.minecraft.util.EnumFacing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supersymmetry.api.metatileentity.IConnectable;
import supersymmetry.client.renderer.handler.MTERendererExtension;

@Mixin(value = MultiblockControllerBase.class, remap = false)
public abstract class MultiblockControllerBaseMixin extends MetaTileEntity implements IConnectable {

    // Dummy
    MultiblockControllerBaseMixin() {
        super(null);
    }

    @Inject(method = "renderMetaTileEntity", at = @At("HEAD"))
    public void injectConnectableLogic(CCRenderState renderState, Matrix4 translation,
                                       IVertexOperation[] pipeline, CallbackInfo ci,
                                       @Share("callOriginal") LocalBooleanRef callOriginal) {

        if (getWorld() != null) {
            MTERendererExtension.renderBaseBlock(renderState, translation, this, getVisualState(null));
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

