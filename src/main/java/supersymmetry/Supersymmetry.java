package supersymmetry;

import gregtech.GTInternalTags;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLConstructionEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.FMLLaunchHandler;
import net.minecraftforge.fml.relauncher.Side;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.sound.SusySounds;
import supersymmetry.client.renderer.handler.RocketRenderer;
import supersymmetry.common.CommonProxy;
import supersymmetry.common.SusyMetaEntities;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SuSyMetaBlocks;
import supersymmetry.common.command.CommandHordeBase;
import supersymmetry.common.command.CommandHordeStart;
import supersymmetry.common.command.CommandHordeStatus;
import supersymmetry.common.command.CommandHordeStop;
import supersymmetry.common.covers.SuSyCoverBehaviors;
import supersymmetry.common.entities.EntityRocket;
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

    @SubscribeEvent
    public void bakeModel(ModelBakeEvent event) {
        IRegistry<ModelResourceLocation, IBakedModel> registry = event.getModelRegistry();
        try {
            IModel model = OBJLoader.INSTANCE.loadModel(new ResourceLocation(MODID, "models/entity/soyuz.obj"));
            registry.putObject(SuSyValues.modelRocket, model.bake(model.getDefaultState(), DefaultVertexFormats.ITEM, ModelLoader.defaultTextureGetter()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SubscribeEvent
    public void stitchTexture(TextureStitchEvent.Pre event) {
        TextureMap map = event.getMap();
        map.registerSprite(new ResourceLocation(MODID, "entity/soyuz"));
    }


    @Mod.EventHandler
    public void onPreInit(@NotNull FMLPreInitializationEvent event) {

        if (FMLLaunchHandler.side() == Side.CLIENT) {
            OBJLoader.INSTANCE.addDomain(MODID);
        }

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
