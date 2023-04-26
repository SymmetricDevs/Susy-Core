package supersymmetry.common.worldgen;

import biomesoplenty.common.world.ChunkGeneratorHellBOP;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.*;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import rtg.api.world.gen.RTGChunkGenSettings;
import rtg.world.gen.ChunkGeneratorRTG;
import supersymmetry.Supersymmetry;
import supersymmetry.api.SusyLog;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Controls world generation
 * <p>
 * May require updates if BOP or RTG change field names or locations
 */
@Mod.EventBusSubscriber(modid = Supersymmetry.MODID) // cannot force server side here, doesn't work on SP
public final class WorldgenControl {

    private WorldgenControl() {/**/}

    /**
     * Disables structures
     *
     * @param event the event for World Loading
     */
    @SubscribeEvent
    public static void disableStructures(@Nonnull WorldEvent.Load event) {
        final World world = event.getWorld();
        // must be server side
        if (world.isRemote) return;

        final IChunkProvider provider = world.getChunkProvider();
        // this should be impossible, check anyway
        if (!(provider instanceof ChunkProviderServer)) {
            SusyLog.logger.fatal("Server-Side world in dimension {} did not have a ChunkProviderServer",
                    world.provider.getDimensionType());
            return;
        }

        // everything here should be guaranteed server-side only from now on

        final IChunkGenerator generator = ((ChunkProviderServer) provider).chunkGenerator;

        if (generator instanceof ChunkGeneratorOverworld) {
            ChunkGeneratorSettings settings = ((ChunkGeneratorOverworld) generator).settings;
            // the settings are nullable, as they are not always initialized
            if (settings != null) disableOverworldStructures(settings);
        } else if (generator instanceof ChunkGeneratorHell) {
            disableNetherStructures((ChunkGeneratorHell) generator);
        } else if (generator instanceof ChunkGeneratorRTG) {
            RTGChunkGenSettings settings;
            try {
                Field field = ChunkGeneratorRTG.class.getDeclaredField("settings");
                field.setAccessible(true);
                settings = (RTGChunkGenSettings) field.get(generator);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to reflect RTGChunkGenSettings in dim " + world.provider.getDimensionType(), e);
            }

            assert settings != null;
            try {
                disableRTGOverworldStructures(settings);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to disable RTG structure gen in dim " + world.provider.getDimensionType(), e);
            }
        } else if (generator instanceof ChunkGeneratorHellBOP) {
            try {
                disableBOPNetherStructures((ChunkGeneratorHellBOP) generator);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to disable BOP Nether structure gen in dim " + world.provider.getDimensionType(), e);
            }
        }
    }

    /**
     * Disables vanilla overworld structures
     *
     * @param settings the chunk generator settings to disable it for
     */
    private static void disableOverworldStructures(@Nonnull ChunkGeneratorSettings settings) {
        // villages
        settings.useVillages = false;
        // spawner dungeons
        settings.useDungeons = false;
        // jungle, desert temples, igloos, witch huts
        settings.useTemples = false;
        // ocean monuments
        settings.useMonuments = false;
        // woodland mansions
        settings.useMansions = false;
        // strongholds
        settings.useStrongholds = false;
    }

    /**
     * Disables RTG overworld structures
     *
     * @param settings the chunk generator settings to disable it for
     * @throws NoSuchFieldException   if the field does not exist
     * @throws IllegalAccessException if the field cannot be modified
     */
    private static void disableRTGOverworldStructures(@Nonnull RTGChunkGenSettings settings) throws NoSuchFieldException, IllegalAccessException {
        // using getField because these fields are public

        // villages
        disableValue(settings, RTGChunkGenSettings.class.getField("useVillages"));
        // spawner dungeons
        disableValue(settings, RTGChunkGenSettings.class.getField("useDungeons"));
        // jungle, desert temples, igloos, witch huts
        disableValue(settings, RTGChunkGenSettings.class.getField("useTemples"));
        // ocean monuments
        disableValue(settings, RTGChunkGenSettings.class.getField("useMonuments"));
        // woodland mansions
        disableValue(settings, RTGChunkGenSettings.class.getField("useMansions"));
        // strongholds
        disableValue(settings, RTGChunkGenSettings.class.getField("useStrongholds"));
    }

    /**
     * Disables nether structures
     *
     * @param generator the chunk generator to disable it for
     */
    private static void disableNetherStructures(@Nonnull ChunkGeneratorHell generator) {
        generator.generateStructures = false;
    }

    /**
     * Disables nether structures
     *
     * @param generator the chunk generator to disable it for
     * @throws NoSuchFieldException   if the field does not exist
     * @throws IllegalAccessException if the field cannot be modified
     */
    private static void disableBOPNetherStructures(@Nonnull ChunkGeneratorHellBOP generator) throws NoSuchFieldException, IllegalAccessException {
        // needs declaredField because this is private in BOP
        disableValue(generator, ChunkGeneratorHellBOP.class.getDeclaredField("generateStructures"));
    }

    /**
     * Sets a final boolean to false
     *
     * @param o     the object to disable the field for
     * @param field the field to disable
     * @throws NoSuchFieldException   if the field does not exist
     * @throws IllegalAccessException if the field cannot be modified
     */
    private static void disableValue(@Nonnull Object o, @Nonnull Field field) throws NoSuchFieldException, IllegalAccessException {
        if (!field.isAccessible()) field.setAccessible(true);

        if ((field.getModifiers() & Modifier.FINAL) != 0) {
            // only make the field non-final if it's currently final
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        }

        field.set(o, false);
    }
}
