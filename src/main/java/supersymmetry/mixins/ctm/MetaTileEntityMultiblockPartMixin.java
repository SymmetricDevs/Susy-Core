package supersymmetry.mixins.ctm;

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
import supersymmetry.client.renderer.textures.ConnectedTextures;
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
        if (getBaseTexture() instanceof VisualStateRenderer stateRenderer) {
            return stateRenderer.getVisualState();
        } else if (controller != null
                && ConnectedTextures.get(controller.metaTileEntityId, this)
                        instanceof VisualStateRenderer stateRenderer) {
            return stateRenderer.getVisualState();
        }
        return null;
    }

    @Override
    public boolean canRenderInLayer(@NotNull BlockRenderLayer layer) {
        if (super.canRenderInLayer(layer)) {
            return true;
        } else if (getBaseTexture() instanceof VisualStateRenderer stateRenderer) {
            return stateRenderer.canRenderInLayer(layer);
        } else {
            var controller = getController();
            if (controller != null
                    && ConnectedTextures.get(controller.metaTileEntityId, this)
                            instanceof VisualStateRenderer stateRenderer) {
                return stateRenderer.canRenderInLayer(layer);
            }
        }
        return false;
    }

    @WrapOperation(method = "renderMetaTileEntity",
                   at = @At(value = "INVOKE",
                            target = "Lgregtech/common/metatileentities/multi/multiblockpart/MetaTileEntityMultiblockPart;getBaseTexture()Lgregtech/client/renderer/ICubeRenderer;"))
    private ICubeRenderer injectReplaceLogic(MetaTileEntityMultiblockPart self, Operation<ICubeRenderer> method) {
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
