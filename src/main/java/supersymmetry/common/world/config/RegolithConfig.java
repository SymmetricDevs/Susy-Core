package supersymmetry.common.world.config;

import supersymmetry.common.blocks.BlockRegolith;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.world.SuSyBiomes;

public class RegolithConfig {

    public static void init() {
        // Lunar Highlands - use HIGHLAND regolith (lighter, anorthositic composition)
        // Represents the ancient, heavily cratered terrain
        SuSyBiomes.setCraterBlock(
                SuSyBiomes.LUNAR_HIGHLANDS,
                SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.HIGHLAND));

        // Lunar Maria - use LOWLAND regolith (darker, basaltic composition)
        // Represents the younger volcanic plains
        SuSyBiomes.setCraterBlock(
                SuSyBiomes.LUNAR_MARIA,
                SuSyBlocks.REGOLITH.getState(BlockRegolith.BlockRegolithType.LOWLAND));

        // keep adding stuff here :) - Monsieur Martin 2/1/26
    }
}
