package supersymmetry.loaders.recipes;

import gregtech.api.recipes.GTRecipeHandler;
import gregtech.api.recipes.ModHandler;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.item.ItemStack;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;
import supersymmetry.common.materials.SusyMaterials;
import supersymmetry.loaders.SuSyMetaTileEntityLoader;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.ore.OrePrefix.dust;
import static gregtech.api.unification.ore.OrePrefix.stone;
import static gregtech.common.items.MetaItems.SHAPE_EXTRUDER_BLOCK;

public class SuSyRecipeLoader {

    public static void init() {
        SuSyMetaTileEntityLoader.init();
        FridgeRecipes.init();
        CoagulationRecipes.init();
        VulcanizationRecipes.init();
        SusyOreRecipeHandler.init();
        SuSyMaterialRecipeHandler.init();
        registerStoneRecipes();
        GTRecipeHandler.removeAllRecipes(ELECTROLYZER_RECIPES);
        // make more loaders to categorize recipes and what is added
    }

    private static void registerStoneRecipes(){
        EnumMap<SusyStoneVariantBlock.StoneVariant, List<ItemStack>> susyVariantListMap = new EnumMap<>(SusyStoneVariantBlock.StoneVariant.class);
        for (SusyStoneVariantBlock.StoneVariant shape : SusyStoneVariantBlock.StoneVariant.values()) {
            SusyStoneVariantBlock block = SuSyBlocks.SUSY_STONE_BLOCKS.get(shape);
            susyVariantListMap.put(shape,
                    Arrays.stream(SusyStoneVariantBlock.StoneType.values())
                            .map(block::getItemVariant)
                            .collect(Collectors.toList()));
        }
        List<ItemStack> susycobbles = susyVariantListMap.get(SusyStoneVariantBlock.StoneVariant.COBBLE);
        List<ItemStack> susysmooths = susyVariantListMap.get(SusyStoneVariantBlock.StoneVariant.SMOOTH);

        EnumMap<StoneVariantBlock.StoneVariant, List<ItemStack>> variantListMap = new EnumMap<>(StoneVariantBlock.StoneVariant.class);
        for (StoneVariantBlock.StoneVariant shape : StoneVariantBlock.StoneVariant.values()) {
            StoneVariantBlock block = MetaBlocks.STONE_BLOCKS.get(shape);
            variantListMap.put(shape,
                    Arrays.stream(StoneVariantBlock.StoneType.values())
                            .map(block::getItemVariant)
                            .collect(Collectors.toList()));
        }

        List<ItemStack> cobbles = variantListMap.get(StoneVariantBlock.StoneVariant.COBBLE);
        List<ItemStack> smooths = variantListMap.get(StoneVariantBlock.StoneVariant.SMOOTH);

        registerSmoothRecipe(susycobbles, susysmooths);
        registerCobbleRecipe(susysmooths, susycobbles);
        registerCobbleSmashingRecipe(susysmooths, susycobbles);
        registerCobbleSmashingRecipe(smooths, cobbles);
        registerMacerationToStoneDustRecipe();
    }

    private static void registerCobbleRecipe(List<ItemStack> smoothStack, List<ItemStack> cobbleStack) {
        for (int i = 0; i < smoothStack.size(); i++) {
            FORGE_HAMMER_RECIPES.recipeBuilder()
                    .inputs(smoothStack.get(i))
                    .outputs(cobbleStack.get(i))
                    .duration(12).EUt(4).buildAndRegister();
        }
    }

    private static void registerSmoothRecipe(List<ItemStack> roughStack, List<ItemStack> smoothStack) {
        for (int i = 0; i < roughStack.size(); i++) {
            ModHandler.addSmeltingRecipe(roughStack.get(i), smoothStack.get(i), 0.1f);

            EXTRUDER_RECIPES.recipeBuilder()
                    .inputs(roughStack.get(i))
                    .notConsumable(SHAPE_EXTRUDER_BLOCK.getStackForm())
                    .outputs(smoothStack.get(i))
                    .duration(24).EUt(8).buildAndRegister();
        }
    }


    private static void registerCobbleSmashingRecipe(List<ItemStack> smoothStack, List<ItemStack> cobbleStack) {
        for (int i = 0; i < smoothStack.size(); i++) {

            ModHandler.addShapedRecipe(smoothStack.get(i).getDisplayName() + "_hammer_smashing", cobbleStack.get(i), new Object[]{"hS", 'S', smoothStack.get(i)});
        }
    }

    private static void registerMacerationToStoneDustRecipe() {
        MACERATOR_RECIPES.recipeBuilder()
                .input(stone, SusyMaterials.Gabbro)
                .output(dust, SusyMaterials.Gabbro)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input(stone, SusyMaterials.Gneiss)
                .output(dust, SusyMaterials.Gneiss)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input(stone, SusyMaterials.Limestone)
                .output(dust, SusyMaterials.Limestone)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input(stone, SusyMaterials.Phyllite)
                .output(dust, SusyMaterials.Phyllite)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input(stone, Materials.Quartzite)
                .output(dust, Materials.Quartzite)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input(stone, SusyMaterials.Shale)
                .output(dust, SusyMaterials.Shale)
                .buildAndRegister();

        MACERATOR_RECIPES.recipeBuilder()
                .input(stone, SusyMaterials.Slate)
                .output(dust, SusyMaterials.Slate)
                .buildAndRegister();

//        MACERATOR_RECIPES.recipeBuilder()
//                .input(stone, Materials.Soapstone)
//                .output(dust, Materials.Soapstone)
//                .buildAndRegister();


        MACERATOR_RECIPES.recipeBuilder()
                .input(stone, SusyMaterials.Kimberlite)
                .output(dust, SusyMaterials.Kimberlite)
                .buildAndRegister();

    }
}
