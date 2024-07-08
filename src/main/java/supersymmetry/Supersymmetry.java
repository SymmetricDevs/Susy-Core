package supersymmetry;

import gregtech.GTInternalTags;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.event.MobHordeEvent;
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
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;
import supersymmetry.loaders.SuSyIRLoader;

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
        SuSyIRLoader.initDefinitions();
        SuSyIRLoader.initEntities();
    }

    @Mod.EventHandler
    public void onPreInit(@NotNull FMLPreInitializationEvent event) {
        proxy.preLoad();

        SuSyMetaBlocks.init();
        SuSyMetaItems.initMetaItems();
        SuSyBlocks.init();

        SusySounds.registerSounds();

        SuSyMetaTileEntities.init();
        SusyMetaEntities.init();
    }

    @Mod.EventHandler
    public void onInit(@NotNull FMLInitializationEvent event) {
        proxy.load();
        SuSyCoverBehaviors.init();
    }

    @Mod.EventHandler
    public void onServerStarting(@NotNull FMLServerStartingEvent event) {
        CommandHordeBase hordeCommand = new CommandHordeBase();
        event.registerServerCommand(hordeCommand);

        hordeCommand.addSubcommand(new CommandHordeStart());
        hordeCommand.addSubcommand(new CommandHordeStop());
        hordeCommand.addSubcommand(new CommandHordeStatus());
    }
}
