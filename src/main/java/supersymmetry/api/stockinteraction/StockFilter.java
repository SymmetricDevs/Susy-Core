package supersymmetry.api.stockinteraction;

import cam72cam.immersiverailroading.entity.EntityRollingStock;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.drawable.UITexture;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.RichTooltip;
import com.cleanroommc.modularui.utils.Color;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import com.cleanroommc.modularui.value.sync.StringSyncValue;
import com.cleanroommc.modularui.value.sync.SyncHandlers;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widgets.ItemSlot;
import com.cleanroommc.modularui.widgets.SlotGroupWidget;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.slot.SlotGroup;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.metatileentity.Mui2MetaTileEntity;
import supersymmetry.common.mui.widget.HighlightedTextField;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class StockFilter implements INBTSerializable<NBTTagCompound>, Predicate<EntityRollingStock> {

    protected final int size;
    protected StockFilterReader handler;
    protected Map<Integer, String> definitions;
    protected String patternString;
    protected Pattern pattern;
    protected boolean errored = false;

    public StockFilter(int size) {
        this.size = size;
        this.handler = new StockFilterReader(this.size);
        this.definitions = new HashMap<>();
        this.patternString = "";
    }

    @Override
    public boolean test(EntityRollingStock entityRollingStock) {
        String name = entityRollingStock.getDefinition().name();
        return definitions.containsValue(name) && matchesName(entityRollingStock.tag);
    }

    protected boolean matchesName(String tag) {
        return errored || patternString.isEmpty() || pattern == null
                || pattern.asPredicate().test(tag);
    }

    @Override
    public NBTTagCompound serializeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setString("patternString", patternString);
        nbt.setBoolean("errored", errored);
        NBTTagCompound handlerNbt = handler.serializeNBT();
        nbt.setTag("handler", handlerNbt);
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt) {
        patternString = nbt.getString("patternString");
        errored = nbt.getBoolean("errored");
        handler.deserializeNBT(nbt.getCompoundTag("handler"));
        refreshAllDefinitions();
        this.pattern = null;
        this.errored = !compilePattern();
    }

    public void refreshAllDefinitions() {
        definitions.clear();
        for (int i = 0; i < handler.getSlots(); i++) {
            refreshDefinitionFrom(i);
        }
    }

    public void refreshDefinitionFrom(int slot) {
        ItemStack stack = handler.getStackInSlot(slot);
        if (stack.isEmpty()) {
            definitions.remove(slot);
        } else {
            String name = StockHelperFunctions.getDefinitionNameFromStack(stack);
            if (name != null) {
                definitions.put(slot, name);
            }
        }
    }

    public List<EntityRollingStock> filterEntities(List<EntityRollingStock> entities) {
        return entities.stream()
                .filter(this::shouldIncludeEntity)
                .collect(Collectors.toList());
    }

    private boolean shouldIncludeEntity(EntityRollingStock entity) {
        return test(entity);
    }

    @SuppressWarnings("deprecation")
    @NotNull
    public ModularPanel createPopupPanel(PanelSyncManager syncManager) { // TODO: loc
        return Mui2MetaTileEntity.createPopupPanel("simple_stock_filter", 86, 101).padding(4)
                .child(IKey.str("Stock Filter").asWidget().pos(5, 5))
                .child(createWidgets(syncManager).top(22));
    }

    @NotNull
    public Widget<?> createWidgets(PanelSyncManager syncManager) {

        SlotGroup filterInventory = new SlotGroup("filter_inv", 3, 1000, true);
        syncManager.registerSlotGroup(filterInventory);

        StringSyncValue patternString = new StringSyncValue(() -> this.patternString, val -> {
            this.patternString = val;
            this.errored = !compilePattern();
        });
//        syncManager.syncValue("patternString", patternString);

        return Flow.column().coverChildrenHeight()
                .child(SlotGroupWidget.builder()
                        .matrix("XXX",
                                "XXX",
                                "XXX")
                        .key('X', index -> new ItemSlot()
                                .tooltip(tooltip -> {
                                    tooltip.setAutoUpdate(true);
                                    tooltip.textColor(Color.GREY.main);
                                })
                                .slot(SyncHandlers.phantomItemSlot(this.handler, index)
                                        .slotGroup(filterInventory)
                                        .filter(stack -> StockHelperFunctions.getDefinitionNameFromStack(stack) != null)))
                        .build().marginRight(4))
                .child(Flow.row().coverChildrenHeight().coverChildrenWidth()
                        .child(Flow.column().height(18)
                                .coverChildrenWidth().marginRight(2)
                                .child(SusyGuiTextures.OREDICT_INFO.asWidget()
                                        .size(8).top(0)
                                        .addTooltipLine(IKey.lang("stock_filter.info")))
                                .child(new Widget<>()
                                        .size(8).bottom(0)
                                        .onUpdateListener(this::getStatusIcon)
                                        .tooltipBuilder(this::createStatusTooltip)
                                        .tooltip(tooltip -> tooltip.setAutoUpdate(true))))
                        .child(new HighlightedTextField()
                                .onUnfocus(() -> this.errored = !compilePattern())
                                .setHighlightRule(this::highlightRule)
                                .setTextColor(Color.WHITE.darker(1))
                                .value(patternString).marginBottom(4)
                                .height(18).width(44)));
    }

    protected String highlightRule(String text) {
        StringBuilder builder = new StringBuilder(text);
        for (int i = 0; i < builder.length(); i++) {
            switch (builder.charAt(i)) {
                case '|', '&', '^', '(', ')' -> {
                    builder.insert(i, TextFormatting.GOLD);
                    i += 2;
                }
                case '*', '?' -> {
                    builder.insert(i, TextFormatting.GREEN);
                    i += 2;
                }
                case '!' -> {
                    builder.insert(i, TextFormatting.RED);
                    i += 2;
                }
                case '\\' -> {
                    builder.insert(i++, TextFormatting.YELLOW);
                    i += 2;
                }
                case '$' -> { // TODO: remove this switch case in 2.9
                    builder.insert(i, TextFormatting.DARK_GREEN);
                    for (; i < builder.length(); i++) {
                        switch (builder.charAt(i)) {
                            case ' ', '\t', '\n', '\r' -> {
                            }
                            case '\\' -> {
                                i++;
                                continue;
                            }
                            default -> {
                                continue;
                            }
                        }
                        break;
                    }
                }
                default -> {
                    continue;
                }
            }
            builder.insert(i + 1, TextFormatting.RESET);
        }
        return builder.toString();
    }

    protected boolean compilePattern() {
        this.pattern = null;
        try {
            this.pattern = Pattern.compile(this.patternString);
        } catch (PatternSyntaxException e) {
            return false;
        }
        return true;
    }

    protected void getStatusIcon(Widget<?> widget) {
        UITexture texture;
        if (this.patternString.isEmpty()) {
            texture = SusyGuiTextures.OREDICT_WAITING;
        } else if (errored) {
            texture = SusyGuiTextures.OREDICT_ERROR;
        } else {
            texture = SusyGuiTextures.OREDICT_SUCCESS;
        }
        widget.background(texture);
    }

    protected void createStatusTooltip(RichTooltip tooltip) {

        if (!this.patternString.isEmpty()) {
            String text;
            if (errored) {
                tooltip.add(I18n.format("stock_filter.status.err"));
            } else {
                tooltip.add(I18n.format("stock_filter.status.success"));
            }
        }
    }

    public class StockFilterReader extends ItemStackHandler {

        public StockFilterReader(int size) {
            super(size);
        }

        @Override
        public void setStackInSlot(int slot, @Nonnull ItemStack stack) {
            validateSlotIndex(slot);
            if (!stack.isEmpty()) {
//                if (StockHelperFunctions.getDefinitionNameFromStack(stack) == null) return;
                stack.setCount(Math.min(stack.getCount(), 1)); // TODO: check this
            }
            this.stacks.set(slot, stack);
            onContentsChanged(slot);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            refreshDefinitionFrom(slot);
        }
    }
}
