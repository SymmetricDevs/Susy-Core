package supersymmetry.common.world.biome;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.EnumCreatureType;

import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;

public class BiomeLunarHighlands extends PlanetaryBiome {

    public BiomeLunarHighlands(BiomeProperties properties) {
        super(properties);
        this.topBlock = SuSyBlocks.REGOLITH.getDefaultState();
        this.fillerBlock = SuSyBlocks.SUSY_STONE_BLOCKS.get(SusyStoneVariantBlock.StoneVariant.SMOOTH)
                .getState(SusyStoneVariantBlock.StoneType.ANORTHOSITE);
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
