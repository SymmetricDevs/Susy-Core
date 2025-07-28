package supersymmetry.api.geology;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import rtg.api.util.noise.OpenSimplexNoise;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class PlateMap {
    public static PlateMap instance;
    public Long2ObjectOpenHashMap<PlateRegion> regions;
    public static int REGION_SIZE = 8192;
    public static int MIN_PLATES = 6;
    public static int MAX_PLATES = 10;
    public static int MIN_PLATE_DISTANCE = 1000;
    private long worldSeed;
    private static double BOUNDARY_THRESHOLD = 64;
    public OpenSimplexNoise noise;

    public PlateMap(long worldSeed) {
        this.worldSeed = worldSeed;
        this.regions = new Long2ObjectOpenHashMap<>();
        this.noise = new OpenSimplexNoise(worldSeed);
    }

    public PlateRegion getRegion(int regionX, int regionZ) {
        long key = packInts(regionX, regionZ);
        long seed = computeRegionSeed(regionX, regionZ);
        Random rand = new Random(seed);
        return regions.computeIfAbsent(key, k -> new PlateRegion(seed, rand.nextInt(MAX_PLATES - MIN_PLATES) + MIN_PLATES, regionX * REGION_SIZE, regionZ * REGION_SIZE, REGION_SIZE, MIN_PLATE_DISTANCE));
    }

    public List<PlateRegion> getSurroundingRegions(int regionX, int regionZ) {
        List<PlateRegion> result = new ArrayList<>();
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                result.add(getRegion(regionX + dx, regionZ + dz));
            }
        }
        return result;
    }

    public Plate getPlateAt(double x, double z) {
        List<PlateRegion> surroundingRegions = getSurroundingRegions((int) x / REGION_SIZE, (int) z / REGION_SIZE);
        List<Plate> possiblePlates = new ArrayList<>();
        for (PlateRegion region :
                surroundingRegions) {
            possiblePlates.addAll(region.plates);
        }

        return possiblePlates.stream().min(Comparator.comparingDouble(
                plate -> plate.center.distanceSquared(x, z)
        )).get();
    }

    public Plate getNeighboringPlate(double x, double z) {
        List<PlateRegion> surroundingRegions = getSurroundingRegions((int) x / REGION_SIZE, (int) z / REGION_SIZE);
        List<Plate> possiblePlates = new ArrayList<>();
        for (PlateRegion region :
                surroundingRegions) {
            possiblePlates.addAll(region.plates);
        }

        Plate currentPlate = possiblePlates.stream().min(Comparator.comparingDouble(
                plate -> plate.center.distanceSquared(x, z)
        )).get();
        possiblePlates.remove(currentPlate);

        Plate neighbour = possiblePlates.stream().min(Comparator.comparingDouble(
                plate -> plate.center.distanceSquared(x, z)
        )).get();



        return neighbour;
    }

    private Long packInts(int x, int z) {
        return ((long)x << 32) | (z & 0xFFFFFFFFL);
    }

    private long computeRegionSeed(int regionX, int regionZ) {
        long seed = this.worldSeed;
        seed ^= regionX;
        seed ^= regionZ;
        return seed;
    }

    public TectonicZone getZone(int x, int z) {
        Plate a = getPlateAt(x, z);
        Plate b = getNeighboringPlate(x, z);

        PlateBoundary boundary = a.getOrCreateBoundary(b);

        Plate closest = boundary.closerPlate(x, z, BOUNDARY_THRESHOLD);

        if (closest != null) {
            if (closest.type == Plate.Type.CONTINENTAL) return TectonicZone.CRATON;
            return TectonicZone.ABYSSAL_PLAIN;
            //return TectonicZone.CRATON;
        }

        if (boundary.relativeMotion > 0.5f) {
            // Converging
            if (a.type == Plate.Type.CONTINENTAL && b.type == Plate.Type.CONTINENTAL) return TectonicZone.OROGENIC_BELT;
            if (a.type == Plate.Type.OCEANIC || b.type == Plate.Type.OCEANIC) return TectonicZone.VOLCANIC_ARC;
        } else if (boundary.relativeMotion < -0.5f) {
            // Diverging
            return TectonicZone.RIFT_ZONE;
        } else {
            // Transform or oblique
            return TectonicZone.SHEAR_ZONE;
        }

        return TectonicZone.SEDIMENTARY_BASIN; // fallback
    }

}
