package supersymmetry.common.world.biome;

import net.minecraft.entity.EnumCreatureType;
import supersymmetry.common.blocks.BlockRegolith;
import supersymmetry.common.blocks.BlockResource1;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.blocks.SusyStoneVariantBlock;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

public class BiomeLunarKreepTerrane extends PlanetaryBiome {

    public BiomeLunarKreepTerrane(BiomeProperties properties) {
        super(new BiomeProperties("lunar kreep terrane")
                .setTemperature(0.0f) // fuck you
                .setRainfall(0.0f)
                .setRainDisabled());
        this.topBlock = SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.KREEP);
        this.fillerBlock = SuSyBlocks.RESOURCE_BLOCK_1.getState(BlockResource1.ResourceBlockType.KREEP);
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
