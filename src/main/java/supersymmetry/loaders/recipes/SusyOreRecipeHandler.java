package supersymmetry.loaders.recipes;

import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.common.ConfigHolder;
import gregtech.loaders.recipe.BatteryRecipes;
import gregtech.loaders.recipe.handlers.OreRecipeHandler;
import supersymmetry.api.unification.ore.SusyOrePrefix;

public class SusyOreRecipeHandler {

    public static void init(){
        if (ConfigHolder.worldgen.allUniqueStoneTypes) {
            SusyOrePrefix.oreGabbro.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            SusyOrePrefix.oreGneiss.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            SusyOrePrefix.oreLimestone.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            SusyOrePrefix.orePhyllite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            SusyOrePrefix.oreQuartzite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            SusyOrePrefix.oreShale.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            SusyOrePrefix.oreSlate.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            SusyOrePrefix.oreSoapstone.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
            SusyOrePrefix.oreKimberlite.addProcessingHandler(PropertyKey.ORE, OreRecipeHandler::processOre);
        }
    }
}
