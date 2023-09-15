package supersymmetry;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.fluids.SusyFluids;
import supersymmetry.api.sound.SusySounds;
import supersymmetry.common.CommonProxy;
import supersymmetry.common.SusyMetaEntities;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SuSyMetaBlocks;
import supersymmetry.common.covers.SuSyCoverBehaviors;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;
import gregtech.GTInternalTags;

@Mod(name = Supersymmetry.NAME, modid = Supersymmetry.MODID, version = Tags.VERSION, dependencies = GTInternalTags.DEP_VERSION_STRING + ";required-after:gcym;after:immersiverailroading")

public class Supersymmetry {

    public static final String NAME = "Supersymmetry";
    public static final String MODID = "susy";

    @SidedProxy(modId = MODID, clientSide = "supersymmetry.client.ClientProxy", serverSide = "supersymmetry.common.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance(Supersymmetry.MODID)
    public static Supersymmetry instance;

    @Mod.EventHandler
    public void onModConstruction(FMLConstructionEvent event) {
        //This is now a config option I think
        //GTValues.HT = true;
    }

    @Mod.EventHandler
    public void onPreInit(@NotNull FMLPreInitializationEvent event) {
        proxy.preLoad();

        SuSyMetaBlocks.init();
        SuSyMetaItems.initMetaItems();
        SuSyBlocks.init();
        SusyFluids.init();

        SusySounds.registerSounds();

        SuSyMetaTileEntities.init();
        SusyMetaEntities.init();
    }

    @Mod.EventHandler
    public void onInit(@NotNull FMLInitializationEvent event) {
        proxy.load();
        SuSyCoverBehaviors.init();
    }
}
