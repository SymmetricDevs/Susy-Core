package supersymmetry.common.util;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;
import net.minecraft.util.math.BlockPos;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PlanetGrowthRulesTest {

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    @Test
    void samplesAirAboveGrassAndMycelium() {
        BlockPos soil = new BlockPos(1, 160, 2);
        assertEquals(soil.up(), PlanetGrowthRules.getAtmospherePosition(soil, Blocks.GRASS.getDefaultState()));
        assertEquals(soil.up(), PlanetGrowthRules.getAtmospherePosition(soil, Blocks.MYCELIUM.getDefaultState()));
    }

    @Test
    void samplesTheOccupiedCellForCropsAndSaplings() {
        BlockPos plant = new BlockPos(1, 161, 2);
        for (Block block : new Block[] { Blocks.WHEAT, Blocks.SAPLING, Blocks.CACTUS, Blocks.REEDS, Blocks.VINE }) {
            assertEquals(plant, PlanetGrowthRules.getAtmospherePosition(plant, block.getDefaultState()));
        }
    }
}
