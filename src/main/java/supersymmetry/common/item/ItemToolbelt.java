package supersymmetry.common.item;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.cleanroommc.modularui.api.IGuiHolder;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.ColorType;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.factory.HandGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widgets.layout.Grid;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.capability.GregtechCapabilities;
import gregtech.api.capability.IElectricItem;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.toolitem.IGTTool;
import gregtech.api.items.toolitem.ItemGTTool;
import gregtech.api.items.toolitem.ToolDefinitionBuilder;
import gregtech.api.items.toolitem.ToolHelper;
import gregtech.api.items.toolitem.aoe.AoESymmetrical;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.material.properties.ToolProperty;
import gregtech.api.util.LocalizationUtils;
import gregtech.api.util.TextFormattingUtil;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.items.behaviors.ColorSprayBehaviour;
import supersymmetry.api.items.IDyeableItem;

/**
 * Toolbelt: stores tools in NBT and delegates tool behaviour to the selected one.
 * <p>
 * Ported from GTCEu-Reloaded 43140b63f (LGPL-3.0) and reduced to the parts that do not
 * depend on fork-only GT APIs: no crafting-ingredient substitution, no maintenance hatch
 * shortcut, no colour-spray passthrough, no keybind/scroll selection. Selection is done
 * in the screen.
 */
public class ItemToolbelt extends ItemGTTool implements IDyeableItem, IGuiHolder<HandGuiData> {

    public static final int SLOTS = 4;

    public static ItemToolbelt TOOLBELT;

    public ItemToolbelt(String domain, String id) {
        super(domain, id, -1, new ToolDefinitionBuilder().cannotAttack().attackSpeed(-2.4F).build(),
                null, false, new HashSet<>(), "toolbelt", new ArrayList<>(), null);
    }

    /** Replaces a belt stack with its selected tool, mirroring the fork's ToolHelper.toolbeltPassthrough. */
    public static @NotNull ItemStack passthrough(@NotNull ItemStack stack) {
        if (stack.getItem() instanceof ItemToolbelt belt) {
            ItemStack selected = belt.getSelectedTool(stack);
            if (!selected.isEmpty()) return selected;
        }
        return stack;
    }

    /**
     * The inventory lives in the stack's own NBT, so copies never share it. One handler is cached
     * per ItemStack identity so that damage and NBT changes apply to the stack the belt holds.
     */
    private static final java.util.Map<ItemStack, ToolStackHandler> HANDLERS = new java.util.WeakHashMap<>();

    public @NotNull ToolStackHandler getHandler(@NotNull ItemStack stack) {
        int size = materialSlotCount(stack);
        ToolStackHandler cached = HANDLERS.get(stack);
        // A belt keeps the inventory it was saved with, so a cached handler larger than the material
        // allows is still the right one: only a smaller cache has to be rebuilt.
        if (cached != null && cached.getSlots() >= size) return cached;

        ToolStackHandler handler = new ToolStackHandler(stack, size);
        NBTTagCompound tag = stack.getTagCompound();
        if (tag != null && tag.hasKey("Belt")) {
            handler.deserializeNBT(tag.getCompoundTag("Belt"));
            // The saved size wins over the material: shrinking would delete the tools in the extra
            // slots, which is what a belt saved under a different slot count would suffer.
            handler.growTo(Math.max(size, handler.getSlots()));
        }
        HANDLERS.put(stack, handler);
        return handler;
    }

    // ---------------------------------------------------------------- capabilities

    /**
     * Exposes the selected tool's capabilities through the belt, as the fork's ToolbeltCapabilityProvider
     * does, and the belt's own inventory when nothing is selected. This is what lets a charger see the
     * tool's electric item capability through a belt sitting in a slot, and lets item automation fill the
     * belt.
     * <p>
     * The electric capability is wrapped so a charge or discharge through the belt is written back into
     * the belt's NBT: the tool's charge lives in the tool stack inside that tag, and nothing else would
     * persist it.
     */
    @Override
    public ICapabilityProvider initCapabilities(@NotNull ItemStack stack, @Nullable NBTTagCompound nbt) {
        return new ToolbeltCapabilityProvider(stack);
    }

    public static class ToolbeltCapabilityProvider implements ICapabilityProvider {

        private final ItemStack belt;

        private IElectricItem electricSource;
        private IElectricItem electricProxy;
        private IFluidHandlerItem fluidSource;
        private IFluidHandlerItem fluidProxy;

        ToolbeltCapabilityProvider(ItemStack belt) {
            this.belt = belt;
        }

        private ItemToolbelt item() {
            return (ItemToolbelt) belt.getItem();
        }

        @Override
        public boolean hasCapability(@NotNull Capability<?> capability, @Nullable EnumFacing facing) {
            ItemStack selected = item().getSelectedTool(belt);
            if (selected.isEmpty()) return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY;
            return selected.hasCapability(capability, facing);
        }

        @Override
        public <T> T getCapability(@NotNull Capability<T> capability, @Nullable EnumFacing facing) {
            ItemStack selected = item().getSelectedTool(belt);
            if (selected.isEmpty()) {
                return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ?
                        CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(item().getHandler(belt)) : null;
            }
            T delegate = selected.getCapability(capability, facing);
            if (delegate == null) return null;
            if (capability == GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM) {
                return GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM.cast(electric(selected));
            }
            if (capability == CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY) {
                return CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY.cast(fluid(selected));
            }
            return delegate;
        }

        /**
         * A fluid handler item hands its {@code getContainer()} back to Forge, which stows that stack
         * into the player's hand. The tool's handler would hand back the tool itself, so the belt was
         * replaced by the bare plunger - a plunger used from a belt appeared to delete the belt. The
         * proxy answers with the belt the capability was asked for, and writes the belt's inventory
         * back after a fill or drain so the wear the tool took survives the copy Forge makes.
         */
        private IFluidHandlerItem fluid(ItemStack selected) {
            IFluidHandlerItem real = selected.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
            if (real == null) return null;
            if (real != fluidSource) {
                fluidSource = real;
                fluidProxy = (IFluidHandlerItem) Proxy.newProxyInstance(ItemToolbelt.class.getClassLoader(),
                        new Class<?>[] { IFluidHandlerItem.class }, (proxy, method, args) -> {
                            if ("getContainer".equals(method.getName())) return belt;
                            Object result;
                            try {
                                result = method.invoke(real, args);
                            } catch (InvocationTargetException e) {
                                throw e.getCause();
                            }
                            boolean mutating = args != null && args.length > 1 &&
                                    Boolean.TRUE.equals(args[args.length - 1]);
                            if (mutating && ("fill".equals(method.getName()) || "drain".equals(method.getName()))) {
                                item().getHandler(belt).persist();
                            }
                            return result;
                        });
            }
            return fluidProxy;
        }

        private IElectricItem electric(ItemStack selected) {
            IElectricItem real = selected.getCapability(GregtechCapabilities.CAPABILITY_ELECTRIC_ITEM, null);
            if (real == null) return null;
            if (real != electricSource) {
                electricSource = real;
                electricProxy = (IElectricItem) Proxy.newProxyInstance(ItemToolbelt.class.getClassLoader(),
                        new Class<?>[] { IElectricItem.class }, (proxy, method, args) -> {
                            Object result;
                            try {
                                result = method.invoke(real, args);
                            } catch (InvocationTargetException e) {
                                throw e.getCause();
                            }
                            if ("charge".equals(method.getName()) || "discharge".equals(method.getName())) {
                                item().getHandler(belt).persist();
                            }
                            return result;
                        });
            }
            return electricProxy;
        }
    }

    // ---------------------------------------------------------------- selection

    public int getSelectedSlot(@NotNull ItemStack belt) {
        return getHandler(belt).getSelectedSlot();
    }

    public @NotNull ItemStack getSelectedTool(@NotNull ItemStack belt) {
        return getHandler(belt).getSelectedStack();
    }

    /** Slots the belt actually has: its saved inventory size, or the material's if that is larger. */
    public int getSlotCount(@NotNull ItemStack stack) {
        return getHandler(stack).getSlots();
    }

    /**
     * Slots the tool material allows, as in the fork (2 + harvest level, capped at 8).
     * Reads the tool NBT directly: going through getToolProperty would call ToolHelper.getToolTag,
     * which our passthrough mixin routes back here and would recurse.
     */
    public int materialSlotCount(@NotNull ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        if (tag == null || !tag.hasKey(ToolHelper.TOOL_TAG_KEY)) return SLOTS;

        String materialName = tag.getCompoundTag(ToolHelper.TOOL_TAG_KEY).getString(ToolHelper.MATERIAL_KEY);
        Material material = GregTechAPI.materialManager.getMaterial(materialName);
        if (material == null) return SLOTS;

        ToolProperty property = material.getProperty(PropertyKey.TOOL);
        return property == null ? SLOTS : Math.min(8, 2 + property.getToolHarvestLevel());
    }

    public @NotNull ItemStack getToolInSlot(@NotNull ItemStack stack, int slot) {
        ToolStackHandler handler = getHandler(stack);
        if (slot < 0 || slot >= handler.getSlots()) return ItemStack.EMPTY;
        return handler.getStackInSlot(slot);
    }

    public void setSelectedSlot(@NotNull ItemStack belt, int slot) {
        getHandler(belt).setSelectedSlot(slot);
    }

    /** The server rewrites this stack's NBT; drop the cached handler so the client re-reads it. */
    @Override
    public void readNBTShareTag(ItemStack stack, NBTTagCompound nbt) {
        stack.setTagCompound(nbt);
        HANDLERS.remove(stack);
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

    // Behaviour is delegated to the selected tool; its identity is not. Damage, metadata and max
    // damage stay the belt's own: Forge's ItemStack forwards those to the item, so delegating them
    // makes the belt's model lookup, stack comparison and damageability change with the selection,
    // which is what sent a belt holding a spray can to the missing model.

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
        if (selected.isEmpty()) return definition$hitEntity(stack, target, attacker);
        boolean result = selected.getItem().hitEntity(selected, target, attacker);
        getHandler(stack).persist();
        return result;
    }

    @Override
    public boolean canDisableShield(@NotNull ItemStack stack, @NotNull ItemStack shield,
                                    @NotNull EntityLivingBase entity, @NotNull EntityLivingBase attacker) {
        ItemStack selected = getSelectedTool(stack);
        return !selected.isEmpty() ? selected.getItem().canDisableShield(selected, shield, entity, attacker) :
                definition$canDisableShield(stack, shield, entity, attacker);
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
        if (selected.isEmpty()) return definition$onBlockDestroyed(stack, world, state, pos, entity);
        boolean result = selected.getItem().onBlockDestroyed(selected, world, state, pos, entity);
        // The tool lives in the belt's NBT: without this write-back the wear stays in memory and the
        // belt's bars only move when something else persists it, which used to mean reselecting.
        if (!world.isRemote) getHandler(stack).persist();
        return result;
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
    public @NotNull AoESymmetrical getMaxAoEDefinition(@NotNull ItemStack stack) {
        ItemStack selected = getSelectedTool(stack);
        if (!selected.isEmpty() && selected.getItem() instanceof IGTTool tool)
            return tool.getMaxAoEDefinition(selected);
        return super.getMaxAoEDefinition(stack);
    }

    @Override
    public @NotNull AoESymmetrical getAoEDefinition(@NotNull ItemStack stack) {
        ItemStack selected = getSelectedTool(stack);
        if (!selected.isEmpty() && selected.getItem() instanceof IGTTool tool) return tool.getAoEDefinition(selected);
        return super.getAoEDefinition(stack);
    }

    @Override
    public int getMetadata(@NotNull ItemStack stack) {
        ItemStack selected = getSelectedTool(stack);
        if (!selected.isEmpty()) return selected.getItem().getMetadata(selected);
        return super.getMetadata(stack);
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

    /**
     * GT never uses the vanilla durability bar: its ASM hook calls RenderItemHooks.renderElectricBar
     * for every IGTTool and ToolChargeBarRenderer reads this value as the fraction that is <em>left</em>
     * (1.0 = full). Vanilla items report the fraction that is damaged, so those get inverted.
     * <p>
     * RenderItemHooksMixin hands the selected tool's own bars (durability and, for electric tools,
     * the blue charge bar) to GT's renderer, so the belt looks exactly like the tool it holds.
     */
    @Override
    public double getDurabilityForDisplay(@NotNull ItemStack stack) {
        ItemStack selected = getSelectedTool(stack);
        if (selected.isEmpty()) return definition$getDurabilityForDisplay(stack);
        double display = selected.getItem().getDurabilityForDisplay(selected);
        // GT items (tools and metaitems such as spray cans) report what is left; vanilla items
        // report what is damaged. Inverting the former showed a full spray can as an empty bar.
        if (!(selected.getItem() instanceof IGTTool) && !(selected.getItem() instanceof MetaItem)) {
            display = 1 - display;
        }
        return display;
    }

    @Override
    public void wrenchUsed(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult rayTrace) {
        ItemStack selected = getSelectedTool(wrench);
        if (!selected.isEmpty() && selected.getItem() instanceof IGTTool tool) {
            tool.wrenchUsed(player, hand, selected, rayTrace);
            getHandler(wrench).persist();
        } else super.wrenchUsed(player, hand, wrench, rayTrace);
    }

    @Override
    public void toolUsed(ItemStack item, EntityLivingBase user, BlockPos pos) {
        ItemStack selected = getSelectedTool(item);
        if (!selected.isEmpty() && selected.getItem() instanceof IGTTool tool) {
            tool.toolUsed(selected, user, pos);
            getHandler(item).persist();
        } else super.toolUsed(item, user, pos);
    }

    @Override
    public void used(@NotNull EnumHand hand, @NotNull EntityPlayer player, @NotNull BlockPos pos) {
        ItemStack belt = player.getHeldItem(hand);
        ItemStack selected = getSelectedTool(belt);
        if (!selected.isEmpty() && selected.getItem() instanceof IGTTool tool) {
            tool.used(hand, player, pos);
            getHandler(belt).persist();
        } else super.used(hand, player, pos);
    }

    @Override
    public @NotNull EnumActionResult onItemUseFirst(@NotNull EntityPlayer player, @NotNull World world,
                                                    @NotNull BlockPos pos, @NotNull EnumFacing side, float hitX,
                                                    float hitY, float hitZ, @NotNull EnumHand hand) {
        EnumActionResult dyed = IDyeableItem.super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
        if (dyed != EnumActionResult.PASS) return dyed;
        return super.onItemUseFirst(player, world, pos, side, hitX, hitY, hitZ, hand);
    }

    /**
     * Right-clicking a block with a selected tool belongs to that tool, never to the belt. The click
     * is consumed on both sides so the client does not fall through to onItemRightClick, which is
     * what sent a second packet and opened the screen on top of the action.
     * <p>
     * Spray cans act through Item.onItemUse, which reads the held stack, so the can is put in hand
     * for that one call on the server. Every other tool is handled by GT itself, through the
     * ToolHelper passthrough, and never has its stack moved.
     */
    @Override
    public @NotNull EnumActionResult onItemUse(@NotNull EntityPlayer player, @NotNull World world,
                                               @NotNull BlockPos pos, @NotNull EnumHand hand,
                                               @NotNull EnumFacing facing, float hitX, float hitY, float hitZ) {
        ItemStack belt = player.getHeldItem(hand);
        ItemStack selected = getSelectedTool(belt);
        if (selected.isEmpty()) return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);

        // Sneak with an area tool belongs to that tool's area editor, which GT opens from
        // onItemRightClick. Passing here on both sides lets the click fall through to it, the same as
        // it does for the tool in hand.
        if (player.isSneaking() && selected.getItem() instanceof IGTTool tool &&
                tool.getMaxAoEDefinition(selected) != AoESymmetrical.none()) {
            return super.onItemUse(player, world, pos, hand, facing, hitX, hitY, hitZ);
        }

        // The client only predicts; the server decides. Returning SUCCESS keeps the belt screen shut.
        if (world.isRemote) return EnumActionResult.SUCCESS;
        if (!ToolStackHandler.isSpraycan(selected)) return EnumActionResult.SUCCESS;

        ToolStackHandler handler = getHandler(belt);
        int slot = handler.getSelectedSlot();
        player.setHeldItem(hand, selected);
        try {
            EnumActionResult result = selected.getItem().onItemUse(player, world, pos, hand, facing, hitX, hitY,
                    hitZ);
            // A spray can that runs out of uses replaces the held stack with an empty can.
            ItemStack after = player.getHeldItem(hand);
            if (after != selected && slot >= 0) handler.setStackInSlot(slot, after);
            if (result != EnumActionResult.PASS) return result;
        } finally {
            handler.persist();
            player.setHeldItem(hand, belt);
        }
        return EnumActionResult.SUCCESS;
    }

    // ---------------------------------------------------------------- display

    @Override
    public @NotNull String getItemStackDisplayName(@NotNull ItemStack stack) {
        ToolStackHandler handler = getHandler(stack);
        ItemStack tool = handler.getSelectedStack();
        // The passthrough resolves getToolTag to the selected tool, so the material read here would be
        // the tool's ("Iron Toolbelt" for a tungsten belt holding an iron pickaxe). The fork switches it
        // off while naming the belt; do the same.
        handler.disablePassthrough();
        String name;
        if (!tool.isEmpty()) {
            name = LocalizationUtils.format(getTranslationKey() + ".select", getToolMaterial(stack).getLocalizedName(),
                    tool.getDisplayName());
        } else {
            name = LocalizationUtils.format(getTranslationKey(), getToolMaterial(stack).getLocalizedName());
        }
        handler.enablePassthrough();
        return name;
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
        tooltip.add(I18n.format("item.gt.tool.toolbelt.size", TextFormattingUtil.formatNumbers(handler.getSlots())));
        tooltip.add("");
        if (TooltipHelper.isShiftDown()) {
            tooltip.add(I18n.format("item.gt.tool.toolbelt.tooltip"));
        } else {
            tooltip.add(I18n.format("gregtech.tooltip.hold_shift"));
        }
        if (TooltipHelper.isCtrlDown()) {
            tooltip.add("");
            for (int i = 0; i < handler.getSlots(); i++) {
                ItemStack tool = handler.getStackInSlot(i);
                String name = tool.isEmpty() ? "x" : tool.getDisplayName();
                tooltip.add(I18n.format(handler.selectedSlot == i ? "item.gt.tool.toolbelt.selected" :
                        "item.gt.tool.toolbelt.tool", i + 1, name));
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

    private static final UITexture SLOT = new UITexture.Builder()
            .location(GTValues.MODID, "textures/gui/base/slot.png")
            .imageSize(18, 18)
            .adaptable(1)
            .build();
    private static final UITexture TOOL_SLOT_OVERLAY = UITexture.fullImage(GTValues.MODID,
            "textures/gui/overlay/tool_slot_overlay.png", ColorType.DEFAULT);

    @Override
    public ModularPanel buildUI(HandGuiData guiData, PanelSyncManager guiSyncManager, UISettings settings) {
        final ItemStack usedStack = guiData.getUsedItemStack();
        final ToolStackHandler handler = getHandler(usedStack);

        int heightBonus = (handler.getSlots() / 9) * 18;
        SlotGroup group = new SlotGroup("toolbelt_inventory", Math.min(handler.getSlots(), 9));
        guiSyncManager.registerSlotGroup(group);

        List<ItemSlot> slots = new ArrayList<>();
        for (int i = 0; i < handler.getSlots(); i++) {
            slots.add(new ItemSlot());
        }

        return ModularPanel.defaultPanel(usedStack.getTranslationKey(), 176, 120 + heightBonus + 12)
                .child(IKey.str(usedStack.getDisplayName()).asWidget().pos(5, 5).height(12))
                .child(new Grid().margin(0).leftRel(0.5f).top(7 + 12).coverChildren()
                        .mapTo(group.getRowSize(), slots, (index, value) -> value
                                .slot(SyncHandlers.itemSlot(handler, index).slotGroup(group)
                                        .changeListener((newItem, onlyAmountChanged, client, init) -> handler
                                                .notifyContentsChanged(index)))
                                .background(SLOT, TOOL_SLOT_OVERLAY)
                                .name("slot_" + index))
                        .name("toolbelt_inventory"))
                .bindPlayerInventory();
    }

    /**
     * Sound pass-through. Not in the fork: getSound() is item-level and the belt has none, so
     * playSound is forwarded to the selected tool, whose own getSound() is used.
     */
    @Override
    public boolean playSoundOnBlockDestroy() {
        return true;
    }

    @Override
    public void playSound(@NotNull EntityPlayer player) {
        ItemStack selected = getSelectedTool(player.getHeldItemMainhand());
        if (!selected.isEmpty() && selected.getItem() instanceof IGTTool tool) {
            tool.playSound(player);
            return;
        }
        super.playSound(player);
    }

    @Override
    public void playCraftingSound(@NotNull EntityPlayer player, @NotNull ItemStack stack) {
        ItemStack selected = getSelectedTool(stack);
        if (!selected.isEmpty() && selected.getItem() instanceof IGTTool tool) {
            tool.playCraftingSound(player, selected);
            return;
        }
        super.playCraftingSound(player, stack);
    }

    /** Sneak + hotbar key selection; the fork redirects Minecraft's hotbar key handling for this. */
    @SideOnly(Side.CLIENT)
    public void changeSelectedToolHotkey(int slot, @NotNull ItemStack stack) {
        ToolStackHandler handler = getHandler(stack);
        handler.setSelectedSlot(slot);
        supersymmetry.network.PacketToolbeltSelectionChange.toServer(handler.getSelectedSlot(), -1);
    }

    /** Scroll selection while holding the belt; the fork drives this from a client input handler. */
    @SideOnly(Side.CLIENT)
    public void changeSelectedToolMousewheel(int direction, @NotNull ItemStack stack) {
        ToolStackHandler handler = getHandler(stack);
        if (direction < 0) handler.incrementSelectedSlot();
        else handler.decrementSelectedSlot();
        supersymmetry.network.PacketToolbeltSelectionChange.toServer(handler.getSelectedSlot(), -1);
    }

    @Override
    public @NotNull ActionResult<ItemStack> onItemRightClick(@NotNull World world, @NotNull EntityPlayer player,
                                                             @NotNull EnumHand hand) {
        ItemStack stack = player.getHeldItem(hand);

        // The fork iterates the selected tool's behaviours before the belt's own OpenGUIBehavior,
        // so a selected tool gets the click and only a pass opens the screen.
        if (!world.isRemote && player instanceof EntityPlayerMP serverPlayer) {
            ItemStack selected = getSelectedTool(stack);
            // GT's own rule for tool UI: sneak with an area-mining tool opens its area settings. The
            // fork's belt hands its screen over to the selected tool; in 2.8.10 the only tool UI is that
            // MUI1 editor, and it reads and writes through getBehaviorsTag, which the passthrough already
            // points at the selected tool. The holder's item stays the belt, so edits land in the tool.
            if (player.isSneaking() && !selected.isEmpty() && selected.getItem() instanceof IGTTool tool &&
                    tool.getMaxAoEDefinition(selected) != AoESymmetrical.none()) {
                getHandler(stack).persist();
                PlayerInventoryHolder.openHandItemUI(player, hand);
            } else {
                com.cleanroommc.modularui.factory.ItemGuiFactory.INSTANCE.open(serverPlayer, hand);
            }
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, stack);
    }

    // ---------------------------------------------------------------- handler

    public static class ToolStackHandler extends ItemStackHandler {

        private static final Set<String> EMPTY = ImmutableSet.of();

        private int selectedSlot = -1;
        private boolean passthrough = true;
        private final Set<String> toolClasses = new HashSet<>();

        private final ItemStack owner;

        public ToolStackHandler(ItemStack owner, int size) {
            super(size);
            this.owner = owner;
        }

        public void persist() {
            if (owner == null) return;
            NBTTagCompound tag = owner.getTagCompound();
            if (tag == null) {
                tag = new NBTTagCompound();
                owner.setTagCompound(tag);
            }
            tag.setTag("Belt", serializeNBT());
        }

        public void setSelectedSlot(int slot) {
            this.selectedSlot = (slot < 0 || slot >= getSlots()) ? -1 : slot;
            persist();
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
            return slot < 0 || slot >= getSlots() ? ItemStack.EMPTY : getStackInSlot(slot);
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
            return isTool(stack) || isSpraycan(stack);
        }

        private static boolean isTool(@NotNull ItemStack stack) {
            return stack.getItem() instanceof IGTTool || stack.getItem() instanceof ItemTool;
        }

        /** Mirrors the fork's ToolHelper.isSpraycan: any metaitem carrying a colour spray behaviour. */
        public static boolean isSpraycan(@NotNull ItemStack stack) {
            if (stack.getItem() instanceof MetaItem<?> meta) {
                for (IItemBehaviour behaviour : meta.getBehaviours(stack)) {
                    if (behaviour instanceof ColorSprayBehaviour) return true;
                }
            }
            return false;
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

        public void incrementSelectedSlot() {
            stepSelection(true);
        }

        public void decrementSelectedSlot() {
            stepSelection(false);
        }

        /**
         * Moves to the next filled slot, wrapping around. Empty slots are never selected: on a full
         * belt the old cycle also stepped through "nothing selected" and through any gap, which is
         * the empty slot the scroll appeared to stop on.
         */
        private void stepSelection(boolean forward) {
            int slots = getSlots();
            int index = this.selectedSlot;
            for (int step = 0; step < slots; step++) {
                index = forward ? (index + 1 >= slots ? 0 : index + 1) : (index - 1 < 0 ? slots - 1 : index - 1);
                if (!getStackInSlot(index).isEmpty()) {
                    this.selectedSlot = index;
                    persist();
                    return;
                }
            }
            this.selectedSlot = -1;
            persist();
        }

        /** Grows the handler without dropping the stacks it already holds. */
        public void growTo(int size) {
            if (size <= getSlots()) return;
            NonNullList<ItemStack> previous = this.stacks;
            setSize(size);
            for (int i = 0; i < previous.size(); i++) this.stacks.set(i, previous.get(i));
        }

        /** Public hook for the screen's slot change listener. */
        public void notifyContentsChanged(int slot) {
            toolClasses.clear();
        }

        @Override
        protected void onContentsChanged(int slot) {
            toolClasses.clear();
            persist();
        }
    }
}
