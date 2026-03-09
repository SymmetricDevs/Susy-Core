package supersymmetry.loaders;

import net.minecraftforge.fml.common.Loader;

import biomesoplenty.api.biome.BOPBiomes;
import biomesoplenty.api.biome.IExtendedBiome;

public class SuSyWorldLoader {

    public static void init() {
        if (Loader.isModLoaded("biomesoplenty")) { // CEu 2.9 removes the mod id anyway...
            BOPBiomes.REG_INSTANCE.getPresentBiomes().forEach(biome -> {
                IExtendedBiome actualBiome = BOPBiomes.REG_INSTANCE.getExtendedBiome(biome);
                if (actualBiome != null) {
                    actualBiome.getGenerationManager().removeGenerator("malachite");
                    actualBiome.getGenerationManager().removeGenerator("amber");
                    actualBiome.getGenerationManager().removeGenerator("amethyst");
                    actualBiome.getGenerationManager().removeGenerator("tanzanite");
                    actualBiome.getGenerationManager().removeGenerator("emerald");
                    actualBiome.getGenerationManager().removeGenerator("ruby");
                    actualBiome.getGenerationManager().removeGenerator("sapphire");
                    actualBiome.getGenerationManager().removeGenerator("peridot");
                    actualBiome.getGenerationManager().removeGenerator("topaz");
                }
            });
        }
    }
}
