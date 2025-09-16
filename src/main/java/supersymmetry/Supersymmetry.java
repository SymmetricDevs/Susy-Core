package supersymmetry;

import gregtech.GTInternalTags;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.*;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import supersymmetry.api.capability.SuSyCapabilities;
import supersymmetry.api.sound.SusySounds;
import supersymmetry.common.CommonProxy;
import supersymmetry.common.SusyMetaEntities;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SuSyMetaBlocks;
import supersymmetry.common.command.CommandHordeBase;
import supersymmetry.common.command.CommandHordeStart;
import supersymmetry.common.command.CommandHordeStatus;
import supersymmetry.common.command.CommandHordeStop;
import supersymmetry.common.covers.SuSyCoverBehaviors;
import supersymmetry.common.event.DimensionBreathabilityHandler;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;
import supersymmetry.common.tileentities.SuSyTileEntities;
import supersymmetry.common.rocketry.SusyRocketComponents;
import supersymmetry.loaders.SuSyIRLoader;

import java.sql.Ref;

@Mod(name = Supersymmetry.NAME, modid = Supersymmetry.MODID, version = Tags.VERSION, dependencies = GTInternalTags.DEP_VERSION_STRING + ";required-after:gcym;after:immersiverailroading")

public class Supersymmetry {

    public static final String NAME = "Supersymmetry";
    public static final String MODID = "susy";

    @SidedProxy(modId = MODID, clientSide = "supersymmetry.client.ClientProxy", serverSide = "supersymmetry.common.CommonProxy")
    public static CommonProxy proxy;

    @Mod.Instance(Supersymmetry.MODID)
    public static Supersymmetry instance;

    public static Reflections reflectionHandler;

    @Mod.EventHandler
    public void onModConstruction(FMLConstructionEvent event) {
        //This is now a config option I think
        //GTValues.HT = true;
        SuSyIRLoader.initDefinitions();
        SuSyIRLoader.initEntities();

        // Groovyscript starts immediately!
        proxy.checkCanaryFile();
    }


    @Mod.EventHandler
    public void onPreInit(@NotNull FMLPreInitializationEvent event) {

        proxy.preLoad();

        reflectionHandler = new Reflections("supersymmetry");

        SuSyMetaBlocks.init();
        SuSyMetaItems.initMetaItems();
        SuSyBlocks.init();

        SusySounds.registerSounds();

        SuSyMetaTileEntities.init();
        SuSyCapabilities.init();
        SusyRocketComponents.init();

        SusyMetaEntities.init();

        if (FMLLaunchHandler.side() == Side.CLIENT) {
            OBJLoader.INSTANCE.addDomain(MODID);
        }

        DimensionBreathabilityHandler.loadConfig();
    }

    @Mod.EventHandler
    public void onInit(@NotNull FMLInitializationEvent event) {
        proxy.load();
        SuSyCoverBehaviors.init();
    }

    @Mod.EventHandler
    public void onPostInit(@NotNull FMLPostInitializationEvent event) {
        proxy.postLoad();
    }

    @Mod.EventHandler
    public void onServerStarting(@NotNull FMLServerStartingEvent event) {
        CommandHordeBase hordeCommand = new CommandHordeBase();
        event.registerServerCommand(hordeCommand);

        hordeCommand.addSubcommand(new CommandHordeStart());
        hordeCommand.addSubcommand(new CommandHordeStop());
        hordeCommand.addSubcommand(new CommandHordeStatus());
    }

    @SideOnly(Side.CLIENT)
    @Mod.EventHandler
    public void registerRenderers(FMLPreInitializationEvent event) {
        SuSyTileEntities.registerRenderers();
    }
}
