package supersymmetry.mixins.ctm;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.BlockRenderLayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import supersymmetry.api.metatileentity.IConnectable;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.client.renderer.textures.custom.VisualStateRenderer;

@Mixin(value = MetaTileEntityMultiblockPart.class, remap = false)
public abstract class MetaTileEntityMultiblockPartMixin extends MetaTileEntity implements IMultiblockPart, IConnectable {

    // Dummy
    MetaTileEntityMultiblockPartMixin() {
        super(null);
    }

    @Shadow
    public abstract MultiblockControllerBase getController();


    @Shadow
    public abstract ICubeRenderer getBaseTexture();

    @Nullable
    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public IBlockState getVisualState(@Nullable IMultiblockPart part) {
        var controller = getController();
        if (controller != null) {
            if (getBaseTexture() instanceof VisualStateRenderer stateRenderer) {
                return stateRenderer.getVisualState();
            } else if (SusyTextures.Replacements.get(controller.metaTileEntityId, null)
                    instanceof VisualStateRenderer stateRenderer) {
                return stateRenderer.getVisualState();
            }
        }
        return null;
    }

    @Override
    public boolean canRenderInLayer(@NotNull BlockRenderLayer layer) {
        if (super.canRenderInLayer(layer)) {
            return true;
        } else {
            var controller = getController();
            if (controller != null) {
                if (getBaseTexture() instanceof VisualStateRenderer stateRenderer) {
                    return stateRenderer.canRenderInLayer(layer);
                } else if (SusyTextures.Replacements.get(controller.metaTileEntityId, null)
                        instanceof VisualStateRenderer stateRenderer) {
                    return stateRenderer.canRenderInLayer(layer);
                }
            }
        }
        return false;
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
                    target = "Lgregtech/common/metatileentities/multi/multiblockpart/MetaTileEntityMultiblockPart;getBaseTexture()Lgregtech/client/renderer/ICubeRenderer;"))
    private ICubeRenderer injectReplaceLogic(MetaTileEntityMultiblockPart self, Operation<ICubeRenderer> method) {
        var controller = getController();
        if (controller != null) {
            ICubeRenderer renderer = SusyTextures.Replacements.get(controller.metaTileEntityId, null);
            if (renderer != null) {
                return renderer;
            }
        }
        return method.call(self);
    }
}
