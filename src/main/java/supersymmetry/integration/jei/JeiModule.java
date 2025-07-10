package supersymmetry.integration.jei;

import cam72cam.immersiverailroading.IRItems;
import gregtech.api.modules.GregTechModule;
import gregtech.api.util.Mods;
import gregtech.integration.IntegrationSubmodule;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import org.jetbrains.annotations.NotNull;
import supersymmetry.Supersymmetry;
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
}
