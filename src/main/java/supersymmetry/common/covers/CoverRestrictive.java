package supersymmetry.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.impl.ItemHandlerDelegate;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.util.ItemStackHashStrategy;
import it.unimi.dsi.fastutil.ints.IntArraySet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenCustomHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArraySet;
import it.unimi.dsi.fastutil.objects.ObjectOpenCustomHashSet;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;
import supersymmetry.client.renderer.textures.SusyTextures;

import java.util.Set;

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
        private final Set<ItemStack> set = new ObjectOpenCustomHashSet<>(ItemStackHashStrategy.comparingAllButCount());

        public ItemHandlerRestrictive(IItemHandler delegate) {
            super(delegate);
        }


        @NotNull
        @Override
        public ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            // Check if the current slot already has the item (by checking if it stacks). If not:
            // Check if it happens to be somewhere else. This is cached in a set.
            // If it is, we reject the stack, but otherwise, we let it through.
            // (The whole point is to prevent more than one slot from automatically filling up.)

            if (!getStackInSlot(slot).isEmpty() && ItemHandlerHelper.canItemStacksStack(stack, getStackInSlot(slot))) {
                return super.insertItem(slot, stack, simulate);
            }
            if (getStackInSlot(slot).isEmpty()) {
                if (set.contains(stack)) {
                    return stack;
                } else {
                    // If it's not already in the set of what goes where, we search if it happens to be anywhere already, for some reason.
                    for (int i = 0; i < getSlots(); i++) {
                        if (i != slot && ItemHandlerHelper.canItemStacksStack(stack, getStackInSlot(slot))) {
                            set.add(stack);
                            return stack;
                        }
                    }
                    // OK, we let it through now that we know it's not anywhere else.
                    return super.insertItem(slot, stack, simulate);
                }
            }
            // It simply wouldn't even fit in that slot anyway.
            return stack;
        }
    }
}
