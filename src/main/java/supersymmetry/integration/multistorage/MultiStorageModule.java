package supersymmetry.integration.multistorage;

import gregtech.api.modules.GregTechModule;
import gregtech.integration.IntegrationSubmodule;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import shetiphian.multistorage.common.block.BlockVault;
import supersymmetry.Supersymmetry;
import supersymmetry.modules.SuSyModules;

import static supersymmetry.api.util.SuSyUtility.susyId;

@GregTechModule(
        moduleID = SuSyModules.MODULE_MULTISTORAGE,
        containerID = Supersymmetry.MODID,
        modDependencies = "multistorage",
        name = "SuSy MultiStorage Integration",
        description = "SuSy MultiStorage Integration Module")
public class MultiStorageModule extends IntegrationSubmodule {

    @Override
    public void preInit(FMLPreInitializationEvent event) {
        GameRegistry.registerTileEntity(TileEntityVaultPowerGTEU.class, susyId("vault_power_gteu"));
    }

    @Override
    public void init(FMLInitializationEvent event) {
        BlockVault.EnumType.WALL_POWER_EU.setTile(TileEntityVaultPowerGTEU.class);
    }
}
