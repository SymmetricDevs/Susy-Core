package supersymmetry.loaders.recipes;

import gregtech.api.recipes.ModHandler;
import gregtech.common.blocks.BlockStoneCobble;
import gregtech.common.blocks.BlockStoneSmooth;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.item.ItemStack;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyBlockStoneCobble;
import supersymmetry.common.blocks.SusyBlockStoneSmooth;
import supersymmetry.loaders.SuSyMetaTileEntityLoader;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static gregtech.api.recipes.RecipeMaps.EXTRUDER_RECIPES;
import static gregtech.api.recipes.RecipeMaps.FORGE_HAMMER_RECIPES;
import static gregtech.common.items.MetaItems.SHAPE_EXTRUDER_BLOCK;

public class SuSyRecipeLoader {

    public static void init() {
        SuSyMetaTileEntityLoader.init();
        FridgeRecipes.init();
        CoagulationRecipes.init();
        VulcanizationRecipes.init();
        SusyOreRecipeHandler.init();
        registerStoneRecipes();
        // make more loaders to categorize recipes and what is added
    }

    private static void registerStoneRecipes(){
        List<ItemStack> susysmooths = Arrays.stream(SusyBlockStoneSmooth.BlockType.values()).map(SuSyBlocks.SUSY_STONE_SMOOTH::getItemVariant).collect(Collectors.toList());
        List<ItemStack> susycobbles = Arrays.stream(SusyBlockStoneCobble.BlockType.values()).map(SuSyBlocks.SUSY_STONE_COBBLE::getItemVariant).collect(Collectors.toList());

        List<ItemStack> smooths = Arrays.stream(BlockStoneSmooth.BlockType.values()).map(MetaBlocks.STONE_SMOOTH::getItemVariant).collect(Collectors.toList());
        List<ItemStack> cobbles = Arrays.stream(BlockStoneCobble.BlockType.values()).map(MetaBlocks.STONE_COBBLE::getItemVariant).collect(Collectors.toList());

        registerSmoothRecipe(susycobbles, susysmooths);
        registerCobbleRecipe(susysmooths, susycobbles);
        registerCobbleSmashingRecipe(susysmooths, susycobbles);
        registerCobbleSmashingRecipe(smooths, cobbles);
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
}
