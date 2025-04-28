package supersymmetry.loaders.recipes;

import gregtech.api.recipes.ModHandler;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.item.ItemStack;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;
import supersymmetry.loaders.SuSyMetaTileEntityLoader;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

import static gregtech.api.recipes.RecipeMaps.*;
import static gregtech.api.unification.ore.OrePrefix.*;
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

        //GTRecipeHandler.removeAllRecipes(ELECTROLYZER_RECIPES);

        // make more loaders to categorize recipes and what is added

        //RES Example Recipe

        /*
        TagCompound tag = new TagCompound();

        tag.setString("defID", "rolling_stock/locomotives/k4_pacific.json");
        tag.setFloat("gauge", (float) Gauges.STANDARD);

        cam72cam.mod.item.ItemStack is = new cam72cam.mod.item.ItemStack(IRItems.ITEM_ROLLING_STOCK, 1);
        is.setTagCompound(tag);
        SuSyRecipeMaps.RAILROAD_ENGINEERING_STATION_RECIPES.recipeBuilder()
                .input(plate, Materials.Steel)
                .input(plate, Materials.Iron)
                .outputs(is.internal)
                .EUt(GTValues.VA[4])
                .duration(1000)
                .buildAndRegister();

        SuSyRecipeMaps.RAILROAD_ENGINEERING_STATION_RECIPES.recipeBuilder()
                .inputNBT(IRItems.ITEM_ROLLING_STOCK.internal, NBTMatcher.EQUAL_TO, NBTCondition.create(NBTTagType.STRING, "defID", "rolling_stock/locomotives/black_mesa_tram.json"))
                .outputs(is.internal)
                .EUt(GTValues.VA[4])
                .duration(4000)
                .buildAndRegister();


        SuSyRecipeMaps.DRONE_PAD.recipeBuilder()
                .input(ingot, Materials.Iron)
                .output(Items.BEEF, 16)
                .duration(10)
                .dimension(0)
                .EUt(2)
                .buildAndRegister();
        */
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
        for (SusyStoneVariantBlock.StoneType stoneType : SusyStoneVariantBlock.StoneType.values()) {
            MACERATOR_RECIPES.recipeBuilder()
                    .inputs(SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.SMOOTH).getItemVariant(stoneType))
                    .output(dust, stoneType.getMaterial())
                    .buildAndRegister();
            MACERATOR_RECIPES.recipeBuilder()
                    .inputs(SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.COBBLE).getItemVariant(stoneType))
                    .output(dust, stoneType.getMaterial())
                    .buildAndRegister();
        }
    }
}
