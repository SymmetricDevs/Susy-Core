package supersymmetry.api.geology;

import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PlateRegion {
    public final List<Plate> plates;
    private double OCEANIC_RATIO = 0.7;
    public PlateRegion(long seed, int plateCount, double x, double z, double size, int minDist) {
        Random rand = new Random(seed);
        plates = new ArrayList<>();
        for (int i = 0; i < plateCount; i++) {
            double newX;
            double newZ;
            do {
                newX = rand.nextDouble() * size + x;
                newZ = rand.nextDouble() * size + z;
            } while (closest(newX, newZ) >= minDist * minDist);

            plates.add(new Plate(
                    newX,
                    newZ,
                    rand.nextDouble() > OCEANIC_RATIO ,
                    Vec3d.fromPitchYaw(0, rand.nextFloat() * 360)
            ));
        }
    }

    private double closest(double newX, double newZ) {
        return this.plates.isEmpty() ? 0 : this.plates.stream().mapToDouble(plate -> plate.center.distanceSquared(newX, newZ)).min().getAsDouble();
    }
}