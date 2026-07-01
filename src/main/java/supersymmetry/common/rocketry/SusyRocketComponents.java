package supersymmetry.common.rocketry;

import net.minecraft.util.ResourceLocation;

import gregtech.api.GregTechAPI;
import gregtech.api.fluids.store.FluidStorageKeys;
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

        new RocketFuelEntry.RocketFuelEntryBuilder("Methane-LOX")
                .addComponent(Materials.Methane.getFluid(FluidStorageKeys.LIQUID) != null ?
                        Materials.Methane.getFluid(FluidStorageKeys.LIQUID) :
                        Materials.Methane.getFluid(), 100)
                .addComponent(Materials.Oxygen.getFluid(FluidStorageKeys.LIQUID), 355)
                .density(0.983).sIVacuum(330).sIPerPressure(240)
                .register();

        if (GregTechAPI.materialManager.getMaterial("rp_one") != null) {
            new RocketFuelEntry.RocketFuelEntryBuilder("RP1-LOX")
                    .addComponent(GregTechAPI.materialManager.getMaterial("rp_one").getFluid(), 100)
                    .addComponent(Materials.Oxygen.getFluid(FluidStorageKeys.LIQUID), 256)
                    .density(0.915).sIVacuum(350).sIPerPressure(290)
                    .register();
        }

        if ((GregTechAPI.materialManager.getMaterial("monomethylhydrazine") != null) &&
                (GregTechAPI.materialManager.getMaterial("dinitrogen_tetroxide") != null)) {
            new RocketFuelEntry.RocketFuelEntryBuilder("MMH-N2O4")
                    .addComponent(GregTechAPI.materialManager.getMaterial("monomethylhydrazine").getFluid(), 100)
                    .addComponent(GregTechAPI.materialManager.getMaterial("dinitrogen_tetroxide").getFluid(), 216)
                    .density(1.2).sIVacuum(410).sIPerPressure(350)
                    .register();
        }

        ROCKET_SOYUZ_BLUEPRINT_DEFAULT = new SimpleStagedRocketBlueprint.Builder("soyuz")
                .stage(
                        new RocketStage.Builder("boosters")
                                .type("engine")
                                .limit(4)
                                .type("tank")
                                .limit(4)
                                .type("engine_small")
                                .limit(12)
                                .build())
                .stage(
                        new RocketStage.Builder("block_A")
                                .type("engine")
                                .limit(4)
                                .type("engine_small")
                                .limit(4)
                                .type("tank")
                                .limit(2)
                                .type("interstage")
                                .limit(1)
                                .build())
                .stage(
                        new RocketStage.Builder("block_F")
                                .type("engine")
                                .limit(4)
                                .type("engine_small")
                                .limit(4)
                                .type("tank")
                                .limit(2)
                                .type("interstage")
                                .limit(1)
                                .build())
                .stage(
                        new RocketStage.Builder("payload")
                                .type("spacecraft")
                                .limit(1)
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
