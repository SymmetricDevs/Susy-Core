package supersymmetry.loaders.recipes;

import gregtech.api.GregTechAPI;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.ConfigHolder;
import gregtech.loaders.recipe.BatteryRecipes;
import gregtech.loaders.recipe.handlers.OreRecipeHandler;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.unification.ore.SusyOrePrefix;

import static gregtech.api.unification.material.Materials.Coal;
import static gregtech.api.unification.material.Materials.Coke;

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

        OrePrefix.ore.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
        OrePrefix.oreEndstone.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
        OrePrefix.oreNetherrack.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
        if (ConfigHolder.worldgen.allUniqueStoneTypes) {
            OrePrefix.oreGranite.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
            OrePrefix.oreDiorite.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
            OrePrefix.oreAndesite.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
            OrePrefix.oreBasalt.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
            OrePrefix.oreBlackgranite.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
            OrePrefix.oreMarble.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
            OrePrefix.oreRedgranite.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
            OrePrefix.oreSand.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
            OrePrefix.oreRedSand.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
            SusyOrePrefix.oreGabbro.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
            SusyOrePrefix.oreGneiss.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
            SusyOrePrefix.oreLimestone.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
            SusyOrePrefix.orePhyllite.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
            SusyOrePrefix.oreQuartzite.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
            SusyOrePrefix.oreShale.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
            SusyOrePrefix.oreSlate.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
            SusyOrePrefix.oreSoapstone.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
            SusyOrePrefix.oreKimberlite.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);

        }

        OrePrefix.crushed.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
        OrePrefix.crushedPurified.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
        OrePrefix.crushedCentrifuged.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
        OrePrefix.dustImpure.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
        OrePrefix.dustPure.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processSuSyOre);
    }

    public static void processSuSyOre(OrePrefix anyOrePrefix, Material material, OreProperty property) {
        Material smeltingResult = property.getDirectSmeltResult() != null ? property.getDirectSmeltResult() : material;
        ItemStack ingotStack;

        if (smeltingResult.hasProperty(PropertyKey.INGOT)) {
            ingotStack = OreDictUnifier.get(OrePrefix.ingot, smeltingResult);
        } else if (smeltingResult.hasProperty(PropertyKey.GEM)) {
            ingotStack = OreDictUnifier.get(OrePrefix.gem, smeltingResult);
        } else {
            ingotStack = OreDictUnifier.get(OrePrefix.dust, smeltingResult);
        }
        int oreTypeMultiplier = anyOrePrefix != OrePrefix.oreNetherrack && anyOrePrefix != OrePrefix.oreEndstone ? 1 : 2;
        int oreMultiplier = property.getOreMultiplier();
        ingotStack.setCount(ingotStack.getCount() * oreTypeMultiplier * oreMultiplier);

        if (smeltingResult.hasProperty(PropertyKey.INGOT)) {
            if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingResult)) {
                SuSyRecipeMaps.PRIMITIVE_SMELTER.recipeBuilder().duration(200)
                        .input(anyOrePrefix, material)
                        .input(OrePrefix.gem, Coal)
                        .outputs(ingotStack)
                        .buildAndRegister();

                ingotStack = ingotStack.copy();
                ingotStack.setCount(ingotStack.getCount() * 2);

                SuSyRecipeMaps.PRIMITIVE_SMELTER.recipeBuilder().duration(300)
                        .input(anyOrePrefix, material, 2)
                        .input(OrePrefix.gem, Coke)
                        .outputs(ingotStack)
                        .buildAndRegister();

                if (GregTechAPI.materialManager.getMaterial("anthracite") != null) {
                    Material anthracite = GregTechAPI.materialManager.getMaterial("anthracite");
                    SuSyRecipeMaps.PRIMITIVE_SMELTER.recipeBuilder().duration(280)
                            .input(anyOrePrefix, material, 2)
                            .input(OrePrefix.gem, anthracite)
                            .outputs(ingotStack)
                            .buildAndRegister();
                }

                if (GregTechAPI.materialManager.getMaterial("lignite") != null) {
                    Material lignite = GregTechAPI.materialManager.getMaterial("lignite");
                    SuSyRecipeMaps.PRIMITIVE_SMELTER.recipeBuilder().duration(400)
                            .input(anyOrePrefix, material, 2)
                            .input(OrePrefix.gem, lignite, 3)
                            .outputs(ingotStack)
                            .buildAndRegister();
                }
            }
        }


    }

    private static boolean doesMaterialUseNormalFurnace(Material material) {
        return !material.hasProperty(PropertyKey.BLAST);
    }


}
