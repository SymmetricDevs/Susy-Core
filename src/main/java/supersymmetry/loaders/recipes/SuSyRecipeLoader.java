package supersymmetry.loaders.recipes;

import static gregtech.common.items.MetaItems.SHAPE_EXTRUDER_BLOCK;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.item.ItemStack;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;
import supersymmetry.loaders.SuSyMetaTileEntityLoader;

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
    }

    private static void registerCobbleRecipe(List<ItemStack> smoothStack, List<ItemStack> cobbleStack) {
        for (int i = 0; i < smoothStack.size(); i++) {
            RecipeMaps.FORGE_HAMMER_RECIPES.recipeBuilder()
                    .inputs(smoothStack.get(i))
                    .outputs(cobbleStack.get(i))
                    .duration(12).EUt(4).buildAndRegister();
        }
    }

    private static void registerSmoothRecipe(List<ItemStack> roughStack, List<ItemStack> smoothStack) {
        for (int i = 0; i < roughStack.size(); i++) {
            ModHandler.addSmeltingRecipe(roughStack.get(i), smoothStack.get(i), 0.1f);

            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
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
