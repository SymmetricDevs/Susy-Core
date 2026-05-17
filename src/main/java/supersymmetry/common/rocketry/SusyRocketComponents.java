package supersymmetry.common.rocketry;

import gregtech.api.GregTechAPI;
import gregtech.api.fluids.store.FluidStorageKeys;
import net.minecraft.util.ResourceLocation;

import gregtech.api.unification.material.Materials;
import net.minecraftforge.fluids.FluidRegistry;
import supersymmetry.Supersymmetry;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.api.rocketry.rockets.AbstractRocketBlueprint;
import supersymmetry.api.rocketry.rockets.RocketStage;
import supersymmetry.common.materials.SusyMaterials;
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
        new RocketFuelEntry.RocketFuelEntryBuilder("RP1-LOX")
                .addComponent(SusyMaterials.RP_1.getFluid(), 100)
                .addComponent(Materials.Oxygen.getFluid(FluidStorageKeys.LIQUID), 256)
                .density(0.915).sIVacuum(380).sIPerPressure(327)
                .register();

        new RocketFuelEntry.RocketFuelEntryBuilder("Methane-LOX")
                .addComponent(Materials.Methane.getFluid(), 100)
                .addComponent(Materials.Oxygen.getFluid(FluidStorageKeys.LIQUID), 355)
                .density(0.983).sIVacuum(315).sIPerPressure(250)
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
                .stage(
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
                .stage(
                        new RocketStage.Builder("payload")
                                .type("spacecraft")
                                .limit(4)
                                .type("fairing")
                                .limit(2)
                                .type("engine_small")
                                .limit(3)
                                .type("tank")
                                .limit(1)
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
