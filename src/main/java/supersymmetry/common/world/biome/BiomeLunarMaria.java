package supersymmetry.common.world.biome;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.EnumCreatureType;

import org.jspecify.annotations.NonNull;

import supersymmetry.common.blocks.BlockRegolith;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;

public class BiomeLunarMaria extends PlanetaryBiome {

    public BiomeLunarMaria(BiomeProperties properties) {
        super(properties);

        this.topBlock = SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.LOWLAND);
        this.fillerBlock = SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.SMOOTH)
                .getState(SusyStoneVariantBlock.StoneType.LEUCOBASALT);
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
