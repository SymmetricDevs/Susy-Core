package supersymmetry.common.rocketry;

import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.common.rocketry.components.*;

public class SusyRocketComponents {
  
  
  public static void init() {
    AbstractComponent.registerComponent(new componentFairing());
    AbstractComponent.registerComponent(new componentControlPod());
    AbstractComponent.registerComponent(new componentLavalEngine());
    AbstractComponent.registerComponent(new componentFairing());
    AbstractComponent.registerComponent(new componentInterstage());
    AbstractComponent.registerComponent(new componentSpacecraft());
    AbstractComponent.lockRegistry();
  }
}
