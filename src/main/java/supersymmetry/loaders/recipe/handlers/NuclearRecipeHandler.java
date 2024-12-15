package supersymmetry.loaders.recipe.handlers;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.common.items.MetaItems;
import supersymmetry.api.unification.material.properties.FissionFuelProperty;
import supersymmetry.api.unification.material.properties.SuSyPropertyKey;
import supersymmetry.api.unification.ore.SusyOrePrefix;
import supersymmetry.common.item.SuSyMetaItems;

import static gregtech.api.GTValues.*;
import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.ore.OrePrefix.*;
import static supersymmetry.api.recipes.SuSyRecipeMaps.SPENT_FUEL_POOL_RECIPES;
import static supersymmetry.api.unification.ore.SusyOrePrefix.*;

public class NuclearRecipeHandler {

    public static void register() {
        SusyOrePrefix.fuelRod.addProcessingHandler(SuSyPropertyKey.FISSION_FUEL, NuclearRecipeHandler::processFuelRod);
    }

    private static void processFuelRod(OrePrefix orePrefix, Material material, FissionFuelProperty oreProperty) {
        // This is fine, since it goes up to 320x parallel
        SPENT_FUEL_POOL_RECIPES.recipeBuilder().duration(10000).EUt(20)
                .input(fuelRodHotDepleted, material)
                .output(fuelRodDepleted, material)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(200).EUt(VA[HV])
                .input(fuelRodDepleted, material)
                .output(SuSyMetaItems.FUEL_CLADDING)
                .output(fuelPelletDepleted, material, 16)
                .buildAndRegister();

        FORMING_PRESS_RECIPES.recipeBuilder().duration(25).EUt(VA[EV])
                .input(dust, material, 1)
                .notConsumable(MetaItems.SHAPE_MOLD_CYLINDER)
                .output(fuelPellet, material)
                .buildAndRegister();

        CANNER_RECIPES.recipeBuilder().duration(300).EUt(VA[HV])
                .input(fuelPellet, material, 16)
                .input(SuSyMetaItems.FUEL_CLADDING)
                .output(fuelRod, material)
                .buildAndRegister();
    }
}
