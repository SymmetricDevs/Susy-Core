package supersymmetry.common.mui.widget;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.DynamicLabelWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.rocketry.rockets.RocketStage;
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
  protected slotProvider provider;
  int selectedStageIndex = 0;
  RocketSimulatorComponentContainerWidget currentStageView =
      new RocketSimulatorComponentContainerWidget(
          new Position(0, 15), new Size(this.getSize().width, this.getSize().height - 15));

  public RocketStageDisplayWidget(Position pos, Size size, slotProvider slotProvider) {
    super(pos, size);
    this.provider = slotProvider;

    previousButton =
        new ClickButtonWidget(
                0,
                0,
                10,
                10,
                "",
                (clickdata) -> {
                  selectedStageIndex =
                      (selectedStageIndex == 0) ? stages.size() : selectedStageIndex - 1;
                  this.updateSelectedStageView();
                })
            .setShouldClientCallback(true)
            .setButtonTexture(SusyGuiTextures.SPACEFLIGHT_SIMULATOR_BUTTON_LEFT);
    nextButton =
        new ClickButtonWidget(
                (size.width / 5) * 4,
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
    amountTextField =
        new DynamicLabelWidget(
            (int) ((size.width / 5) * 2.5),
            -1,
            () -> {
              return Integer.toString(this.getSelectedIndex()) + "/" + this.stages.size();
            });

    this.addWidget(amountTextField);
    this.addWidget(nextButton);
    this.addWidget(previousButton);
    this.addWidget(currentStageView);
  }

  public int getSelectedIndex() {
    return this.stages.isEmpty() ? 0 : selectedStageIndex % this.stages.size();
  }

  public RocketStage getSelectedStage() {
    return this.stages.get(getSelectedIndex());
  }

  public void updateSelectedStageView() {
    RocketStage stage = this.getSelectedStage();
    this.currentStageView.RemoveSlotLists();
    for (Map.Entry<String, int[]> componentLimits : stage.getComponentLimits().entrySet()) {
      int maxSlotCount = stage.maxComponentsOf(componentLimits.getKey());
      HorizontalScrollableListWidget slots = new HorizontalScrollableListWidget(0, 0, 18 * 5, 28);
      for (int i = 0; i < maxSlotCount; i++) {
        slots.addWidget(
            new SlotWidget(
                    this.provider.getItemFor(stage, componentLimits.getKey()).get(i),
                    0,
                    0 /*i forgot where exactly, but the x possition gets reset later somewhere */,
                    0)
                .setBackgroundTexture(GuiTextures.SLOT_DARK));
      }

      slots.setSliderActive(slots.widgets.size() > 5);
      RocketComponentEntryWidget entry =
          new RocketComponentEntryWidget(
              new Position(0, 0), new Size(18 * 5, 28), slots, componentLimits.getValue());
      this.currentStageView.addSlotList(
          componentLimits.getKey(),
          "susy.rocketry.components." + componentLimits.getKey() + ".name",
          entry);
    }
  }

  public Map<String, Map<String, List<DataStorageLoader>>> generateSlotsFromBlueprint(
      AbstractRocketBlueprint bp, MetaTileEntity mte) {
    Map<String, Map<String, List<DataStorageLoader>>> map = new HashMap<>();
    for (RocketStage stage : bp.stages) {
      Map<String, List<DataStorageLoader>> stageComponents = new HashMap<>();
      for (String componentname : stage.componentLimits.keySet()) {
        List<DataStorageLoader> slots = new ArrayList<>();
        for (int i = 0; i < stage.maxComponentsOf(componentname); i++) {
          slots.add(
              new DataStorageLoader(
                  mte,
                  x -> {
                    if (SuSyMetaItems.isMetaItem(x) == SuSyMetaItems.DATA_CARD_ACTIVE.metaValue) {
                      if (x.hasTagCompound()) {
                        var c =
                            AbstractComponent.getComponentFromName(
                                x.getTagCompound().getString("name"));
                        if (c.getComponentSlotValidator().test(componentname)) {
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
    this.updateSelectedStageView();
  }
}
