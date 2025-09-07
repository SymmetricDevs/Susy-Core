package supersymmetry.common.rocketry;

import net.minecraft.util.ResourceLocation;
import software.bernie.shadowed.eliotlash.mclib.math.functions.classic.Abs;
import supersymmetry.Supersymmetry;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.rocketry.rockets.RocketStage;
import supersymmetry.common.rocketry.components.*;
import supersymmetry.common.rocketry.rockets.SimpleStagedRocketBlueprint;

public class SusyRocketComponents {

  public static SimpleStagedRocketBlueprint ROCKET_SOYUZ_BLUEPRINT_DEFAULT;

  public static void init() {
    AbstractComponent.registerComponent(new ComponentFairing());
    AbstractComponent.registerComponent(new ComponentControlPod());
    AbstractComponent.registerComponent(new ComponentLavalEngine());
    AbstractComponent.registerComponent(new ComponentInterstage());
    AbstractComponent.registerComponent(new ComponentSpacecraft());
    AbstractComponent.registerComponent(new ComponentLiquidFuelTank());
    AbstractComponent.lockRegistry();
    // TODO add the emergency escape system
    ROCKET_SOYUZ_BLUEPRINT_DEFAULT =
        new SimpleStagedRocketBlueprint.Builder("soyuz")
            .stage(
                new RocketStage.Builder("boosters")
                    .type("engine")
                    .limit(16)
                    .type("tank")
                    .limit(8)
                    .type("vernier_engine")
                    .limit(8)
                    .build())
            .ignitesWith(
                new RocketStage.Builder("block A")
                    .type("engine")
                    .limit(4)
                    .type("vernier_engine")
                    .limit(4)
                    .type("tank")
                    .limit(2)
                    .build())
            .stage(
                new RocketStage.Builder("block F")
                    .type("engine")
                    .limit(4)
                    .type("vernier_engine")
                    .limit(4)
                    .type("tank")
                    .limit(2)
                    .build())
            .entityResourceLocation(new ResourceLocation(Supersymmetry.MODID, "rocket_basic"))
            .build();
    AbstractRocketBlueprint.registerBlueprint(ROCKET_SOYUZ_BLUEPRINT_DEFAULT);
    AbstractRocketBlueprint.setRegistryLock(true);
  }
}
