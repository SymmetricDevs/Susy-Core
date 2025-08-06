package supersymmetry.common.rocketry;

import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.rockets.rocketStage;
import supersymmetry.common.rocketry.components.*;
import supersymmetry.common.rocketry.rockets.SimpleStagedRocketBlueprint;

public class SusyRocketComponents {

  public static SimpleStagedRocketBlueprint ROCKET_SOYUZ_BLUEPRINT_DEFAULT;

  public static void init() {
    AbstractComponent.registerComponent(new ComponentFairing());
    AbstractComponent.registerComponent(new ComponentControlPod());
    AbstractComponent.registerComponent(new ComponentLavalEngine());
    AbstractComponent.registerComponent(new ComponentFairing());
    AbstractComponent.registerComponent(new ComponentInterstage());
    AbstractComponent.registerComponent(new ComponentSpacecraft());
    AbstractComponent.lockRegistry();
    // TODO add the emergency escape system
    ROCKET_SOYUZ_BLUEPRINT_DEFAULT =
        new SimpleStagedRocketBlueprint.Builder("soyuz")
            .stage(
                new rocketStage.Builder("boosters")
                    .type("engine")
                    .limit(16)
                    .type("tank")
                    .limit(8)
                    .type("vernier_engine")
                    .limit(8)
                    .build())
            .ignitesWith(
                new rocketStage.Builder("block A")
                    .type("engine")
                    .limit(4)
                    .type("vernier_engine")
                    .limit(4)
                    .type("tank")
                    .limit(2)
                    .build())
            .stage(
                new rocketStage.Builder("block F")
                    .type("engine")
                    .limit(4)
                    .type("vernier_engine")
                    .limit(4)
                    .type("tank")
                    .limit(2)
                    .build())
            .build();
  }
}
