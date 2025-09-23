package supersymmetry.common.world.biome;

import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.entity.EnumCreatureType;

import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import supersymmetry.common.blocks.BlockRegolith;
import supersymmetry.common.blocks.SuSyBlocks;

public class BiomeLunarMaria extends PlanetaryBiome {

    public BiomeLunarMaria(BiomeProperties properties) {
        super(properties);

        this.topBlock = SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.LOWLAND);
        this.fillerBlock = MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH)
                .getState(StoneVariantBlock.StoneType.BASALT);
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
