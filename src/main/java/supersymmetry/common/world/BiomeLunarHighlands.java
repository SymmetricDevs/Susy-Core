package supersymmetry.common.world;

import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

public class BiomeLunarHighlands extends Biome {
    public BiomeLunarHighlands(BiomeProperties properties) {
        super(properties);
        this.decorator.generateFalls = false;
        this.decorator.flowersPerChunk = 0;
        this.decorator.grassPerChunk = 0;
        this.decorator.treesPerChunk = 0;
        this.decorator.mushroomsPerChunk = 0;
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
        return 0f; //Nothing spawns
    }
}
