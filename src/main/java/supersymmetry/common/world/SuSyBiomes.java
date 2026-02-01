package supersymmetry.common.world;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.world.biome.Biome;

import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.world.biome.BiomeLunarHighlands;
import supersymmetry.common.world.biome.BiomeLunarMaria;

public class SuSyBiomes {

    public static BiomeLunarHighlands LUNAR_HIGHLANDS;
    public static BiomeLunarMaria LUNAR_MARIA;

    private static final Map<Biome, IBlockState> BIOME_CRATER_BLOCKS = new HashMap<>();

    /**
     * Sets the crater/regolith block for a specific biome.
     * This block will be used for crater ejecta and surface material in that biome.
     *
     * @param biome The biome to configure
     * @param block The regolith block state to use
     */
    public static void setCraterBlock(Biome biome, IBlockState block) {
        BIOME_CRATER_BLOCKS.put(biome, block);
    }

    /**
     * Gets the crater block for a specific biome.
     * Returns default regolith if no custom block is set.
     *
     * @param biome The biome to query
     * @return The IBlockState for crater material in this biome
     */
    public static IBlockState getCraterBlock(Biome biome) {
        return BIOME_CRATER_BLOCKS.getOrDefault(biome, SuSyBlocks.REGOLITH.getDefaultState());
    }

    /**
     * Checks if a biome has a custom crater block configured.
     *
     * @param biome The biome to check
     * @return true if custom crater block is set, false otherwise
     */
    public static boolean hasCraterBlock(Biome biome) {
        return BIOME_CRATER_BLOCKS.containsKey(biome);
    }

    /**
     * Clears all custom crater block configurations.
     */
    public static void clearCraterBlocks() {
        BIOME_CRATER_BLOCKS.clear();
    }
}
