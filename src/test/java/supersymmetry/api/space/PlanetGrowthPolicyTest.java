package supersymmetry.api.space;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Bootstrap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

class PlanetGrowthPolicyTest {

    private BiMap<Planetoid, Integer> savedPlanets;

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
    }

    @BeforeEach
    void isolateRegistry() {
        savedPlanets = HashBiMap.create(Planetoid.PLANETOIDS);
        Planetoid.PLANETOIDS.clear();
    }

    @AfterEach
    void restoreRegistry() {
        Planetoid.PLANETOIDS.clear();
        Planetoid.PLANETOIDS.putAll(savedPlanets);
    }

    private static Planetoid planet(String name, int dimension) {
        return new Planetoid(name, 1.0, 0.0, 0.0, 0.0, 0.0, null, PlanetType.TERRESTRIAL)
                .setDimension(dimension);
    }

    @Test
    void coversVanillaGrowthAndSpreadingIncludingNonGrowablePlants() {
        planet("moon", 800).setGrowthPolicy(PlanetGrowthPolicy.MACHINE_ONLY);
        Block[] vegetation = { Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS,
                Blocks.SAPLING, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM, Blocks.COCOA, Blocks.NETHER_WART,
                Blocks.GRASS, Blocks.MYCELIUM, Blocks.VINE, Blocks.CACTUS, Blocks.REEDS,
                Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.CHORUS_FLOWER };
        for (Block block : vegetation) {
            assertTrue(PlanetGrowthPolicy.forDimension(800).suppressRandomTick(block),
                    block.getRegistryName().toString());
        }
    }

    @Test
    void retainsNonVegetationTicksAndLeafDecay() {
        planet("moon", 800).setGrowthPolicy(PlanetGrowthPolicy.MACHINE_ONLY);
        Block[] unaffected = { Blocks.FIRE, Blocks.ICE, Blocks.SNOW, Blocks.LEAVES, Blocks.LEAVES2,
                Blocks.FARMLAND, Blocks.STONE, Blocks.FLOWING_WATER, Blocks.FLOWING_LAVA };
        for (Block block : unaffected) {
            assertFalse(PlanetGrowthPolicy.forDimension(800).suppressRandomTick(block),
                    block.getRegistryName().toString());
        }
    }

    @Test
    void newPlanetsAndUnknownDimensionsDefaultToNormalGrowth() {
        planet("earth", 0);
        planet("future", 801);
        for (int dimension : new int[] { -1, 0, 1, 8, 25, 799, 801, 802 }) {
            PlanetGrowthPolicy policy = PlanetGrowthPolicy.forDimension(dimension);
            assertFalse(policy.suppressRandomTick(Blocks.WHEAT));
            assertFalse(policy.suppressRandomTick(Blocks.SAPLING));
            assertTrue(policy.allowsPlayerBonemeal());
        }
    }

    @Test
    void onePolicyCanBeAppliedToMultiplePlanets() {
        planet("moon", 800).setGrowthPolicy(PlanetGrowthPolicy.MACHINE_ONLY);
        planet("future", 801).setGrowthPolicy(PlanetGrowthPolicy.MACHINE_ONLY);
        for (int dimension : new int[] { 800, 801 }) {
            PlanetGrowthPolicy policy = PlanetGrowthPolicy.forDimension(dimension);
            assertTrue(policy.suppressRandomTick(Blocks.WHEAT));
            assertFalse(policy.allowsPlayerBonemeal());
        }
        assertFalse(PlanetGrowthPolicy.forDimension(0).suppressRandomTick(Blocks.WHEAT));
    }

    @Test
    void naturalGrowthAndPlayerBonemealAreConfiguredIndependently() {
        planet("natural", 801).setGrowthPolicy(new PlanetGrowthPolicy(true, false));
        planet("bonemeal", 802).setGrowthPolicy(new PlanetGrowthPolicy(false, true));
        assertFalse(PlanetGrowthPolicy.forDimension(801).suppressRandomTick(Blocks.WHEAT));
        assertFalse(PlanetGrowthPolicy.forDimension(801).allowsPlayerBonemeal());
        assertTrue(PlanetGrowthPolicy.forDimension(802).suppressRandomTick(Blocks.WHEAT));
        assertTrue(PlanetGrowthPolicy.forDimension(802).allowsPlayerBonemeal());
    }

    @Test
    void policyFollowsDimensionReassignmentWithoutAnotherRegistry() {
        Planetoid moon = planet("moon", 800).setGrowthPolicy(PlanetGrowthPolicy.MACHINE_ONLY);
        moon.setDimension(805);
        assertFalse(PlanetGrowthPolicy.forDimension(800).suppressRandomTick(Blocks.WHEAT));
        assertTrue(PlanetGrowthPolicy.forDimension(805).suppressRandomTick(Blocks.WHEAT));
        assertFalse(PlanetGrowthPolicy.forDimension(805).allowsPlayerBonemeal());
    }

    @Test
    void rejectsNullWithoutLosingExistingPolicy() {
        Planetoid moon = planet("moon", 800).setGrowthPolicy(PlanetGrowthPolicy.MACHINE_ONLY);
        assertThrows(NullPointerException.class, () -> moon.setGrowthPolicy(null));
        assertTrue(PlanetGrowthPolicy.forDimension(800).suppressRandomTick(Blocks.WHEAT));
    }

    @Test
    void atmosphericPolicyAllowsGrowthAndBonemealOnlyWithLocalOxygen() {
        PlanetGrowthPolicy policy = PlanetGrowthPolicy.ATMOSPHERE_REQUIRED;
        assertTrue(policy.requiresAtmosphere());
        assertTrue(policy.suppressRandomTick(Blocks.WHEAT, false));
        assertTrue(policy.suppressRandomTick(Blocks.SAPLING, false));
        assertTrue(policy.suppressBonemeal(null, false));
        assertFalse(policy.suppressRandomTick(Blocks.WHEAT, true));
        assertFalse(policy.suppressRandomTick(Blocks.SAPLING, true));
        assertFalse(policy.suppressBonemeal(null, true));
    }

    @Test
    void fixedPoliciesKeepTheirMeaningWhenOxygenIsPresent() {
        assertFalse(PlanetGrowthPolicy.UNRESTRICTED.requiresAtmosphere());
        assertFalse(PlanetGrowthPolicy.MACHINE_ONLY.requiresAtmosphere());
        assertFalse(PlanetGrowthPolicy.UNRESTRICTED.suppressRandomTick(Blocks.WHEAT, false));
        assertTrue(PlanetGrowthPolicy.MACHINE_ONLY.suppressRandomTick(Blocks.WHEAT, true));
        assertTrue(PlanetGrowthPolicy.MACHINE_ONLY.suppressBonemeal(null, true));
    }

    @Test
    void atmosphericPolicyKeepsUnrelatedRandomTicksInVacuum() {
        for (Block block : new Block[] { Blocks.LEAVES, Blocks.FARMLAND, Blocks.ICE, Blocks.FIRE }) {
            assertFalse(PlanetGrowthPolicy.ATMOSPHERE_REQUIRED.suppressRandomTick(block, false));
        }
    }

    @Test
    void atmosphericPolicyFollowsTheExistingPlanetRegistry() {
        Planetoid moon = planet("moon", 800).setGrowthPolicy(PlanetGrowthPolicy.ATMOSPHERE_REQUIRED);
        assertSame(PlanetGrowthPolicy.ATMOSPHERE_REQUIRED, PlanetGrowthPolicy.forDimension(800));
        moon.setDimension(805);
        assertSame(PlanetGrowthPolicy.UNRESTRICTED, PlanetGrowthPolicy.forDimension(800));
        assertSame(PlanetGrowthPolicy.ATMOSPHERE_REQUIRED, PlanetGrowthPolicy.forDimension(805));
    }
}
