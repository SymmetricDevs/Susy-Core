package supersymmetry.common.mui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.*;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.rocketry.rockets.RocketStage;
import supersymmetry.api.util.DataStorageLoader;

public class RocketStageDisplayWidget extends AbstractWidgetGroup {

    @FunctionalInterface
    public interface BlueprintProvider {

        AbstractRocketBlueprint get();
    }

    @FunctionalInterface
    public interface RowStateProvider {

        BlueprintRowState get(RocketStage stage, String componentType);
    }

    @FunctionalInterface
    public interface StageIndexCallback {

        void onStageChange(int newIndex);
    }

    @FunctionalInterface
    public interface BlueprintActionCallback {

        void onBlueprintAction(int actionType, PacketBuffer data);
    }

    public final BlueprintProvider blueprintProvider;
    public final RowStateProvider rowStateProvider;
    public final Runnable markDirty;

    public int selectedStageIndex = 0;
    public int previousSelectedStageIndex = 0;

    public ClickButtonWidget previousButton;
    public ClickButtonWidget nextButton;
    public DynamicLabelWidget selectedStageText;

    public Map<String, StageContainerWidget> stageContainers = new TreeMap<>();

    public RocketStageDisplayWidget(
                                    Position pos,
                                    Size size,
                                    BlueprintProvider blueprintProvider,
                                    RowStateProvider rowStateProvider,
                                    Runnable markDirty) {
        super(pos, size);
        this.blueprintProvider = blueprintProvider;
        this.rowStateProvider = rowStateProvider;
        this.markDirty = markDirty;

        previousButton = new ClickButtonWidget(
                0,
                0,
                10,
                10,
                "",
                (d) -> {
                    AbstractRocketBlueprint bp = blueprintProvider.get();
                    if (bp != null && !bp.getStages().isEmpty()) {
                        selectedStageIndex = (selectedStageIndex - 1 + bp.getStages().size()) % bp.getStages().size();
                        updateStageVisibility();
                    }
                })
                        .setShouldClientCallback(true)
                        .setButtonTexture(SusyGuiTextures.BLUEPRINT_ASSEMBLER_BUTTON_LEFT);

        nextButton = new ClickButtonWidget(
                (size.width - 20),
                0,
                10,
                10,
                "",
                (data) -> {
                    AbstractRocketBlueprint bp = blueprintProvider.get();
                    if (bp != null && !bp.getStages().isEmpty()) {
                        selectedStageIndex = (selectedStageIndex + 1) % bp.getStages().size();
                        updateStageVisibility();
                    }
                })
                        .setShouldClientCallback(true)
                        .setButtonTexture(SusyGuiTextures.BLUEPRINT_ASSEMBLER_BUTTON_RIGHT);

        selectedStageText = new DynamicLabelWidget(
                (int) ((size.width / 5) * 1.5),
                -1,
                () -> {
                    AbstractRocketBlueprint bp = blueprintProvider.get();
                    if (bp == null || bp.getStages().isEmpty()) {
                        return "0/0";
                    }

                    RocketStage stage = getSelectedStage();
                    if (stage == null) {
                        return "0/" + bp.getStages().size();
                    }
                    return I18n.format(
                            "susy.machine.blueprint_assembler.stagename",
                            (selectedStageIndex + 1) + "/" + bp.getStages().size(),
                            I18n.format(stage.getLocalizationKey()));
                });



        this.addWidget(selectedStageText);
        this.addWidget(nextButton);
        this.addWidget(previousButton);
    }

    @Override
    public void setVisible(boolean v) {
        super.setVisible(v);
        this.selectedStageText.setVisible(v);
        this.nextButton.setVisible(v);
        this.previousButton.setVisible(v);
    }

    @Override
    public void setActive(boolean a) {
        super.setActive(a);
        this.selectedStageText.setActive(a);
        this.nextButton.setActive(a);
        this.previousButton.setActive(a);
    }

    public int getSelectedIndex() {
        AbstractRocketBlueprint bp = blueprintProvider.get();
        if (bp == null || bp.getStages().isEmpty()) {
            return 0;
        }
        return selectedStageIndex % bp.getStages().size();
    }

    public RocketStage getSelectedStage() {
        AbstractRocketBlueprint bp = blueprintProvider.get();
        if (bp == null || bp.getStages().isEmpty()) {
            return null;
        }
        List<RocketStage> stages = bp.getStages();
        int idx = ((selectedStageIndex % stages.size()) + stages.size()) % stages.size();
        return stages.get(idx);
    }

    public void updateStageVisibility() {
        AbstractRocketBlueprint bp = blueprintProvider.get();
        if (bp == null) {
            return;
        }

        RocketStage selectedStage = getSelectedStage();
        if (selectedStage == null) {
            return;
        }

        for (Map.Entry<String, StageContainerWidget> entry : stageContainers.entrySet()) {
            boolean isSelected = entry.getKey().equals(selectedStage.getName());
            entry.getValue().setPrimary(isSelected);
        }
    }

    public boolean containersBuilt = false;

    public void buildContainers() {
        if (containersBuilt) {
            return;
        }

        for (StageContainerWidget container : stageContainers.values()) {
            this.removeWidget(container);
        }
        stageContainers.clear();

        AbstractRocketBlueprint bp = blueprintProvider.get();
        if (bp == null || bp.getStages().isEmpty()) {
            return;
        }

        for (RocketStage stage : bp.getStages()) {
            StageContainerWidget stageView = new StageContainerWidget(
                    new Position(0, 0),
                    new Size(this.getSize().width, this.getSize().height - 15),
                    stage,
                    rowStateProvider,
                    markDirty);

            stageContainers.put(stage.getName(), stageView);
            this.addWidget(stageView);
        }

        containersBuilt = true;
        updateStageVisibility();
    }

    public void rebuildContainers() {
        containersBuilt = false;
        buildContainers();
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        AbstractRocketBlueprint bp = blueprintProvider.get();
        boolean hasBlueprint = bp != null && !bp.getStages().isEmpty();

        if (hasBlueprint && selectedStageIndex != previousSelectedStageIndex) {
            writeUpdateInfo(2, buffer -> buffer.writeVarInt(selectedStageIndex));
            previousSelectedStageIndex = selectedStageIndex;
        }
    }

    public void notifyBlueprintChanged() {
        rebuildContainers();
        selectedStageIndex = 0;
        previousSelectedStageIndex = 0;
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 2) {
            selectedStageIndex = buffer.readVarInt();
            updateStageVisibility();
        }
    }

    public void setSelectedStageIndex(int index) {
        this.selectedStageIndex = index;
        this.previousSelectedStageIndex = index;
        updateStageVisibility();
    }

    public static class StageContainerWidget extends AbstractWidgetGroup {

        public Map<String, ComponentEntryWidget> components = new TreeMap<>();
        public int rowSkip = 0;

        public static final int ROW_SEPARATION = 18;

        private static class RowLayoutEntry {

            final DynamicLabelWidget label;
            final ComponentEntryWidget entry;
            final int labelX;

            RowLayoutEntry(DynamicLabelWidget label, ComponentEntryWidget entry, int labelX) {
                this.label = label;
                this.entry = entry;
                this.labelX = labelX;
            }
        }

        private final List<RowLayoutEntry> rowEntries = new ArrayList<>();

        public StageContainerWidget(
                                    Position pos, Size size, RocketStage stage, RowStateProvider rowStateProvider,
                                    Runnable markDirty) {
            super(pos, size);

            for (Map.Entry<String, int[]> componentLimits : stage.getComponentLimits().entrySet()) {
                String componentType = componentLimits.getKey();
                int maxSlotCount = stage.maxComponentsOf(componentType);

                BlueprintRowState rowState = rowStateProvider.get(stage, componentType);

                HorizontalScrollableListWidget slotsw = new HorizontalScrollableListWidget(0, 0, 18 * 5, 28);

                if (rowState != null) {
                    for (int i = 0; i < maxSlotCount; i++) {
                        DataStorageLoader slot = rowState.slots.get(i);
                        SlotWidget s = new ComponentCardSlotWidget(slot, 0, 0, 0);
                        s.setBackgroundTexture(GuiTextures.SLOT_DARK);
                        slotsw.addWidget(s);
                    }
                }

                slotsw.setSliderActive(slotsw.widgets.size() > 5);

                //dial button
                ComponentEntryWidget entry = new ComponentEntryWidget(
                        new Position(0, 0), new Size(18 * 5, 28), slotsw, rowState, markDirty);

                addSlotList(componentType, "susy.rocketry.components." + componentType + ".name", entry);
            }
        }

        public void addSlotList(String entryName, String localizationKey, ComponentEntryWidget entry) {
            // Use the natural (non-short-view) slider state so rows initialized with shortView=true
            // still reserve the right amount of vertical space for their scrollbar.
            boolean naturalSlider = entry.shortView ? entry.previousSliderState : entry.itemList.sliderActive;
            int scrollbarPadding = naturalSlider ? HorizontalScrollableListWidget.scrollPaneWidth : 0;

            entry.setSelfPosition(new Position(0, rowSkip + ROW_SEPARATION + scrollbarPadding));
            entry.setSize(new Size(90, 28));
            this.addWidget(entry);

            //part name
            String text = I18n.format(localizationKey);
            int textWidth = net.minecraft.client.Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
            int xPos = this.getSize().width - textWidth - 10;

            DynamicLabelWidget textWidget = new DynamicLabelWidget(
                    xPos,
                    rowSkip + ROW_SEPARATION + scrollbarPadding,
                    () -> text,
                    0xffffff);

            this.addWidget(textWidget);

            rowEntries.add(new RowLayoutEntry(textWidget, entry, xPos));

            components.put(entryName, entry);
            rowSkip += ROW_SEPARATION + scrollbarPadding;
        }

        public void setPrimary(boolean active) {
            this.setActive(active);
            this.setVisible(active);

            for (ComponentEntryWidget entry : components.values()) {
                entry.setActive(active);
                entry.setVisible(active);
            }
        }
    }

    public static class ComponentEntryWidget extends AbstractWidgetGroup {

        public class WidgetIntSelector extends AbstractWidgetGroup {

            public final ClickButtonWidget decreaseButton;
            public final ClickButtonWidget increaseButton;
            public final DynamicLabelWidget amountTextField;
            public final int[] validValues;
            public int selectedIndex = 0;

            public WidgetIntSelector(int[] validValues, Position position, Size size) {
                super(position, size);
                this.validValues = validValues;

                decreaseButton = new ClickButtonWidget(
                        0,
                        0,
                        size.width / 5,
                        size.height,
                        "",
                        (data) -> {
                            selectedIndex = (selectedIndex - 1 + validValues.length) % validValues.length;
                            writeClientAction(3, buf -> buf.writeVarInt(selectedIndex));
                        })
                                .setShouldClientCallback(true)
                                .setButtonTexture(SusyGuiTextures.BLUEPRINT_ASSEMBLER_BUTTON_LEFT);

                increaseButton = new ClickButtonWidget(
                        (size.width / 5) * 4,
                        0,
                        size.width / 5,
                        size.height,
                        "",
                        (data) -> {
                            selectedIndex = (selectedIndex + 1) % validValues.length;
                            writeClientAction(3, buf -> buf.writeVarInt(selectedIndex));
                        })
                                .setShouldClientCallback(true)
                                .setButtonTexture(SusyGuiTextures.BLUEPRINT_ASSEMBLER_BUTTON_RIGHT);

                amountTextField = new DynamicLabelWidget(
                        (int) ((size.width / 5) * 1.5),
                        3,
                        () -> Integer.toString(getSelectedValue()) + "x",
                        0xffffff);

                this.addWidget(amountTextField);
                this.addWidget(increaseButton);
                this.addWidget(decreaseButton);

                decreaseButton.setVisible(validValues.length > 1);
                decreaseButton.setActive(validValues.length > 1);
                increaseButton.setVisible(validValues.length > 1);
                increaseButton.setActive(validValues.length > 1);
            }

            public int getSelectedValue() {
                return validValues[selectedIndex];
            }

            @Override
            public void handleClientAction(int id, PacketBuffer buffer) {
                super.handleClientAction(id, buffer);
                if (id == 3) {
                    selectedIndex = buffer.readVarInt();
                    if (boundRow != null) {
                        boundRow.multiplierIndex = selectedIndex;
                        markDirty.run();
                    }
                }
            }
        }

        public WidgetIntSelector selector;
        public boolean shortView = false;
        public HorizontalScrollableListWidget itemList;
        public final Widget shortViewButton;
        public final BlueprintRowState boundRow;
        public final Runnable markDirty;

        public Size previousStateSize = new Size(18 * 5 + 2, 28);
        public boolean previousSliderState = false;

        public ComponentEntryWidget(
                                    Position pos, Size size, HorizontalScrollableListWidget itemList,
                                    BlueprintRowState boundRow, Runnable markDirty) {
            super(pos, size);
            this.itemList = itemList;
            this.boundRow = boundRow;
            this.markDirty = markDirty;

            int[] validValues = (boundRow != null) ? boundRow.validMultiplierValues : new int[] { 1 };

            shortViewButton = new ToggleButtonWidget(
                    itemList.getSize().width + 10,
                    0,
                    16,
                    16,
                    this::isShortView,
                    (isShort) -> {
                        setShortView(isShort);
                    }) {

                @Override
                @SideOnly(Side.CLIENT)
                public boolean mouseClicked(int mouseX, int mouseY, int button) {
                    if (super.mouseClicked(mouseX, mouseY, button)) {
                        setShortView(this.isPressed);
                        return true;
                    }
                    return false;
                }
            }
                    .setButtonTexture(SusyGuiTextures.BLUEPRINT_ASSEMBLER_BUTTON_SHORTVIEW)
                    .setTooltipText("susy.gui.toggle_short_view");

            selector = new WidgetIntSelector(
                    validValues,
                    new Position(itemList.getSize().width - 25, 0),
                    new Size(itemList.getSize().width / 2 - 10, 18));

            selector.setVisible(false);
            selector.setActive(false);

            this.addWidget(itemList);
            this.addWidget(shortViewButton);
            this.addWidget(selector);

            if (boundRow != null) {
                selector.selectedIndex = Math.min(boundRow.multiplierIndex, validValues.length - 1);
                if (boundRow.shortView) {
                    applyShortViewSize(true);
                    this.shortView = true;
                    selector.setActive(true);
                    selector.setVisible(true);
                }
            }
        }

        @Override
        public void setActive(boolean active) {
            this.selector.setActive(active);
            this.itemList.widgets.forEach(x -> x.setActive(active));
            this.itemList.setActive(active);
        }

        public int getAmount() {
            return selector.getSelectedValue();
        }

        public boolean isShortView() {
            return shortView;
        }

        private void applyShortViewSize(boolean state) {
            if (state) {
                previousStateSize = itemList.getSize();
                previousSliderState = itemList.sliderActive;
                itemList.setSize(new Size(18, 18));
                itemList.setSliderActive(false);
                itemList.setSliderOffset(0f);
            } else {
                itemList.setSize(previousStateSize);
                itemList.setSliderActive(previousSliderState);
            }
        }

        public void setShortView(boolean state) {
            if (this.shortView == state) return;

            applyShortViewSize(state);

            this.shortView = state;
            if (boundRow != null) {
                boundRow.shortView = state;
                markDirty.run();
            }
            selector.setActive(state);
            selector.setVisible(state);
            writeClientAction(4, buf -> buf.writeBoolean(state));
        }

        @Override
        public void handleClientAction(int id, PacketBuffer buffer) {
            super.handleClientAction(id, buffer);
            if (id == 4) {
                boolean state = buffer.readBoolean();
                if (this.shortView != state) {
                    applyShortViewSize(state);
                    this.shortView = state;
                    if (boundRow != null) {
                        boundRow.shortView = state;
                        markDirty.run();
                    }
                    selector.setActive(state);
                    selector.setVisible(state);
                }
            }
        }
    }
}
