package supersymmetry;

import gregtech.api.GTValues;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.sound.SusySounds;
import supersymmetry.common.CommonProxy;
import supersymmetry.common.SusyMetaEntities;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

@Mod(name = Supersymmetry.NAME, modid = Supersymmetry.MODID,
        dependencies = GTValues.MOD_VERSION_DEP + "required-after:gcym")
public class Supersymmetry {

    public static final String NAME = "Supersymmetry";
    public static final String MODID = "susy";

    @SidedProxy(modId = MODID, clientSide = "supersymmetry.client.ClientProxy", serverSide = "supersymmetry.common.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance(Supersymmetry.MODID)
    public static Supersymmetry instance;

    @Mod.EventHandler
    public void onPreInit(@NotNull FMLPreInitializationEvent event) {
        proxy.preLoad();
        SuSyMetaItems.initMetaItems();
        SuSyBlocks.init();

        SusySounds.registerSounds();

        SuSyMetaTileEntities.init();
        SusyMetaEntities.init();
    }
}
