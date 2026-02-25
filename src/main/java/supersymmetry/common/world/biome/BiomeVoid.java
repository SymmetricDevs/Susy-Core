package supersymmetry.common.world.biome;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.EnumCreatureType;

import supersymmetry.api.space.dimension.biome.SpaceBiome;

public class BiomeVoid extends SpaceBiome {

    public BiomeVoid(BiomeProperties properties) {
        super(new BiomeProperties("void")
                .setTemperature(0.0f) // fuck you
                .setRainfall(0.0f)
                .setRainDisabled());
    }

    @Override
    @Nonnull
    public List<SpawnListEntry> getSpawnableList(EnumCreatureType type) {
        return new LinkedList<>();
    }

    @Override
    public float getSpawningChance() {
        return 0f; // Nothing spawns
    }
}
