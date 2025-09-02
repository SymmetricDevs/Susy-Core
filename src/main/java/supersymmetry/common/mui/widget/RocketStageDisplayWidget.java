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
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.util.Constants.NBT;
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
  protected slotProvider provider;
  protected String errorStage="";
  protected String errorComponentType="";
  protected RocketStage.ComponentValidationResult error=ComponentValidationResult.UNKNOWN;
  public int selectedStageIndex = 0;
  protected Map<String, RocketSimulatorComponentContainerWidget> stageContainers = new HashMap<>();

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
              x.setActive(false);
              x.setVisible(false);
            });

    if (!this.stageContainers.containsKey(this.getSelectedStage().getName())) {
      // this would happen if the key was RocketStage and not a String and im not sure why
      return;
    }
    this.stageContainers.get(this.getSelectedStage().getName()).setActive(true);
    this.stageContainers.get(this.getSelectedStage().getName()).setVisible(true);
  }

  public void generateSelectedStageView(RocketStage stage) {
    if (!this.stageContainers.containsKey(stage.getName())) {
      RocketSimulatorComponentContainerWidget stageView =
          new RocketSimulatorComponentContainerWidget(
              new Position(0, 15), new Size(this.getSize().width, this.getSize().height - 15));
      for (Map.Entry<String, int[]> componentLimits : stage.getComponentLimits().entrySet()) {
        int maxSlotCount = stage.maxComponentsOf(componentLimits.getKey());
        HorizontalScrollableListWidget slots = new HorizontalScrollableListWidget(0, 0, 18 * 5, 28);
        for (int i = 0; i < maxSlotCount; i++) {
          slots.addWidget(
              new SlotWidget(
                      this.provider.getItemFor(stage, componentLimits.getKey()).get(i),
                      0,
                      0 /*i forgot where exactly, but the x possition gets set later somewhere */,
                      0)
                  .setBackgroundTexture(GuiTextures.SLOT_DARK));
        }

        slots.setSliderActive(slots.widgets.size() > 5);
        RocketComponentEntryWidget entry =
            new RocketComponentEntryWidget(
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
  // stuff. taken from the gui slots.
  public boolean blueprintBuildAttempt(AbstractRocketBlueprint blueprint) {
    this.errorStage = "";
    this.errorComponentType = "";
    this.error = ComponentValidationResult.UNKNOWN;
    // go through every stage widget
    for (var stageEntry : this.stageContainers.entrySet()) {
      RocketStage stageFrombp =
          blueprint.getStages().stream()
              .filter(x -> x.getName() == stageEntry.getKey())
              .findFirst()
              .get();

      this.errorStage = stageEntry.getKey();
      List<AbstractComponent<?>> components = new ArrayList<>();
      // go through every component type within that stage component
      for (var entryWidgets : stageEntry.getValue().components.entrySet()) {
        this.errorComponentType = entryWidgets.getKey();

        // go through each slot and add each component separately
        if (!entryWidgets.getValue().isShortView()) {
          for (DataStorageLoader componentContainer : entryWidgets.getValue().getSlots()) {
            var cardStack = componentContainer.getStackInSlot(0);
            if (!cardStack.hasTagCompound()) {
              this.error = ComponentValidationResult.INVALID_CARD;
              return false;
            }
            NBTTagCompound tag = cardStack.getTagCompound();
            if (!tag.hasKey("name", NBT.TAG_STRING)) {
              this.error = ComponentValidationResult.INVALID_CARD;
              return false;
            }
            var component =
                AbstractComponent.getComponentFromName(tag.getString("name")).readFromNBT(tag);
            if (!component.isPresent()) {
              this.error = ComponentValidationResult.INVALID_CARD;
              return false;
            }
            components.add(component.get());
          }
        }
        // duplicate the component from the first slot n times since its the same stuff most of the
        // times
        else {
          var cardStack = entryWidgets.getValue().getSlots().get(0).getStackInSlot(0);
          if (!cardStack.hasTagCompound()) {
            this.error = ComponentValidationResult.INVALID_CARD;
            return false;
          }
          NBTTagCompound tag = cardStack.getTagCompound();
          if (!tag.hasKey("name", NBT.TAG_STRING)) {
            this.error = ComponentValidationResult.INVALID_CARD;
            return false;
          }
          var component =
              AbstractComponent.getComponentFromName(tag.getString("name")).readFromNBT(tag);
          if (!component.isPresent()) {
            this.error = ComponentValidationResult.INVALID_CARD;
            return false;
          }
          for (int i = 0; i < entryWidgets.getValue().getAmount(); i++) {
            components.add(component.get());
          }
        }
        // actually set the component type lists with the generated AbstractComponents
        if (!stageFrombp.setComponentListEntry(entryWidgets.getKey(), components)) {
          this.error = ComponentValidationResult.INVALID_AMOUNT;
          return false;
        }
      }
      for (var componentLists : stageFrombp.getComponents().entrySet()) {
        var stat =
            stageFrombp
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

  public String getStatusText() {
    if (this.error == ComponentValidationResult.SUCCESS) {
      return I18n.format(ComponentValidationResult.SUCCESS.getTranslationkey());
    } else {
      if (this.errorComponentType == ""
          || this.errorStage == ""
          || this.error == ComponentValidationResult.UNKNOWN) return "";
    }
    return I18n.format(this.error.getTranslationkey())
        + I18n.format("susy.machine.aerospace_flight_simulator.error_message")
        + "\n"
        + I18n.format("susy.rocketry.stages." + this.errorStage + ".name")
        + "\n"
        + I18n.format("susy.rocketry.components." + this.errorComponentType + ".name");
  }
}
