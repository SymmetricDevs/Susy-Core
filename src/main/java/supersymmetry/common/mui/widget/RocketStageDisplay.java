package supersymmetry.common.mui.widget;

import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.ClickButtonWidget;
import gregtech.api.gui.widgets.DynamicLabelWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import java.util.ArrayList;
import java.util.List;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.rocketry.rockets.RocketStage;
import supersymmetry.api.util.DataStorageLoader;

public class RocketStageDisplay extends AbstractWidgetGroup {

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
    RocketSimulatorComponentContainerWidget currentStageView = new RocketSimulatorComponentContainerWidget(new Position(0, 10),new Size(300, 300));

  public RocketStageDisplay(Position pos, Size size, slotProvider slotProvider) {
    super(pos, size);
    this.provider = slotProvider;
    previousButton =
        new ClickButtonWidget(
                0,
                0,
                size.width / 5,
                10,
                "<",
                (clickdata) -> {
                  this.selectedStageIndex--;
                })
            .setShouldClientCallback(true)
            .setButtonTexture(SusyGuiTextures.SPACEFLIGHT_SIMULATOR_BUTTON_LEFT);
    nextButton =
        new ClickButtonWidget(
                (size.width / 5) * 4,
                0,
                size.width / 5,
                10,
                ">",
                (clickdata) -> {
                  this.selectedStageIndex++;
                })
            .setShouldClientCallback(true)
            .setButtonTexture(SusyGuiTextures.SPACEFLIGHT_SIMULATOR_BUTTON_RIGHT);
    amountTextField =
        new DynamicLabelWidget(
            (int) ((size.width / 5) * 2.5),
            0,
            () -> {
              return String.format(
                  "{}/{}", Integer.toString(this.getSelectedIndex()), this.stages.size());
            });

    this.addWidget(amountTextField);
    this.addWidget(nextButton);
  }

  public int getSelectedIndex() {
    return selectedStageIndex % this.stages.size();
  }

  public RocketStage getSelectedStage() {
    return this.stages.get(getSelectedIndex());
  }

  public void generateFromBlueprint(AbstractRocketBlueprint blueprint) {}
}
