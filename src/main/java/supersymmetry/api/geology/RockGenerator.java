package supersymmetry.api.geology;

import rtg.api.util.noise.OpenSimplexNoise;

public class RockGenerator {

    private final OpenSimplexNoise lithoNoise;
    private final OpenSimplexNoise tectonicNoise;

    public RockGenerator(long seed) {
        this.lithoNoise = new OpenSimplexNoise(seed ^ 0x514A);
        this.tectonicNoise = new OpenSimplexNoise(seed ^ 0xBEEF);
    }


    /*public ChunkLithologyData generateLithology(int worldX, int worldZ) {
        TectonicZone zone = getTectonicZone(worldX, worldZ);
        ChunkLithologyData data = new ChunkLithologyData();

        for (int y = 0; y < 256; y++) {
            RockType rock = pickRockForZone(zone, y, worldX, worldZ);
            data.setRockAtDepth(y, rock);
        }

        return data;
    }

    private RockType pickRockForZone(TectonicZone zone, int y, int x, int z) {
        double noise = lithoNoise.noise3D(x * 0.01, y * 0.01, z * 0.01);

        switch (zone) {
            case SEDIMENTARY_BASIN:
                if (y < 32) return noise > 0.3 ? RockType.SHALE : RockType.SANDSTONE;
                if (y < 64) return RockType.LIMESTONE;
                return noise > 0 ? RockType.SHALE : RockType.SILTSTONE;

            case CRATON:
                if (y < 48) return RockType.SHALE;
                if (y < 128) return noise > 0 ? RockType.GRANITE : RockType.GNEISS;
                return RockType.AMPHIBOLITE;

            case ORGENIC_BELT:
                if (y < 64) return noise > 0.2 ? RockType.SLATE : RockType.SHALE;
                return RockType.GNEISS;

            case RIFT_ZONE:
                if (y < 48) return RockType.BASALT;
                return noise > 0 ? RockType.GRANITE : RockType.GABBRO;

            case VOLCANIC_ARC:
                if (y < 32) return RockType.ANDESITE;
                return noise > 0 ? RockType.DACITE : RockType.RHYOLITE;
        }

        return RockType.UNKNOWN;
    }*/

    enum TectonicZone {
        CRATON,
        SEDIMENTARY_BASIN,
        OROGENIC_BELT,
        VOLCANIC_ARC,
        RIFT_ZONE
    }

}
