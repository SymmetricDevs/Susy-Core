package supersymmetry.common.worldgen;

import biomesoplenty.common.world.ChunkGeneratorHellBOP;
import net.minecraft.world.World;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.*;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
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

    private WorldgenControl() {
    }

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
            ChunkGeneratorSettings settings;
            try {
                Field field = ObfuscationReflectionHelper.findField(ChunkGeneratorOverworld.class, "settings");
                field.setAccessible(true);
                settings = (ChunkGeneratorSettings) field.get(generator);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to reflect ChunkGeneratorSettings in dim " + world.provider.getDimensionType(), e);
            }

            assert settings != null;
            try {
                disableOverworldStructures(settings);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to disable Overworld structure gen in dim " + world.provider.getDimensionType(), e);
            }
        } else if (generator instanceof ChunkGeneratorRTG) {
            RTGChunkGenSettings settings;
            try {
                Field field = ObfuscationReflectionHelper.findField(ChunkGeneratorRTG.class, "settings");
                field.setAccessible(true);
                settings = (RTGChunkGenSettings) field.get(generator);
            } catch (IllegalAccessException e) {
                throw new RuntimeException("Failed to reflect RTGChunkGenSettings in dim " + world.provider.getDimensionType(), e);
            }

            assert settings != null;
            try {
                disableOverworldStructures(settings);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to disable RTG structure gen in dim " + world.provider.getDimensionType(), e);
            }
        } else if (generator instanceof ChunkGeneratorHell) {
            try {
                disableNetherStructures(generator);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to disable Nether structure gen in dim " + world.provider.getDimensionType(), e);
            }
        } else if (generator instanceof ChunkGeneratorHellBOP) {
            try {
                disableNetherStructures(generator);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to disable BOP Nether structure gen in dim " + world.provider.getDimensionType(), e);
            }
        }
    }

    /**
     * Disables overworld structures
     *
     * @param o the chunk generator to disable it for
     * @throws NoSuchFieldException   if the field does not exist
     * @throws IllegalAccessException if the field cannot be modified
     */
    private static void disableOverworldStructures(@Nonnull Object o) throws NoSuchFieldException, IllegalAccessException {
        final Class<?> clazz = o.getClass();
        // villages
        disableValue(o, ObfuscationReflectionHelper.findField(clazz, "useVillages"));
        // spawner dungeons
        disableValue(o, ObfuscationReflectionHelper.findField(clazz, "useDungeons"));
        // jungle, desert temples, igloos, witch huts
        disableValue(o, ObfuscationReflectionHelper.findField(clazz, "useTemples"));
        // ocean monuments
        disableValue(o, ObfuscationReflectionHelper.findField(clazz, "useMonuments"));
        // woodland mansions
        disableValue(o, ObfuscationReflectionHelper.findField(clazz, "useMansions"));
        // strongholds
        disableValue(o, ObfuscationReflectionHelper.findField(clazz, "useStrongholds"));
    }

    /**
     * Disables nether structures
     *
     * @param generator the chunk generator to disable it for
     * @throws NoSuchFieldException   if the field does not exist
     * @throws IllegalAccessException if the field cannot be modified
     */
    private static void disableNetherStructures(@Nonnull IChunkGenerator generator) throws NoSuchFieldException, IllegalAccessException {
        disableValue(generator, ObfuscationReflectionHelper.findField(generator.getClass(), "generateStructures"));
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
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(o, false);
    }
}
