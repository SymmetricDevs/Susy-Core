package supersymmetry.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.ItemHandlerDelegate;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import org.jetbrains.annotations.NotNull;
import supersymmetry.client.renderer.textures.SusyTextures;

public class CoverRestrictive extends CoverBase {

    protected ItemHandlerRestrictive itemHandler;

    public CoverRestrictive(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                            @NotNull EnumFacing attachedSide) {
        super(definition, coverableView, attachedSide);
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing enumFacing) {
        return coverable.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, getAttachedSide()) != null;
    }

    @Override
    public <T> T getCapability(@NotNull Capability<T> capability, T defaultValue) {
        if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (defaultValue == null) {
                return null;
            }
            IItemHandler delegate = (IItemHandler) defaultValue;
            if (itemHandler == null || itemHandler.delegate != delegate) {
                this.itemHandler = new ItemHandlerRestrictive(delegate);
            }
            return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(itemHandler);
        }
        return defaultValue;
    }

    @Override
    public boolean canPipePassThrough() {
        return true;
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation, @NotNull IVertexOperation[] pipeline,
                            @NotNull Cuboid6 plateBox, @NotNull BlockRenderLayer renderLayer) {
        SusyTextures.RESTRICTIVE_FILTER_FILTER_OVERLAY.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    protected static class ItemHandlerRestrictive extends ItemHandlerDelegate {

        public ItemHandlerRestrictive(IItemHandler delegate) {
            super(delegate);
        }

        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!stack.isEmpty() && !stack.isItemEqual(delegate.getStackInSlot(slot))) {
                for (int i = 0; i < getSlots(); i++) {
                    if (i != slot && stack.isItemEqual(getStackInSlot(i))) {
                        return super.insertItem(i, stack, simulate);
                    }
                }
            }
            return super.insertItem(slot, stack, simulate);
        }
    }
}
