package supersymmetry.common.mui.widget;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;

import org.jetbrains.annotations.NotNull;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.DynamicLabelWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import supersymmetry.api.SusyLog;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.rocketry.rockets.RocketStage;
import supersymmetry.api.rocketry.rockets.RocketStage.ComponentValidationResult;
import supersymmetry.api.util.DataStorageLoader;
import supersymmetry.common.item.SuSyMetaItems;

public class RocketStageDisplayWidget extends AbstractWidgetGroup {

    @FunctionalInterface
    public static interface slotProvider {

        public List<DataStorageLoader> getItemFor(RocketStage stage, String compname);
    }

    public List<RocketStage> stages = new ArrayList<>();
    protected ClickButtonWidget previousButton;
    protected ClickButtonWidget nextButton;
    protected DynamicLabelWidget amountTextField;
    protected DynamicLabelWidget stageName;
    protected slotProvider provider;
    public String errorStage = "";
    public String errorComponentType = "";
    public Consumer<RocketStageDisplayWidget> removalAction;
    public Consumer<RocketStageDisplayWidget> insertionAction;
    public RocketStage.ComponentValidationResult error = ComponentValidationResult.UNKNOWN;
    public int selectedStageIndex = 0;
    // a hashmap of ui elements, this is a bad idea, but its either that or a hashmap of hashmaps of
    // booleans for the button
    protected Map<String, RocketSimulatorComponentContainerWidget> stageContainers = new HashMap<>();

    public RocketStageDisplayWidget(Position pos, Size size, slotProvider slotProvider) {
        super(pos, size);
        this.provider = slotProvider;

        previousButton = new ClickButtonWidget(
                0,
                0,
                10,
                10,
                "",
                (clickdata) -> {
                    selectedStageIndex = (selectedStageIndex == 0) ? stages.size() : selectedStageIndex - 1;
                    this.updateSelectedStageView();
                })
                        .setShouldClientCallback(true)
                        .setButtonTexture(SusyGuiTextures.SPACEFLIGHT_SIMULATOR_BUTTON_LEFT);
        nextButton = new ClickButtonWidget(
                (size.width - 20),
                0,
                10,
                10,
                "",
                (clickdata) -> {
                    this.selectedStageIndex++;
                    this.updateSelectedStageView();
                })
                        .setShouldClientCallback(true)
                        .setButtonTexture(SusyGuiTextures.SPACEFLIGHT_SIMULATOR_BUTTON_RIGHT);
        amountTextField = new DynamicLabelWidget(
                (int) ((size.width / 5) * 2.5),
                -1,
                () -> {
                    return Integer.toString(this.getSelectedIndex() + 1) + "/" + this.stages.size();
                });
        stageName = new DynamicLabelWidget(
                2,
                12,
                () -> {
                    if (this.stages.isEmpty()) return "";
                    return I18n.format(
                            "susy.machine.aerospace_flight_simulator.stagename",
                            I18n.format(this.getSelectedStage().getLocalizationKey()));
                },
                0xffffff);

        this.addWidget(amountTextField);
        this.addWidget(nextButton);
        this.addWidget(previousButton);
        this.addWidget(stageName);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.amountTextField.setVisible(visible);
        this.nextButton.setVisible(visible);
        this.previousButton.setVisible(visible);
    }

    @Override
    public void setActive(boolean active) {
        super.setActive(active);
        this.amountTextField.setActive(active);
        this.nextButton.setActive(active);
        this.previousButton.setActive(active);
    }

    public int getSelectedIndex() {
        return this.stages.isEmpty() ? 0 : selectedStageIndex % this.stages.size();
    }

    public RocketStage getSelectedStage() {
        return this.stages.get(getSelectedIndex());
    }

    public void updateSelectedStageView() {
        this.stageContainers
                .values()
                .forEach(
                        x -> {
                            // i have no clue how but this causes a ConcurrentModificationException
                            x.setPrimary(false);
                        });

        if (!this.stageContainers.containsKey(this.getSelectedStage().getName())) {
            // this would happen if the key was RocketStage and not a String and im not sure why
            return;
        }
        this.stageContainers.get(this.getSelectedStage().getName()).setPrimary(true);
    }

    public void generateSelectedStageView(RocketStage stage) {
        if (!this.stageContainers.containsKey(stage.getName())) {
            RocketSimulatorComponentContainerWidget stageView = new RocketSimulatorComponentContainerWidget(
                    new Position(0, 15), new Size(this.getSize().width, this.getSize().height - 15));
            for (Map.Entry<String, int[]> componentLimits : stage.getComponentLimits().entrySet()) {
                int maxSlotCount = stage.maxComponentsOf(componentLimits.getKey());
                HorizontalScrollableListWidget slots = new HorizontalScrollableListWidget(0, 0, 18 * 5, 28);
                for (int i = 0; i < maxSlotCount; i++) {
                    slots.addWidget(
                            new SlotWidget(
                                    this.provider.getItemFor(stage, componentLimits.getKey()).get(i),
                                    0,
                                    0 /* i forgot where exactly, but the x possition gets set later somewhere */,
                                    0)
                                            .setBackgroundTexture(GuiTextures.SLOT_DARK));
                }

                slots.setSliderActive(slots.widgets.size() > 5);
                RocketComponentEntryWidget entry = new RocketComponentEntryWidget(
                        new Position(0, 0), new Size(18 * 5, 28), slots, componentLimits.getValue());
                stageView.addSlotList(
                        componentLimits.getKey(),
                        "susy.rocketry.components." + componentLimits.getKey() + ".name",
                        entry);
            }
            stageContainers.put(stage.getName(), stageView);
            this.addWidget(stageView);
        }
    }

    public Map<String, Map<String, List<DataStorageLoader>>> generateSlotsFromBlueprint(
                                                                                        AbstractRocketBlueprint bp,
                                                                                        MetaTileEntity mte) {
        Map<String, Map<String, List<DataStorageLoader>>> map = new HashMap<>();
        // copy the array because it explodes if you dont
        for (RocketStage stage : new ArrayList<>(bp.stages)) {

            Map<String, List<DataStorageLoader>> stageComponents = new HashMap<>();
            for (String componentname : new HashSet<>(stage.componentLimits.keySet())) {
                List<DataStorageLoader> slots = new ArrayList<>();
                for (int i = 0; i < stage.maxComponentsOf(componentname); i++) {
                    // final int indx = i;
                    // final String compname = componentname;
                    // final String stagename = stage.getName();
                    slots.add(
                            new DataStorageLoader(
                                    mte,
                                    x -> {
                                        if (SuSyMetaItems.isMetaItem(x) == SuSyMetaItems.DATA_CARD_ACTIVE.metaValue) {
                                            if (x.hasTagCompound()) {
                                                AbstractComponent<?> c = AbstractComponent.getComponentFromName(
                                                        x.getTagCompound().getString("name"));
                                                if (c.getComponentSlotValidator().test(componentname)) {
                                                    // SusyLog.logger.info(
                                                    // "slot {} stage {} component array {}", indx, stagename,
                                                    // compname);
                                                    return true;
                                                }
                                            }
                                        }
                                        return false;
                                    }));
                }
                stageComponents.put(componentname, slots);
            }
            map.put(stage.getName(), stageComponents);
        }
        return map;
    }

    public void generateFromBlueprint(AbstractRocketBlueprint blueprint) {
        this.stages = blueprint.getStages();
        this.selectedStageIndex = 0;
        this.stageContainers.values().forEach(x -> this.removeWidget(x));
        this.stageContainers.clear();
        this.stages.forEach(
                x -> {
                    this.generateSelectedStageView(x);
                });
        this.updateSelectedStageView();
    }

    // this thing is an absolute mess
    // takes in a blueprint, adds the component entries into it with all of the AbstractComponent<?>
    // stuff. taken from the gui slots. this is here because of the shortview buttons
    public boolean blueprintBuildAttempt(AbstractRocketBlueprint blueprint) {
        this.error = ComponentValidationResult.UNKNOWN;
        // go through every stage widget
        for (Entry<String, RocketSimulatorComponentContainerWidget> stageEntry : this.stageContainers.entrySet()) {
            RocketStage stageFrombp;
            Optional<RocketStage> st = blueprint.getStages().stream()
                    .filter(x -> x.getName() == stageEntry.getKey())
                    .findFirst();
            if (!st.isPresent()) {
                SusyLog.logger.error(
                        "blueprint stages: {} actually here: {}",
                        blueprint.getStages().stream().map(x -> x.getName()).collect(Collectors.toList()),
                        this.stageContainers.keySet());
                // for (RocketStage stage : blueprint.getStages()) {
                // for (Entry<String, RocketSimulatorComponentContainerWidget> guiEntry :
                // this.stageContainers
                // .entrySet()) {
                // SusyLog.logger.info(
                // "stage name: \"{}\" guiEntry name: \"{}\" equal??? {}",
                // stage.getName(),
                // guiEntry.getKey(),
                // stage.getName() == guiEntry.getKey());
                // }
                // }
                throw new RuntimeException(
                        String.format(
                                "failed to match a stage to the provided blueprint, %s not in %s",
                                stageEntry.getKey(),
                                blueprint.getStages().stream().map(x -> x.getName()).collect(Collectors.toList())));
            }
            stageFrombp = st.get();

            this.errorStage = stageFrombp.getName();
            // go through every component type within that stage component
            for (Entry<String, RocketComponentEntryWidget> entryWidgets : stageEntry.getValue().components.entrySet()) {
                List<AbstractComponent<?>> components = new ArrayList<>();
                this.errorComponentType = entryWidgets.getKey();

                if (!entryWidgets.getValue().isShortView()) {
                    // go through each slot and add each component separately
                    for (DataStorageLoader componentContainer : entryWidgets.getValue().getSlots()) {
                        @NotNull
                        ItemStack cardStack = componentContainer.getStackInSlot(0);
                        if (!cardStack.hasTagCompound()) {
                            // this.error = ComponentValidationResult.INVALID_CARD;
                            // return false;
                            continue;
                        }
                        NBTTagCompound tag = cardStack.getTagCompound();
                        Optional<? extends AbstractComponent<?>> component = AbstractComponent
                                .getComponentFromName(tag.getString("name")).readFromNBT(tag);
                        if (!component.isPresent()) {
                            // this.error = ComponentValidationResult.INVALID_CARD;
                            // return false;
                            continue;
                        }
                        components.add(component.get());
                    }
                }
                // duplicate the component from the first slot n times since its the same stuff most of the
                // times
                else {
                    @NotNull
                    ItemStack cardStack = entryWidgets.getValue().getSlots().get(0).getStackInSlot(0);
                    if (!cardStack.hasTagCompound()) {
                        this.error = ComponentValidationResult.INVALID_CARD;
                        return false;
                    }
                    NBTTagCompound tag = cardStack.getTagCompound();
                    Optional<? extends AbstractComponent<?>> component = AbstractComponent
                            .getComponentFromName(tag.getString("name")).readFromNBT(tag);
                    if (!component.isPresent()) {
                        this.error = ComponentValidationResult.INVALID_CARD;
                        return false;
                    }
                    for (int i = 0; i < entryWidgets.getValue().getAmount(); i++) {
                        components.add(component.get());
                    }
                }
                // actually set the component type lists with the generated AbstractComponents
                ComponentValidationResult res = stageFrombp.setComponentListEntry(entryWidgets.getKey(), components);
                if (res != ComponentValidationResult.SUCCESS) {
                    this.error = res;
                    return false;
                }
            }
            for (Entry<String, List<AbstractComponent<?>>> componentLists : stageFrombp.getComponents().entrySet()) {
                ComponentValidationResult stat = stageFrombp
                        .getComponentValidationFunction()
                        .apply(
                                new Tuple<String, List<AbstractComponent<?>>>(
                                        componentLists.getKey(), componentLists.getValue()));
                if (stat != ComponentValidationResult.SUCCESS) {
                    this.error = stat;
                    return false;
                }
            }
        }
        this.error = ComponentValidationResult.SUCCESS;
        return true;
    }

    // a very bad way to just call a function when the server needs it
    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 100) {
            this.insertionAction.accept(this);
        } else if (id == 101) {
            this.removalAction.accept(this);
        }
    }

    public void onBlueprintRemoved() {
        this.writeUpdateInfo(101, (buf) -> {});
        this.removalAction.accept(this);
    }

    public void onBlueprintInserted() {
        this.writeUpdateInfo(100, (buf) -> {});
        this.insertionAction.accept(this);
    }

    public String getStatusText() {
        if (this.error == ComponentValidationResult.SUCCESS) {
            return I18n.format(ComponentValidationResult.SUCCESS.getTranslationKey());
        } else {
            if (this.errorComponentType == "" || this.errorStage == ""
            // || this.error == ComponentValidationResult.UNKNOWN
            ) return "";
        }
        return String.format(
                "%s \n%s \n%s",
                I18n.format(this.error.getTranslationKey()),
                I18n.format("susy.rocketry.stages." + this.errorStage + ".name"),
                I18n.format("susy.rocketry.components." + this.errorComponentType + ".name"));
    }
}
