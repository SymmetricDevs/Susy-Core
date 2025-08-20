package supersymmetry.api.geology;

import net.minecraft.init.Biomes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import rtg.util.ModCompat;

import javax.annotation.Nullable;

public class TectonicZone {
    public static TectonicZone CRATON = new TectonicZone("shield");
    public static TectonicZone DESERT = new TectonicZone(Biomes.DESERT);
    public static TectonicZone PLAINS = new TectonicZone(Biomes.PLAINS);
    public static TectonicZone SEDIMENTARY_BASIN = new TectonicZone("prairie");
    public static TectonicZone RIFT_ZONE = new TectonicZone("volcanic_island");
    public static TectonicZone VOLCANIC_ARC = new TectonicZone("rainforest");
    public static TectonicZone OROGENIC_BELT = new TectonicZone("alps");
    public static TectonicZone SHEAR_ZONE = new TectonicZone("crag");
    public static TectonicZone ABYSSAL_PLAIN = new TectonicZone("kelp_forest");

    public int biomeId;

    public TectonicZone(Biome biome) {
        this(Biome.getIdForBiome(biome));
    }

    public TectonicZone(int biomeId) {
        this.biomeId = biomeId;
    }

    public TectonicZone(String resloc) {
        this(getBiome(ModCompat.Mods.biomesoplenty.getResourceLocation(resloc)));
    }

    @Nullable
    private static Biome getBiome(final ResourceLocation resloc)
    {
        return ForgeRegistries.BIOMES.getValue(resloc);
    }
}
