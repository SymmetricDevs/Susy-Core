package supersymmetry.api.geology;

import climateControl.genLayerPack.GenLayerPack;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerTectonicPlates extends GenLayerPack {
    public GenLayerTectonicPlates(long seed, GenLayer parent) {
        super(seed);
        this.parent = parent;
        if(PlateMap.instance == null) PlateMap.instance = new PlateMap(seed);
    }

    @Override
    public int[] getInts(int x, int z, int width, int height) {
        int[] out = IntCache.getIntCache(width * height);
        for (int dz = 0; dz < height; dz++) {
            for (int dx = 0; dx < width; dx++) {
                int px = x + dx;
                int pz = z + dz;

                int biomeId = PlateMap.instance.getZone(px * 4, pz * 4).biomeId;
                out[dx + dz * width] = biomeId;
            }
        }
        return out;
    }
}
