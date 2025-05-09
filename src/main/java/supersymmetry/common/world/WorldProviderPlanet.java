package supersymmetry.common.world;

import net.minecraft.world.DimensionType;
import net.minecraft.world.WorldProvider;

public class WorldProviderPlanet extends WorldProvider {
    @Override
    public DimensionType getDimensionType() {
        return SuSyDimensions.planetType;
    }
}
