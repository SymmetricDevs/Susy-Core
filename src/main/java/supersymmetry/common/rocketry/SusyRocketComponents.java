package supersymmetry.common.rocketry;

import net.minecraft.util.ResourceLocation;

import gregtech.api.unification.material.Materials;
import supersymmetry.Supersymmetry;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.rocketry.rockets.RocketStage;
import supersymmetry.common.rocketry.components.*;
import supersymmetry.common.rocketry.rockets.SimpleStagedRocketBlueprint;

public class SusyRocketComponents {

    public static SimpleStagedRocketBlueprint ROCKET_SOYUZ_BLUEPRINT_DEFAULT;
    public static SimpleStagedRocketBlueprint ROCKET_V1_BLUEPRINT_DEFAULT;

    public static void init() {
        AbstractComponent.registerComponent(new ComponentFairing());
        AbstractComponent.registerComponent(new ComponentLavalEngine());
        AbstractComponent.registerComponent(new ComponentVernierEngine());
        AbstractComponent.registerComponent(new ComponentInterstage());
        AbstractComponent.registerComponent(new ComponentSpacecraft());
        AbstractComponent.registerComponent(new ComponentLiquidFuelTank());
        AbstractComponent.lockRegistry();
        if (Materials.Benzene == null || Materials.Oxygen == null) {
            throw new RuntimeException();
        }
        new RocketFuelEntry.RocketFuelEntryBuilder("kerosene-lox", Materials.Benzene, 0.5)
                .addComponent(Materials.Oxygen, 0.5)
                .setCharacteristics(1000, 1000, 1000)
                .register();

        // TODO add the emergency escape system
        ROCKET_SOYUZ_BLUEPRINT_DEFAULT = new SimpleStagedRocketBlueprint.Builder("soyuz")
                .stage(
                        new RocketStage.Builder("boosters")
                                .type("engine")
                                .limit(16)
                                .type("tank")
                                .limit(8)
                                .type("engine_small")
                                .limit(8)
                                .build())
                .ignitesWith(
                        new RocketStage.Builder("block_A")
                                .type("engine")
                                .limit(4)
                                .type("engine_small")
                                .limit(4)
                                .type("tank")
                                .limit(2)
                                .build())
                .stage(
                        new RocketStage.Builder("block_F")
                                .type("engine")
                                .limit(4)
                                .type("engine_small")
                                .limit(4)
                                .type("tank")
                                .limit(2)
                                .build())
                .entityResourceLocation(new ResourceLocation(Supersymmetry.MODID, "rocket_basic"))
                .build();

        // this was added for testing so that i dont have to fill out all components every time
        ROCKET_V1_BLUEPRINT_DEFAULT = new SimpleStagedRocketBlueprint.Builder("V1")
                .stage(
                        new RocketStage.Builder("main")
                                .type("engine")
                                .limit(1)
                                .type("tank")
                                .limit(2)
                                // .type("chemical_bomb")
                                // .limit(1)
                                // .limit(2)
                                // .limit(3)
                                .build())
                .entityResourceLocation(new ResourceLocation(Supersymmetry.MODID, "rocket_basic"))
                .build();

        AbstractRocketBlueprint.registerBlueprint(ROCKET_V1_BLUEPRINT_DEFAULT);
        AbstractRocketBlueprint.registerBlueprint(ROCKET_SOYUZ_BLUEPRINT_DEFAULT);
        AbstractRocketBlueprint.setRegistryLock(true);
    }
}
