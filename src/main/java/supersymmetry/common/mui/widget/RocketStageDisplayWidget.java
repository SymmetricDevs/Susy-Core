package supersymmetry.common.mui.widget;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.resources.I18n;
import net.minecraft.network.PacketBuffer;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.DynamicLabelWidget;
import gregtech.api.gui.widgets.SlotWidget;
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
    public interface SlotListProvider {

        List<DataStorageLoader> get(RocketStage stage, String componentType);
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
    public final SlotListProvider slotListProvider;

    public int selectedStageIndex = 0;
    public int previousSelectedStageIndex = 0;

    public ClickButtonWidget previousButton;
    public ClickButtonWidget nextButton;
    public DynamicLabelWidget amountTextField;
    public DynamicLabelWidget stageName;

    public Map<String, StageContainerWidget> stageContainers = new HashMap<>();

    public RocketStageDisplayWidget(
                                    Position pos,
                                    Size size,
                                    BlueprintProvider blueprintProvider,
                                    SlotListProvider slotListProvider) {
        super(pos, size);
        this.blueprintProvider = blueprintProvider;
        this.slotListProvider = slotListProvider;

        previousButton = new ClickButtonWidget(
                0,
                0,
                10,
                10,
                "",
                (data) -> {
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

        amountTextField = new DynamicLabelWidget(
                (int) ((size.width / 5) * 2.5),
                -1,
                () -> {
                    AbstractRocketBlueprint bp = blueprintProvider.get();
                    if (bp == null || bp.getStages().isEmpty()) {
                        return "0/0";
                    }
                    return (selectedStageIndex + 1) + "/" + bp.getStages().size();
                });

        stageName = new DynamicLabelWidget(
                2,
                12,
                () -> {
                    AbstractRocketBlueprint bp = blueprintProvider.get();
                    if (bp == null || bp.getStages().isEmpty()) {
                        return "";
                    }
                    RocketStage stage = getSelectedStage();
                    if (stage == null) {
                        return "";
                    }
                    return I18n.format(
                            "susy.machine.blueprint_assembler.stagename",
                            I18n.format(stage.getLocalizationKey()));
                },
                0xffffff);

        this.addWidget(amountTextField);
        this.addWidget(nextButton);
        this.addWidget(previousButton);
        this.addWidget(stageName);
    }

    @Override
    public void setVisible(boolean v) {
        super.setVisible(v);
        this.amountTextField.setVisible(v);
        this.nextButton.setVisible(v);
        this.previousButton.setVisible(v);
        this.stageName.setVisible(v);
    }

    @Override
    public void setActive(boolean a) {
        super.setActive(a);
        this.amountTextField.setActive(a);
        this.nextButton.setActive(a);
        this.previousButton.setActive(a);
        this.stageName.setActive(a);
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
                    new Position(0, 15),
                    new Size(this.getSize().width, this.getSize().height - 15),
                    stage,
                    slotListProvider);

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

        public Map<String, ComponentEntryWidget> components = new HashMap<>();
        public int rowSkip = 0;

        public static final int ROW_SEPARATION = 18;

        public StageContainerWidget(
                                    Position pos, Size size, RocketStage stage, SlotListProvider slotListProvider) {
            super(pos, size);

            for (Map.Entry<String, int[]> componentLimits : stage.getComponentLimits().entrySet()) {
                String componentType = componentLimits.getKey();
                int maxSlotCount = stage.maxComponentsOf(componentType);

                List<DataStorageLoader> slots = slotListProvider.get(stage, componentType);

                HorizontalScrollableListWidget slotsw = new HorizontalScrollableListWidget(0, 0, 18 * 5, 28);

                for (int i = 0; i < maxSlotCount; i++) {
                    DataStorageLoader slot = slots.get(i);
                    // SlotWidget s = new SlotWidget(slot, 0, 0, 0);
                    SlotWidget s = new ComponentCardSlotWidget(slot, 0, 0, 0);
                    s.setBackgroundTexture(GuiTextures.SLOT_DARK);
                    // s.setClearSlotOnRightClick(true);
                    slotsw.addWidget(s);
                }

                slotsw.setSliderActive(slotsw.widgets.size() > 5);

                ComponentEntryWidget entry = new ComponentEntryWidget(
                        new Position(0, 0), new Size(18 * 5, 28), slotsw, componentLimits.getValue());

                addSlotList(componentType, "susy.rocketry.components." + componentType + ".name", entry);
            }
        }

        public void addSlotList(String entryName, String localizationKey, ComponentEntryWidget entry) {
            int scrollbarPadding = entry.itemList.sliderActive ? HorizontalScrollableListWidget.scrollPaneWidth : 0;

            String text = I18n.format(localizationKey);
            int textWidth = net.minecraft.client.Minecraft.getMinecraft().fontRenderer.getStringWidth(text);
            int xPos = this.getSize().width - textWidth - 2;

            DynamicLabelWidget textWidget = new DynamicLabelWidget(
                    xPos,
                    rowSkip + ROW_SEPARATION + scrollbarPadding,
                    () -> text,
                    0xffffff);

            this.addWidget(textWidget);

            entry.setSelfPosition(new Position(9, rowSkip + ROW_SEPARATION + scrollbarPadding));
            entry.setSize(new Size(90, 28));
            this.addWidget(entry);

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

            // public void setSelectedIndex(int index) {
            // this.selectedIndex = ((index % validValues.length) + validValues.length) % validValues.length;
            // }
        }

        public WidgetIntSelector selector;
        public boolean shortView = false;
        public HorizontalScrollableListWidget itemList;
        public final ClickButtonWidget shortViewButton;

        public Size previousStateSize = new Size(18 * 5 + 2, 28);
        public boolean previousSliderState = false;

        public ComponentEntryWidget(
                                    Position pos, Size size, HorizontalScrollableListWidget itemList,
                                    int[] validValues) {
            super(pos, size);
            this.itemList = itemList;

            shortViewButton = new ClickButtonWidget(
                    itemList.getSize().width + 10,
                    0,
                    12,
                    12,
                    "",
                    (data) -> {
                        setShortView(!shortView);
                    })
                            .setShouldClientCallback(true)
                            .setButtonTexture(SusyGuiTextures.BLUEPRINT_ASSEMBLER_BUTTON_SHORTVIEW);

            selector = new WidgetIntSelector(
                    validValues,
                    new Position(itemList.getSize().width - 25, 0),
                    new Size((int) (itemList.getSize().width / 2 - 10), 18));

            selector.setVisible(false);
            selector.setActive(false);

            this.addWidget(itemList);
            this.addWidget(shortViewButton);
            this.addWidget(selector);
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

        public void setShortView(boolean state) {
            if (this.shortView == state) return;

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

            this.shortView = state;
            selector.setActive(state);
            selector.setVisible(state);
        }
    }
}
