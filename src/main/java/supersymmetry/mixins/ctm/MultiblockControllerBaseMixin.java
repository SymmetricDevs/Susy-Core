package supersymmetry.mixins.ctm;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.client.renderer.ICubeRenderer;
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

@Mixin(value = MultiblockControllerBase.class, remap = false)
public abstract class MultiblockControllerBaseMixin extends MetaTileEntity implements IConnectable {

    // Dummy
    MultiblockControllerBaseMixin() {
        super(null);
    }

    @Shadow
    public abstract boolean isStructureFormed();

    @Shadow
    public abstract ICubeRenderer getBaseTexture(IMultiblockPart sourcePart);

    @Nullable
    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public IBlockState getVisualState(@Nullable IMultiblockPart part) {
        if (isStructureFormed()) {
            if (getBaseTexture(null) instanceof VisualStateRenderer stateRenderer) {
                return stateRenderer.getVisualState();
            } else if (ConnectedTextures.get(metaTileEntityId, null) instanceof VisualStateRenderer stateRenderer) {
                return stateRenderer.getVisualState();
            }
        }
        return null;
    }

    @Override
    public boolean canRenderInLayer(@NotNull BlockRenderLayer layer) {
        if (super.canRenderInLayer(layer)) {
            return true;
        } else if (getBaseTexture(null) instanceof VisualStateRenderer stateRenderer) {
            return stateRenderer.canRenderInLayer(layer);
        } else if (ConnectedTextures.get(metaTileEntityId, null) instanceof VisualStateRenderer stateRenderer) {
            return stateRenderer.canRenderInLayer(layer);
        }
        return false;
    }

    @WrapOperation(method = "renderMetaTileEntity",
                   at = @At(value = "INVOKE",
                            target = "Lgregtech/api/metatileentity/multiblock/MultiblockControllerBase;getBaseTexture(Lgregtech/api/metatileentity/multiblock/IMultiblockPart;)Lgregtech/client/renderer/ICubeRenderer;"))
    private ICubeRenderer overridesBaseTexture(
            MultiblockControllerBase self, IMultiblockPart part, Operation<ICubeRenderer> method) {

        ICubeRenderer renderer = ConnectedTextures.get(metaTileEntityId, part);
        if (renderer != null) {
            return renderer;
        }
        return method.call(self, part);
    }
}

