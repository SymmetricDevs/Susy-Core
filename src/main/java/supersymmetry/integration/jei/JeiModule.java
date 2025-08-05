package supersymmetry.integration.jei;

import cam72cam.immersiverailroading.IRItems;
import gregtech.api.GTValues;
import gregtech.api.modules.GregTechModule;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.util.Mods;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.integration.IntegrationSubmodule;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import org.jetbrains.annotations.NotNull;
import supersymmetry.Supersymmetry;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;
import supersymmetry.modules.SuSyModules;

@JEIPlugin
@GregTechModule(
        moduleID = SuSyModules.MODULE_JEI,
        containerID = Supersymmetry.MODID,
        modDependencies = Mods.Names.JUST_ENOUGH_ITEMS,
        name = "SuSy JEI Integration",
        description = "SuSy JEI Integration Module")
public class JeiModule extends IntegrationSubmodule implements IModPlugin {

    @Override
    public void registerItemSubtypes(@NotNull ISubtypeRegistry subtypeRegistry) {
        subtypeRegistry.registerSubtypeInterpreter(IRItems.ITEM_ROLLING_STOCK.internal,
                new RollingStockSubtypeHandler());
    }

    @Override
    public void register(IModRegistry registry) {
        String semiFluidMapId = GTValues.MODID + ":" + RecipeMaps.SEMI_FLUID_GENERATOR_FUELS.getUnlocalizedName();

        registry.addRecipeCatalyst(SuSyMetaTileEntities.LARGE_BRONZE_BOILER.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.LARGE_STEEL_BOILER.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.STEAM_BOILER_LIQUID_BRONZE.getStackForm(), semiFluidMapId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.STEAM_BOILER_LIQUID_STEEL.getStackForm(), semiFluidMapId);

        String solidMapId = GTValues.MODID + ":" + SuSyRecipeMaps.BOILER_RECIPES.getUnlocalizedName();

        registry.addRecipeCatalyst(SuSyMetaTileEntities.LARGE_BRONZE_BOILER.getStackForm(), solidMapId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.LARGE_STEEL_BOILER.getStackForm(), solidMapId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.STEAM_BOILER_COAL_BRONZE.getStackForm(), solidMapId);
        registry.addRecipeCatalyst(SuSyMetaTileEntities.STEAM_BOILER_COAL_STEEL.getStackForm(), solidMapId);

    }
}
