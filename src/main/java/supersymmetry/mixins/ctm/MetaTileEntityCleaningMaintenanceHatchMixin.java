package supersymmetry.mixins.ctm;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityCleaningMaintenanceHatch;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import supersymmetry.client.renderer.textures.ConnectedTextures;

// I hate special cases...
@Mixin(value = MetaTileEntityCleaningMaintenanceHatch.class, remap = false)
public abstract class MetaTileEntityCleaningMaintenanceHatchMixin extends MetaTileEntityMultiblockPart {

    // Dummy
    MetaTileEntityCleaningMaintenanceHatchMixin() {
        super(null, 0);
    }

    @WrapOperation(method = "renderMetaTileEntity",
                   at = @At(value = "INVOKE",
                            target = "Lgregtech/common/metatileentities/multi/multiblockpart/MetaTileEntityCleaningMaintenanceHatch;getBaseTexture()Lgregtech/client/renderer/ICubeRenderer;"))
    private ICubeRenderer injectReplaceLogic(MetaTileEntityCleaningMaintenanceHatch self,
                                             Operation<ICubeRenderer> method) {

        var controller = getController();
        if (controller != null) {
            ICubeRenderer renderer = ConnectedTextures.get(controller.metaTileEntityId, this);
            if (renderer != null) {
                return renderer;
            }
        }
        return method.call(self);
    }
}
