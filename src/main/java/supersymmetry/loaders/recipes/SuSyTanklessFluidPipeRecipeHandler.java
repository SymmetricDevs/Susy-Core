package supersymmetry.loaders.recipes;

import static gregtech.api.GTValues.ULV;
import static gregtech.api.GTValues.VA;
import static gregtech.api.recipes.RecipeMaps.ASSEMBLER_RECIPES;
import static gregtech.api.unification.material.Materials.Iron;
import static gregtech.api.unification.material.Materials.Steel;
import static gregtech.api.unification.ore.OrePrefix.*;
import static supersymmetry.api.unification.ore.SusyOrePrefix.*;

import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.info.MaterialFlags;
import gregtech.api.unification.ore.OrePrefix;
import lombok.val;
import supersymmetry.api.unification.material.properties.SuSyPropertyKey;

public class SuSyTanklessFluidPipeRecipeHandler {

    public static void init() {
        registerTankless(pipeTinyFluid, pipeTinyTanklessFluid);
        registerTankless(pipeSmallFluid, pipeSmallTanklessFluid);
        registerTankless(pipeNormalFluid, pipeNormalTanklessFluid);
        registerTankless(pipeLargeFluid, pipeLargeTanklessFluid);
        registerTankless(pipeHugeFluid, pipeHugeTanklessFluid);

        // Restrictive variants: 1 regular tankless pipe + 2 rings -> restrictive pipe,
        registerRestrictive(pipeTinyTanklessFluid, pipeTinyRestrictiveTanklessFluid);
        registerRestrictive(pipeSmallTanklessFluid, pipeSmallRestrictiveTanklessFluid);
        registerRestrictive(pipeNormalTanklessFluid, pipeNormalRestrictiveTanklessFluid);
        registerRestrictive(pipeLargeTanklessFluid, pipeLargeRestrictiveTanklessFluid);
        registerRestrictive(pipeHugeTanklessFluid, pipeHugeRestrictiveTanklessFluid);
    }

    private static void registerRestrictive(OrePrefix regularPipe, OrePrefix restrictivePipe) {
        restrictivePipe.addProcessingHandler(SuSyPropertyKey.TANKLESS_FLUID_PIPE,
                (_, material, _) -> ASSEMBLER_RECIPES.recipeBuilder()
                        .input(regularPipe, material, 1)
                        .input(ring, Iron, 2)
                        .output(restrictivePipe, material, 1)
                        .duration(20)
                        .EUt(VA[ULV])
                        .buildAndRegister());
    }

    private static void registerTankless(OrePrefix sourcePipe, OrePrefix tanklessPipe) {
        tanklessPipe.addProcessingHandler(SuSyPropertyKey.TANKLESS_FLUID_PIPE,
                (_, material, _) -> processTanklessPipe(sourcePipe, tanklessPipe, material));
    }

    private static void processTanklessPipe(OrePrefix sourcePipe, OrePrefix tanklessPipe, Material material) {
        val screwMaterial = material.hasFlag(MaterialFlags.GENERATE_BOLT_SCREW) ? material : Steel;

        int plateCount;
        if (sourcePipe == pipeTinyFluid || sourcePipe == pipeSmallFluid) {
            plateCount = 1;
        } else if (sourcePipe == pipeNormalFluid) {
            plateCount = 3;
        } else if (sourcePipe == pipeLargeFluid) {
            plateCount = 6;
        } else if (sourcePipe == pipeHugeFluid) {
            plateCount = 6;
        } else {
            return;
        }

        // 1 normal fluid pipe + 2 screws -> 1 tankless pipe
        ASSEMBLER_RECIPES.recipeBuilder()
                .input(sourcePipe, material, 1)
                .input(screw, screwMaterial, (plateCount + 1) / 2)
                .output(tanklessPipe, material, 1)
                .circuitMeta(2)
                .duration(20)
                .EUt(VA[ULV])
                .buildAndRegister();
    }
}
