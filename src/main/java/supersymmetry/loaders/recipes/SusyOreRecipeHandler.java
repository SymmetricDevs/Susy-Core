package supersymmetry.loaders.recipes;

import net.minecraft.item.ItemStack;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeBuilder;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.OreProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.MaterialStack;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.api.util.GTUtility;
import gregtech.common.ConfigHolder;
import gregtech.loaders.recipe.handlers.OreRecipeHandler;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.unification.ore.SusyOrePrefix;

public class SusyOreRecipeHandler {

    public static void init() {
        OrePrefix.ore.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
        OrePrefix.oreEndstone.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
        OrePrefix.oreNetherrack.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);

        if (ConfigHolder.worldgen.allUniqueStoneTypes) {
            OrePrefix.oreGranite.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            OrePrefix.oreDiorite.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            OrePrefix.oreAndesite.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            OrePrefix.oreBasalt.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            OrePrefix.oreBlackgranite.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            OrePrefix.oreMarble.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            OrePrefix.oreRedgranite.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            OrePrefix.oreSand.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            OrePrefix.oreRedSand.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            // SUSY ores
            SusyOrePrefix.oreGabbro.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            SusyOrePrefix.oreGneiss.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            SusyOrePrefix.oreLimestone.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            SusyOrePrefix.orePhyllite.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            SusyOrePrefix.oreQuartzite.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            SusyOrePrefix.oreShale.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            SusyOrePrefix.oreSlate.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            SusyOrePrefix.oreSoapstone.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            SusyOrePrefix.oreKimberlite.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
            SusyOrePrefix.oreAnorthosite.addProcessingHandler(PropertyKey.ORE, SusyOreRecipeHandler::processOre);
        }
    }

    public static void processDoubleOre(OrePrefix orePrefix, Material material, OreProperty property) {
        // Basically just a copy from OreRecipeHandler
        // But the original hardcodes when the ore is doubled...
        Material byproductMaterial = property.getOreByProduct(0, material);
        ItemStack byproductStack = OreDictUnifier.get(OrePrefix.gem, byproductMaterial);
        if (byproductStack.isEmpty()) {
            byproductStack = OreDictUnifier.get(OrePrefix.dust, byproductMaterial);
        }

        ItemStack crushedStack = OreDictUnifier.get(OrePrefix.crushed, material);
        Material smeltingMaterial = property.getDirectSmeltResult() == null ? material :
                property.getDirectSmeltResult();
        double amountOfCrushedOre = (double) property.getOreMultiplier();
        ItemStack ingotStack;
        if (smeltingMaterial.hasProperty(PropertyKey.INGOT)) {
            ingotStack = OreDictUnifier.get(OrePrefix.ingot, smeltingMaterial);
        } else if (smeltingMaterial.hasProperty(PropertyKey.GEM)) {
            ingotStack = OreDictUnifier.get(OrePrefix.gem, smeltingMaterial);
        } else {
            ingotStack = OreDictUnifier.get(OrePrefix.dust, smeltingMaterial);
        }

        int oreTypeMultiplier = 2;
        ingotStack.setCount(ingotStack.getCount() * property.getOreMultiplier() * oreTypeMultiplier);
        crushedStack.setCount(crushedStack.getCount() * property.getOreMultiplier());
        if (!crushedStack.isEmpty()) {
            RecipeBuilder<?> builder = RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder().input(orePrefix, material)
                    .duration(10).EUt(16);
            if (material.hasProperty(PropertyKey.GEM) && !OreDictUnifier.get(OrePrefix.gem, material).isEmpty()) {
                builder.outputs(GTUtility.copy((int) Math.ceil(amountOfCrushedOre) * oreTypeMultiplier,
                        OreDictUnifier.get(OrePrefix.gem, material, crushedStack.getCount())));
            } else {
                builder.outputs(GTUtility.copy((int) Math.ceil(amountOfCrushedOre) * oreTypeMultiplier, crushedStack));
            }

            builder.buildAndRegister();
            builder = RecipeMaps.MACERATOR_RECIPES.recipeBuilder().input(orePrefix, material)
                    .outputs(GTUtility.copy((int) Math.round(amountOfCrushedOre) * 2 * oreTypeMultiplier, crushedStack))
                    .chancedOutput(byproductStack, 1400, 850).duration(400);

            for (MaterialStack secondaryMaterial : orePrefix.secondaryMaterials) {
                if (secondaryMaterial.material.hasProperty(PropertyKey.DUST)) {
                    ItemStack dustStack = OreDictUnifier.getGem(secondaryMaterial);
                    builder.chancedOutput(dustStack, 6700, 800);
                }
            }

            builder.buildAndRegister();
        }

        if (!ingotStack.isEmpty() && doesMaterialUseNormalFurnace(smeltingMaterial)) {
            ModHandler.addSmeltingRecipe(new UnificationEntry(orePrefix, material), ingotStack, 0.5F);
        }
    }

    public static void processOre(OrePrefix orePrefix, Material material, OreProperty property) {
        int oreTypeMultiplier = orePrefix != SusyOrePrefix.oreAnorthosite ? 2 : 1;
        if (orePrefix.id >= SusyOrePrefix.oreGabbro.id) { // Hacky way to look for SUSY ores in particular
            if (oreTypeMultiplier == 2) {
                processDoubleOre(orePrefix, material, property);
            } else {
                OreRecipeHandler.processOre(orePrefix, material, property);
            }
        }

        double amountOfCrushedOre = property.getOreMultiplier();
        Material byproductMaterial = property.getOreByProduct(0, material);
        ItemStack byproductStack = OreDictUnifier.get(OrePrefix.gem, byproductMaterial);

        ItemStack crushedStack = OreDictUnifier.get(OrePrefix.crushed, material);
        ItemStack dustImpureStack = OreDictUnifier.get(OrePrefix.dustImpure, material);

        SuSyRecipeMaps.ECCENTRIC_ROLL_CRUSHER.recipeBuilder()
                .input(orePrefix, material).outputs(
                        GTUtility.copy((int) Math.round(amountOfCrushedOre) * 2 * oreTypeMultiplier, crushedStack))
                .chancedOutput(byproductStack, 1400, 850)
                .chancedOutput(byproductStack, 1400, 850)
                .duration(50)
                .EUt(24)
                .buildAndRegister();
        SuSyRecipeMaps.BALL_MILL.recipeBuilder()
                .input(OrePrefix.crushed, material).outputs(dustImpureStack)
                .chancedOutput(byproductStack, 1400, 850)
                .chancedOutput(byproductStack, 1400, 850)
                .duration(50)
                .EUt(16)
                .buildAndRegister();
    }

    private static boolean doesMaterialUseNormalFurnace(Material material) {
        return !material.hasProperty(PropertyKey.BLAST);
    }
}
