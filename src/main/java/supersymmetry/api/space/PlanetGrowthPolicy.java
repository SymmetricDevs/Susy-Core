package supersymmetry.api.space;

import net.minecraft.block.Block;
import net.minecraft.block.BlockChorusFlower;
import net.minecraft.block.BlockMycelium;
import net.minecraft.block.BlockVine;
import net.minecraft.block.IGrowable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.util.FakePlayer;

/** Environmental growth rules selected when a planet is defined. Direct machine growth remains separate. */
public final class PlanetGrowthPolicy {

    public static final PlanetGrowthPolicy UNRESTRICTED = new PlanetGrowthPolicy(true, true);
    public static final PlanetGrowthPolicy MACHINE_ONLY = new PlanetGrowthPolicy(false, false);

    public static final PlanetGrowthPolicy ATMOSPHERE_REQUIRED = new PlanetGrowthPolicy(false, false, true);

    private final boolean atmosphereRequired;
    private final boolean naturalGrowthAllowed;
    private final boolean playerBonemealAllowed;

    public PlanetGrowthPolicy(boolean naturalGrowthAllowed, boolean playerBonemealAllowed) {
        this(naturalGrowthAllowed, playerBonemealAllowed, false);
    }

    private PlanetGrowthPolicy(boolean naturalGrowthAllowed, boolean playerBonemealAllowed,
                               boolean atmosphereRequired) {
        this.atmosphereRequired = atmosphereRequired;
        this.naturalGrowthAllowed = naturalGrowthAllowed;
        this.playerBonemealAllowed = playerBonemealAllowed;
    }

    /** Uses the existing planet registry, so policy follows the planet when its dimension changes. */
    public static PlanetGrowthPolicy forDimension(int dimension) {
        Planetoid planet = Planetoid.PLANETOIDS.inverse().get(dimension);
        return planet == null ? UNRESTRICTED : planet.getGrowthPolicy();
    }

    public boolean requiresAtmosphere() {
        return atmosphereRequired;
    }

    public boolean allowsNaturalGrowth() {
        return allowsNaturalGrowth(false);
    }

    public boolean allowsNaturalGrowth(boolean oxygenated) {
        return naturalGrowthAllowed || (atmosphereRequired && oxygenated);
    }

    public boolean allowsPlayerBonemeal() {
        return allowsPlayerBonemeal(false);
    }

    public boolean allowsPlayerBonemeal(boolean oxygenated) {
        return playerBonemealAllowed || (atmosphereRequired && oxygenated);
    }

    public boolean suppressRandomTick(Block block) {
        return suppressRandomTick(block, false);
    }

    public boolean suppressRandomTick(Block block, boolean oxygenated) {
        return !allowsNaturalGrowth(oxygenated) && isVegetation(block);
    }

    public boolean suppressBonemeal(EntityPlayer player) {
        return suppressBonemeal(player, false);
    }

    public boolean suppressBonemeal(EntityPlayer player, boolean oxygenated) {
        return !allowsPlayerBonemeal(oxygenated) && !(player instanceof FakePlayer);
    }

    public static boolean isVegetation(Block block) {
        // Forge's plant interface also covers reeds and cactus, which are not IGrowable.
        // Chorus, vines and mycelium lack both interfaces in 1.12.2.
        return block instanceof IGrowable || block instanceof IPlantable ||
                block instanceof BlockMycelium || block instanceof BlockVine || block instanceof BlockChorusFlower;
    }
}
