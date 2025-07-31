package supersymmetry.mixins.ctm;


import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityCleaningMaintenanceHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.client.renderer.textures.custom.VisualStateRenderer;

// I hate special cases...
@Mixin(value = MetaTileEntityCleaningMaintenanceHatch.class, remap = false)
public abstract class MetaTileEntityCleaningMaintenanceHatchMixin extends MetaTileEntityMultiblockPart {


    // Dummy
    MetaTileEntityCleaningMaintenanceHatchMixin() {
        super(null, 0);
    }

    @WrapOperation(method = "renderMetaTileEntity",
            at = @At(value = "INVOKE",
                    target = "Lgregtech/client/renderer/ICubeRenderer;render(Lcodechicken/lib/render/CCRenderState;Lcodechicken/lib/vec/Matrix4;[Lcodechicken/lib/render/pipeline/IVertexOperation;)V"))
    private void injectConnectableLogic(ICubeRenderer renderer,
                                        CCRenderState renderState,
                                        Matrix4 translation,
                                        IVertexOperation[] pipeline,
                                        Operation<Void> method) {

        if (getController() != null && renderer instanceof VisualStateRenderer stateRenderer) {
            stateRenderer.renderVisualState(renderState, getWorld(), getPos(),
                    isPainted() ? getPaintingColor() : null);
        } else {
            method.call(renderer, renderState, translation, pipeline);
        }
    }


    @WrapOperation(method = "renderMetaTileEntity",
            at = @At(value = "INVOKE",
                    target = "Lgregtech/common/metatileentities/multi/multiblockpart/MetaTileEntityCleaningMaintenanceHatch;getBaseTexture()Lgregtech/client/renderer/ICubeRenderer;"))
    private ICubeRenderer injectReplaceLogic(MetaTileEntityCleaningMaintenanceHatch self,
                                             Operation<ICubeRenderer> method) {

        var controller = getController();
        if (controller != null) {
            ICubeRenderer renderer = SusyTextures.RenderPlacements.get(controller.metaTileEntityId, null);
            if (renderer != null) {
                return renderer;
            }
        }
        return method.call(self);
    }
}
