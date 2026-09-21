package supersymmetry.common.world.biome;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.EnumCreatureType;

import org.jspecify.annotations.NonNull;

import supersymmetry.common.blocks.BlockRegolith;
import supersymmetry.common.blocks.BlockResource1;
import supersymmetry.common.blocks.SuSyBlocks;

public class BiomeLunarKreepTerrane extends PlanetaryBiome {

    public BiomeLunarKreepTerrane(BiomeProperties properties) {
        super(properties);
        this.topBlock = SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.KREEP);
        this.fillerBlock = SuSyBlocks.RESOURCE_BLOCK_1.getState(BlockResource1.ResourceBlockType.KREEP);
    }

    @Override
    @NonNull public List<SpawnListEntry> getSpawnableList(EnumCreatureType type) {
        return new LinkedList<>();
    }

    @Override
    public float getSpawningChance() {
        return 0f; // Nothing spawns
    }
}
