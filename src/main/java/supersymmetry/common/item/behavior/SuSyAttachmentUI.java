package supersymmetry.common.item.behavior;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.items.IItemHandlerModifiable;

import org.jetbrains.annotations.NotNull;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.widget.IWidget;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.sync.ItemSlotSH;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ItemDisplayWidget;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.slot.ItemSlot;
import com.cleanroommc.modularui.widgets.slot.ModularSlot;

import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.guns.attachment.AttachmentSlot;
import supersymmetry.api.guns.attachment.AttachmentStats;

/**
 * Gun attachment screen: the slots around a non-interactive view of the gun, the stats they add up to, and the player
 * inventory. Slots write straight to the gun stack's NBT and are synced; a locked slot is crossed out and refuses
 * changes.
 */
public final class SuSyAttachmentUI {

    private static final String LANG_KEY = "susy.gui.gun_attachments";
    private static final String SLOT_SYNC_KEY = "gun_attachment_";
    private static final String STAT_SYNC_KEY = "gun_stat_";
    private static final int SLOT_SIZE = 18;
    private static final int GAP = 2;

    private SuSyAttachmentUI() {}

    public static Builder builder(ItemStack gun, Supplier<AttachmentStats> stats, PanelSyncManager syncManager) {
        return new Builder(gun, stats, syncManager);
    }

    /** Lets the caller decide which slots can be changed. */
    public static final class Builder {

        private final ItemStack gun;
        private final Supplier<AttachmentStats> stats;
        private final PanelSyncManager syncManager;
        private final Set<AttachmentSlot> changeable = EnumSet.allOf(AttachmentSlot.class);

        private Builder(ItemStack gun, Supplier<AttachmentStats> stats, PanelSyncManager syncManager) {
            this.gun = gun;
            this.stats = stats;
            this.syncManager = syncManager;
        }

        public Builder changeable(Collection<AttachmentSlot> slots) {
            changeable.clear();
            for (AttachmentSlot slot : slots) {
                changeable.add(Objects.requireNonNull(slot, "slot"));
            }
            return this;
        }

        public ModularPanel build() {
            SlotInventory handler = new SlotInventory(gun, changeable);

            Flow layout = new Row().coverChildren().childPadding(12)
                    .crossAxisAlignment(Alignment.CrossAxis.CENTER)
                    .child(attachmentGrid(handler))
                    .child(statsColumn());

            ModularPanel panel = ModularPanel.defaultPanel("gun_attachments", 200, 220);
            panel.child(new Column().coverChildren().left(8).top(8)
                    .crossAxisAlignment(Alignment.CrossAxis.START).childPadding(4)
                    .child(IKey.lang(LANG_KEY + ".title").asWidget())
                    .child(layout));
            panel.child(SlotGroupWidget.playerInventory(8, true).horizontalCenter());
            return panel;
        }

        private Flow attachmentGrid(SlotInventory handler) {
            Flow grid = new Column().coverChildren().childPadding(GAP)
                    .crossAxisAlignment(Alignment.CrossAxis.CENTER);
            grid.child(gridRow(spacer(), slotWidget(handler, AttachmentSlot.TOP), spacer()));
            grid.child(gridRow(slotWidget(handler, AttachmentSlot.STOCK), gunDisplay(),
                    slotWidget(handler, AttachmentSlot.MUZZLE)));
            grid.child(gridRow(slotWidget(handler, AttachmentSlot.LATERAL),
                    slotWidget(handler, AttachmentSlot.UNDERSIDE), spacer()));
            return grid;
        }

        private Flow gridRow(IWidget left, IWidget middle, IWidget right) {
            return new Row().coverChildren().childPadding(GAP).child(left).child(middle).child(right);
        }

        private ItemSlot slotWidget(SlotInventory handler, AttachmentSlot slot) {
            ItemSlotSH syncHandler = new ItemSlotSH(new ModularSlot(handler, slot.ordinal()));
            syncManager.syncValue(SLOT_SYNC_KEY + slot.getSerializedName(), syncHandler);

            ItemSlot widget = new ItemSlot().syncHandler(syncHandler).size(SLOT_SIZE);
            widget.tooltip(tooltip -> tooltip.addLine(IKey.lang(LANG_KEY + ".slot." + slot.getSerializedName())));
            if (!changeable.contains(slot)) {
                widget.overlay(IKey.str("X").style(TextFormatting.RED).asIcon().size(SLOT_SIZE)
                        .alignment(Alignment.Center));
            }
            return widget;
        }

        private ItemDisplayWidget gunDisplay() {
            return new ItemDisplayWidget().size(SLOT_SIZE).item(gun).displayAmount(false)
                    .background(SusyGuiTextures.SLOT);
        }

        private IWidget spacer() {
            return new Widget<>().size(SLOT_SIZE);
        }

        private Flow statsColumn() {
            Flow column = new Column().coverChildren().crossAxisAlignment(Alignment.CrossAxis.START).childPadding(2);
            column.child(IKey.lang(LANG_KEY + ".stats").asWidget());
            column.child(statRow("damage", s -> format(s.getDamage())));
            column.child(statRow("spread", s -> format(s.getSpread())));
            column.child(statRow("recoil", s -> format(s.getRecoil())));
            column.child(statRow("aim_time", s -> format(s.getAimTime())));
            column.child(statRow("zoom", s -> format(s.getZoom())));
            column.child(statRow("magazine", s -> Integer.toString(s.getMagazineSize())));
            return column;
        }

        private Flow statRow(String stat, Function<AttachmentStats, String> value) {
            StringSyncValue syncValue = new StringSyncValue(() -> value.apply(stats.get()));
            syncManager.syncValue(STAT_SYNC_KEY + stat, syncValue);
            return new Row().coverChildren().childPadding(4)
                    .child(IKey.dynamic(() -> I18n.format(LANG_KEY + ".stat." + stat, syncValue.getStringValue()))
                            .asWidget());
        }

        private static String format(double value) {
            return String.format(Locale.ROOT, "%.2f", value);
        }
    }

    private static final class SlotInventory implements IItemHandlerModifiable {

        private final ItemStack gun;
        private final Set<AttachmentSlot> changeable;

        private SlotInventory(ItemStack gun, Set<AttachmentSlot> changeable) {
            this.gun = gun;
            this.changeable = changeable;
        }

        @Override
        public int getSlots() {
            return AttachmentSlot.values().length;
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            ItemStack mounted = SuSyAttachmentData.getAttachment(gun, slotAt(slot));
            return mounted == null ? ItemStack.EMPTY : mounted;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || !isItemValid(slot, stack) || !getStackInSlot(slot).isEmpty()) {
                return stack;
            }
            if (!simulate) {
                SuSyAttachmentData.putAttachment(gun, slotAt(slot), stack);
            }
            ItemStack remaining = stack.copy();
            remaining.shrink(1);
            return remaining;
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (amount <= 0 || !changeable.contains(slotAt(slot))) return ItemStack.EMPTY;
            ItemStack mounted = getStackInSlot(slot);
            if (mounted.isEmpty()) return ItemStack.EMPTY;
            if (!simulate) {
                SuSyAttachmentData.removeAttachment(gun, slotAt(slot));
            }
            return mounted;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            return changeable.contains(slotAt(slot)) && accepts(slotAt(slot), stack);
        }

        @Override
        public void setStackInSlot(int slot, @NotNull ItemStack stack) {
            AttachmentSlot attachmentSlot = slotAt(slot);
            if (!changeable.contains(attachmentSlot)) return;
            if (stack.isEmpty()) {
                SuSyAttachmentData.removeAttachment(gun, attachmentSlot);
            } else if (accepts(attachmentSlot, stack)) {
                SuSyAttachmentData.putAttachment(gun, attachmentSlot, stack);
            }
        }

        private static boolean accepts(AttachmentSlot slot, ItemStack stack) {
            if (stack.isEmpty()) return false;
            SuSyAttachmentBehaviour behavior = SuSyAttachmentBehaviour.getAttachmentBehaviour(stack);
            return behavior != null && behavior.getSlot() == slot;
        }

        private static AttachmentSlot slotAt(int index) {
            return AttachmentSlot.values()[index];
        }
    }
}
