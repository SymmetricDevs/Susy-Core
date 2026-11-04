package supersymmetry.common.item;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ItemGTTool;
import gregtech.api.items.toolitem.ToolDefinitionBuilder;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.TooltipHelper;
import supersymmetry.api.items.IDyeableItem;

/**
 * Toolbelt: stores tools in NBT and delegates tool behaviour to the selected one.
 * <p>
 * Ported from GTCEu-Reloaded 43140b63f (LGPL-3.0) and reduced to the parts that do not
 * depend on fork-only GT APIs: no crafting-ingredient substitution, no maintenance hatch
 * shortcut, no colour-spray passthrough, no keybind/scroll selection. Selection is done
 * in the screen.
 */
public class ItemToolbelt extends ItemGTTool implements IDyeableItem {

    public static final int SLOTS = 4;

    public static ItemToolbelt TOOLBELT;

    public ItemToolbelt(String domain, String id) {
        super(domain, id, -1, new ToolDefinitionBuilder().cannotAttack().attackSpeed(-2.4F).build(),
                null, false, new HashSet<>(), "toolbelt", new ArrayList<>(), () -> ItemStack.EMPTY);
    }

    public @NotNull ToolStackHandler getHandler(@NotNull ItemStack stack) {
        if (stack.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY,
                null) instanceof ToolStackHandler handler) {
            return handler;
        }
        return FALLBACK;
    }

    private static final ToolStackHandler FALLBACK = new ToolStackHandler(0);

    @Override
    public @NotNull ICapabilityProvider initCapabilities(@NotNull ItemStack stack, NBTTagCompound nbt) {
        return new ToolbeltCapabilityProvider(stack);
    }

    @Override
    public NBTTagCompound getNBTShareTag(ItemStack stack) {
        NBTTagCompound tag = new NBTTagCompound();
        if (stack.getTagCompound() != null) tag.setTag("NBT", stack.getTagCompound());
        tag.setTag("Cap", getHandler(stack).serializeNBT());
        return tag;
    }

    @Override
    public void readNBTShareTag(ItemStack stack, NBTTagCompound nbt) {
        stack.setTagCompound(nbt == null ? null : (nbt.hasKey("NBT") ? nbt.getCompoundTag("NBT") : null));
        if (nbt != null && nbt.hasKey("Cap")) getHandler(stack).deserializeNBT(nbt.getCompoundTag("Cap"));
    }

    // ---------------------------------------------------------------- selection

    public int getSelectedSlot(@NotNull ItemStack belt) {
        return getHandler(belt).getSelectedSlot();
    }

    public @NotNull ItemStack getSelectedTool(@NotNull ItemStack belt) {
        return getHandler(belt).getSelectedStack();
    }

    public void setSelectedSlot(@NotNull ItemStack belt, int slot) {
        getHandler(belt).setSelectedSlot(slot);
    }

    // ---------------------------------------------------------------- crafting

    private static final ThreadLocal<Integer> lastSlot = ThreadLocal.withInitial(() -> -999);
    private static final ThreadLocal<EntityPlayerMP> lastPlayer = ThreadLocal.withInitial(() -> null);

    /** Called from the container hook while a toolbelt is on the cursor. */
    public static void setCraftingSlot(int slot, EntityPlayerMP player) {
        lastSlot.set(slot);
        lastPlayer.set(player);
    }

    public static boolean checkIngredientAgainstToolbelt(ItemStack input, Ingredient ingredient) {
        if (input.getItem() instanceof ItemToolbelt belt && belt.supportsIngredient(input, ingredient)) {
            belt.setOnCraftIngredient(input, ingredient);
            return true;
        }
        return false;
    }

    public boolean supportsIngredient(ItemStack stack, Ingredient ingredient) {
        return getHandler(stack).checkIngredientAgainstTools(ingredient) != -1;
    }

    public void setOnCraftIngredient(ItemStack stack, Ingredient ingredient) {
        int match = getHandler(stack).checkIngredientAgainstTools(ingredient);
        if (match != -1) {
            getHandler(stack).setSelectedSlot(match);
            EntityPlayerMP player = lastPlayer.get();
            if (player != null) {
                supersymmetry.network.PacketToolbeltSelectionChange.toClient(match, lastSlot.get(), player);
            }
        }
    }

    // ---------------------------------------------------------------- delegation

    @Override
    public float getDestroySpeed(@NotNull ItemStack stack, @NotNull IBlockState state) {
        ItemStack selected = getSelectedTool(stack);
        return !selected.isEmpty() ? selected.getItem().getDestroySpeed(selected, state) :
                definition$getDestroySpeed(stack, state);
    }

    @Override
    public boolean hitEntity(@NotNull ItemStack stack, @NotNull EntityLivingBase target,
                             @NotNull EntityLivingBase attacker) {
        ItemStack selected = getSelectedTool(stack);
        return !selected.isEmpty() ? selected.getItem().hitEntity(selected, target, attacker) :
                definition$hitEntity(stack, target, attacker);
    }

    @Override
    public boolean onBlockStartBreak(@NotNull ItemStack stack, @NotNull BlockPos pos, @NotNull EntityPlayer player) {
        ItemStack selected = getSelectedTool(stack);
        return !selected.isEmpty() ? selected.getItem().onBlockStartBreak(selected, pos, player) :
                definition$onBlockStartBreak(stack, pos, player);
    }

    @Override
    public boolean onBlockDestroyed(@NotNull ItemStack stack, @NotNull World world, @NotNull IBlockState state,
                                    @NotNull BlockPos pos, @NotNull EntityLivingBase entity) {
        ItemStack selected = getSelectedTool(stack);
        return !selected.isEmpty() ? selected.getItem().onBlockDestroyed(selected, world, state, pos, entity) :
                definition$onBlockDestroyed(stack, world, state, pos, entity);
    }

    @Override
    public boolean getIsRepairable(@NotNull ItemStack toRepair, @NotNull ItemStack repair) {
        ItemStack selected = getSelectedTool(toRepair);
        return !selected.isEmpty() ? selected.getItem().getIsRepairable(selected, repair) :
                definition$getIsRepairable(toRepair, repair);
    }

    @NotNull @Override
    public Multimap<String, AttributeModifier> getAttributeModifiers(@NotNull EntityEquipmentSlot slot,
                                                                     @NotNull ItemStack stack) {
        ItemStack selected = getSelectedTool(stack);
        return !selected.isEmpty() ? selected.getItem().getAttributeModifiers(slot, selected) :
                definition$getAttributeModifiers(slot, stack);
    }

    @Override
    public int getHarvestLevel(@NotNull ItemStack stack, @NotNull String toolClass, @Nullable EntityPlayer player,
                               @Nullable IBlockState state) {
        ItemStack selected = getSelectedTool(stack);
        return !selected.isEmpty() ? selected.getItem().getHarvestLevel(stack, toolClass, player, state) :
                definition$getHarvestLevel(stack, toolClass, player, state);
    }

    @NotNull @Override
    public Set<String> getToolClasses(@NotNull ItemStack stack) {
        return getHandler(stack).getToolClasses();
    }

    @Override
    public boolean canHarvestBlock(@NotNull IBlockState state, @NotNull ItemStack stack) {
        ItemStack selected = getSelectedTool(stack);
        return !selected.isEmpty() ? selected.getItem().canHarvestBlock(state, selected) :
                ToolHelper.isToolEffective(state, getToolClasses(stack), getTotalHarvestLevel(stack));
    }

    @Override
    public boolean doesSneakBypassUse(@NotNull ItemStack stack, @NotNull IBlockAccess world, @NotNull BlockPos pos,
                                      @NotNull EntityPlayer player) {
        ItemStack selected = getSelectedTool(stack);
        return !selected.isEmpty() ? selected.getItem().doesSneakBypassUse(selected, world, pos, player) :
                definition$doesSneakBypassUse(stack, world, pos, player);
    }

    @Override
    public boolean onEntitySwing(@NotNull EntityLivingBase entity, @NotNull ItemStack stack) {
        ItemStack selected = getSelectedTool(stack);
        return !selected.isEmpty() ? selected.getItem().onEntitySwing(entity, selected) :
                definition$onEntitySwing(entity, stack);
    }

    @Override
    public boolean canDestroyBlockInCreative(@NotNull World world, @NotNull BlockPos pos, @NotNull ItemStack stack,
                                             @NotNull EntityPlayer player) {
        ItemStack selected = getSelectedTool(stack);
        return !selected.isEmpty() ? selected.getItem().canDestroyBlockInCreative(world, pos, selected, player) :
                definition$canDestroyBlockInCreative(world, pos, stack, player);
    }

    @Override
    public boolean shouldCauseReequipAnimation(@NotNull ItemStack oldStack, @NotNull ItemStack newStack,
                                               boolean slotChanged) {
        return false;
    }

    @Override
    public boolean isDamaged(@NotNull ItemStack stack) {
        ItemStack selected = getSelectedTool(stack);
        return !selected.isEmpty() ? selected.getItem().isDamaged(selected) : definition$isDamaged(stack);
    }

    @Override
    public int getDamage(@NotNull ItemStack stack) {
        ItemStack selected = getSelectedTool(stack);
        return !selected.isEmpty() ? selected.getItem().getDamage(selected) : definition$getDamage(stack);
    }

    @Override
    public int getMaxDamage(@NotNull ItemStack stack) {
        ItemStack selected = getSelectedTool(stack);
        return !selected.isEmpty() ? selected.getItem().getMaxDamage(selected) : definition$getMaxDamage(stack);
    }

    @Override
    public void setDamage(@NotNull ItemStack stack, int damage) {
        ItemStack selected = getSelectedTool(stack);
        if (!selected.isEmpty()) selected.getItem().setDamage(selected, damage);
        else definition$setDamage(stack, damage);
    }

    @Override
    public double getDurabilityForDisplay(@NotNull ItemStack stack) {
        ItemStack selected = getSelectedTool(stack);
        if (!selected.isEmpty()) {
            double display = selected.getItem().getDurabilityForDisplay(selected);
            if (selected.getItem() instanceof ItemTool) display = 1 - display;
            return display;
        }
        return definition$getDurabilityForDisplay(stack);
    }

    @Override
    public void wrenchUsed(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        ItemStack selected = getSelectedTool(wrench);
        if (!selected.isEmpty() && selected.getItem() instanceof IGTTool tool) tool.wrenchUsed(player, hand, selected,
                rayTrace);
        else super.wrenchUsed(player, hand, wrench, rayTrace);
    }

    @Override
    public void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos) {
        ItemStack selected = getSelectedTool(item);
        if (!selected.isEmpty() && selected.getItem() instanceof IGTTool tool) tool.toolUsed(selected, user, pos);
        else super.toolUsed(item, user, pos);
    }

    @Override
    public void used(@NotNull EnumHand hand, @NotNull EntityPlayer player, @NotNull BlockPos pos) {
        ItemStack selected = getSelectedTool(player.getHeldItem(hand));
        if (!selected.isEmpty() && selected.getItem() instanceof IGTTool tool) tool.used(hand, player, pos);
        else super.used(hand, player, pos);
    }

    @Override
    public @NotNull EnumActionResult onItemUseFirst(@NotNull EntityPlayer player, @NotNull World world,
                                                    @NotNull BlockPos pos, @NotNull EnumFacing side, float hitX,
                                                    float hitY, float hitZ, @NotNull EnumHand hand) {
        EnumActionResult dyed = IDyeableItem.super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
        if (dyed != EnumActionResult.PASS) return dyed;
        EnumActionResult delegated = withSelectedTool(player, hand,
                tool -> tool.getItem().onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand));
        if (delegated != EnumActionResult.PASS) return delegated;
        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    @Override
    public @NotNull EnumActionResult onItemUse(@NotNull EntityPlayer player, @NotNull World world,
                                               @NotNull BlockPos pos, @NotNull EnumHand hand,
                                               @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        EnumActionResult delegated = withSelectedTool(player, hand,
                tool -> tool.getItem().onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ));
        if (delegated != EnumActionResult.PASS) return delegated;
        return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
    }

    /** Runs an action with the selected tool temporarily in hand, then restores the belt. */
    private EnumActionResult withSelectedTool(EntityPlayer player, EnumHand hand,
                                              java.util.function.Function<ItemStack, EnumActionResult> action) {
        ItemStack belt = player.getHeldItem(hand);
        if (!(belt.getItem() instanceof ItemToolbelt toolbelt)) return EnumActionResult.PASS;
        ItemStack selected = toolbelt.getSelectedTool(belt);
        if (selected.isEmpty()) return EnumActionResult.PASS;

        player.setHeldItem(hand, selected);
        try {
            return action.apply(selected);
        } finally {
            player.setHeldItem(hand, belt);
        }
    }

    // ---------------------------------------------------------------- display

    @Override
    public @NotNull String getItemStackDisplayName(@NotNull ItemStack stack) {
        ItemStack tool = getSelectedTool(stack);
        if (!tool.isEmpty()) {
            return LocalizationUtils.format(getTranslationKey() + ".select", getToolMaterial(stack).getLocalizedName(),
                    tool.getDisplayName());
        }
        return LocalizationUtils.format(getTranslationKey(), getToolMaterial(stack).getLocalizedName());
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(@NotNull ItemStack stack, @Nullable World world, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag flag) {
        ToolStackHandler handler = getHandler(stack);
        ItemStack selected = handler.getSelectedStack();
        if (!selected.isEmpty()) {
            selected.getItem().addInformation(selected, world, tooltip, flag);
            return;
        }
        tooltip.add(I18n.format("item.susy.toolbelt.size", TextFormattingUtil.formatNumbers(handler.getSlots())));
        tooltip.add("");
        if (TooltipHelper.isShiftDown()) {
            tooltip.add(I18n.format("item.susy.toolbelt.tooltip"));
        } else {
            tooltip.add(I18n.format("gregtech.tooltip.hold_shift"));
        }
        if (TooltipHelper.isCtrlDown()) {
            tooltip.add("");
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack tool = handler.getStackInSlot(i);
                String name = tool.isEmpty() ? "x" : tool.getDisplayName();
                tooltip.add(I18n.format(handler.selectedSlot == i ? "item.susy.toolbelt.selected" :
                        "item.susy.toolbelt.tool", i + 1, name));
            }
        } else {
            tooltip.add(I18n.format("gregtech.tooltip.hold_ctrl"));
        }
    }

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 0) return this.getColor(stack);
        getHandler(stack).disablePassthrough();
        int color = super.getColor(stack, tintIndex);
        getHandler(stack).enablePassthrough();
        return color;
    }

    @Override
    public int getDefaultColor(ItemStack stack) {
        return 0xA06540;
    }

    @Override
    public boolean shouldGetContainerItem() {
        return false;
    }

    @Override
    public boolean hasContainerItem(@NotNull ItemStack stack) {
        return true;
    }

    @Override
    public @NotNull ItemStack getContainerItem(@NotNull ItemStack stack) {
        if (getHandler(stack).dealCraftDamageToSelected()) return stack.copy();
        return super.getContainerItem(stack);
    }

    // ---------------------------------------------------------------- screen

    @Override
    public ModularUI createUI(@NotNull PlayerInventoryHolder holder, @NotNull EntityPlayer player) {
        ItemStack stack = holder.getCurrentItem();
        ToolStackHandler handler = getHandler(stack);

        ModularUI.Builder builder = ModularUI.defaultBuilder();
        for (int i = 0; i < handler.getSlots(); i++) {
            int index = i;
            builder.widget(new SlotWidget(handler, i, 8 + (i % 4) * 18, 8, true, true));
            builder.widget(new ClickButtonWidget(8 + (i % 4) * 18, 30, 16, 16, String.valueOf(i + 1),
                    data -> handler.setSelectedSlot(index)));
        }
        builder.bindPlayerInventory(player.inventory, 8 + 18 * 3);
        return builder.build(holder, player);
    }

    // ---------------------------------------------------------------- handler

    public static class ToolStackHandler extends ItemStackHandler {

        private static final Set<String> EMPTY = ImmutableSet.of();

        private int selectedSlot = -1;
        private boolean passthrough = true;
        private final Set<String> toolClasses = new HashSet<>();

        public ToolStackHandler(int size) {
            super(size);
        }

        public void setSelectedSlot(int slot) {
            this.selectedSlot = (slot < 0 || slot >= getSlots()) ? -1 : slot;
        }

        public int getSelectedSlot() {
            return passthrough ? selectedSlot : -1;
        }

        public void enablePassthrough() {
            this.passthrough = true;
        }

        public void disablePassthrough() {
            this.passthrough = false;
        }

        public @NotNull ItemStack getSelectedStack() {
            int slot = getSelectedSlot();
            return slot == -1 ? ItemStack.EMPTY : getStackInSlot(slot);
        }

        public Set<String> getToolClasses() {
            ItemStack selected = getSelectedStack();
            if (!selected.isEmpty()) {
                if (selected.getItem() instanceof IGTTool tool) return tool.getToolClasses(selected);
                if (selected.getItem() instanceof ItemTool tool) return tool.getToolClasses(selected);
            }
            return EMPTY;
        }

        public int checkIngredientAgainstTools(Ingredient ingredient) {
            for (int i = 0; i < getSlots(); i++) {
                ItemStack stack = getStackInSlot(i);
                if (!stack.isEmpty() && ingredient.test(stack)) return i;
            }
            return -1;
        }

        public boolean dealCraftDamageToSelected() {
            if (selectedSlot != -1) {
                dealCraftDamageToSlot(selectedSlot);
                return true;
            }
            return false;
        }

        public void dealCraftDamageToSlot(int slot) {
            ItemStack stack = getStackInSlot(slot);
            if (!stack.isEmpty()) setStackInSlot(slot, stack.getItem().getContainerItem(stack));
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (stack.getItem() instanceof ItemToolbelt) return false;
            return stack.getItem() instanceof IGTTool || stack.getItem() instanceof ItemTool;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            NBTTagCompound tag = super.serializeNBT();
            if (selectedSlot != -1) tag.setByte("SelectedSlot", (byte) selectedSlot);
            return tag;
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            super.deserializeNBT(nbt);
            if (nbt.hasKey("SelectedSlot")) selectedSlot = nbt.getByte("SelectedSlot");
        }

        @Override
        protected void onContentsChanged(int slot) {
            toolClasses.clear();
        }
    }

    public static class ToolbeltCapabilityProvider implements ICapabilityProvider, INBTSerializable<NBTTagCompound> {

        private final ItemStack stack;
        private ToolStackHandler handler;

        public ToolbeltCapabilityProvider(ItemStack stack) {
            this.stack = stack;
        }

        private @NotNull ToolStackHandler getHandler() {
            if (handler == null) handler = new ToolStackHandler(slotCount());
            return handler;
        }

        private int slotCount() {
            // Fixed size for the first pass; the fork derives this from the tool material.
            return SLOTS;
        }

        @Override
        public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
            return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
        }

        @Override
        public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
            if (capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
                return CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(getHandler());
            return null;
        }

        @Override
        public NBTTagCompound serializeNBT() {
            return getHandler().serializeNBT();
        }

        @Override
        public void deserializeNBT(NBTTagCompound nbt) {
            getHandler().deserializeNBT(nbt.copy());
        }
    }
}
