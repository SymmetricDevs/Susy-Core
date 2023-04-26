package supersymmetry.loaders.recipes;

import gregtech.api.recipes.ModHandler;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.DustProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.stack.UnificationEntry;
import gregtech.common.items.MetaItems;
import supersymmetry.api.unification.material.info.SuSyMaterialFlags;
import supersymmetry.api.unification.ore.SusyOrePrefix;
import supersymmetry.common.item.SuSyMetaItems;

import static gregtech.api.GTValues.ULV;
import static gregtech.api.GTValues.VA;

public class SuSyMaterialRecipeHandler {

    public static void init() {
        SusyOrePrefix.catalystBed.addProcessingHandler(PropertyKey.DUST, SuSyMaterialRecipeHandler::processCatalystBed);
        SusyOrePrefix.catalystPellet.addProcessingHandler(PropertyKey.DUST, SuSyMaterialRecipeHandler::processCatalystPellet);
    }

    public static void processCatalystBed(OrePrefix catalystBedPrefix, Material mat, DustProperty property) {
        if (mat.hasFlag(SuSyMaterialFlags.GENERATE_CATALYST_BED)) {
            ModHandler.addShapedRecipe(String.format("catalyst_bed_%s", mat),
                    OreDictUnifier.get(catalystBedPrefix, mat, 1),
                    " S ", "SCS",  " S ",
                    'S', new UnificationEntry(SusyOrePrefix.catalystPellet, mat),
                    'C', SuSyMetaItems.CATALYST_BED_SUPPORT_GRID);

            RecipeMaps.ASSEMBLER_RECIPES.recipeBuilder()
                    .input(SusyOrePrefix.catalystPellet, mat, 4)
                    .input(SuSyMetaItems.CATALYST_BED_SUPPORT_GRID)
                    .outputs(OreDictUnifier.get(catalystBedPrefix, mat, 1))
                    .EUt(VA[ULV]).duration(64)
                    .buildAndRegister();
        }
    }

    public static void processCatalystPellet(OrePrefix catalystPelletPrefix, Material mat, DustProperty property) {
        if (mat.hasFlag(SuSyMaterialFlags.GENERATE_CATALYST_PELLET)) {
            RecipeMaps.EXTRUDER_RECIPES.recipeBuilder()
                    .input(OrePrefix.dust, mat, 1)
                    .notConsumable(MetaItems.SHAPE_EXTRUDER_BOLT)
                    .outputs(OreDictUnifier.get(catalystPelletPrefix, mat, 4))
                    .EUt(VA[ULV]).duration(64)
                    .buildAndRegister();
        }
    }
}
