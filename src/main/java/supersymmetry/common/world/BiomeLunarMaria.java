package supersymmetry.common.world;

import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.biome.Biome;
import supersymmetry.common.blocks.BlockRegolith;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

public class BiomeLunarMaria extends Biome {
    public BiomeLunarMaria(BiomeProperties properties) {
        super(properties);
        this.decorator.generateFalls = false;
        this.decorator.flowersPerChunk = 0;
        this.decorator.grassPerChunk = 0;
        this.decorator.treesPerChunk = 0;
        this.decorator.mushroomsPerChunk = 0;
        this.topBlock = SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.LOWLAND);
        this.fillerBlock = MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(StoneVariantBlock.StoneType.BASALT);
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
