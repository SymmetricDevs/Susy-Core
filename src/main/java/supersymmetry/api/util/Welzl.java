package supersymmetry.api.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Welzl {

    public static double computeMinimalRadius(List<Point2D> points) {
        if (points.isEmpty()) {
            return 0;
        }

        List<Point2D> shuffled = new ArrayList<>(points);
        // Fortunately this only really needs doing once.
        Collections.shuffle(shuffled, ThreadLocalRandom.current());
        return welzl(shuffled, new ArrayList<>(), shuffled.size()).radius;
    }

    private static Circle welzl(List<Point2D> points, List<Point2D> boundary, int size) {
        Circle circle;
        if (size == 0 || boundary.size() == 3) {
            circle = circleFromBoundary(boundary);
        } else {
            Point2D p = points.get(size - 1);
            circle = welzl(points, boundary, size - 1);
            if (circle == null || !circle.contains(p)) {
                boundary.add(p);
                circle = welzl(points, boundary, size - 1);
                boundary.remove(boundary.size() - 1);
            }
        }
        return circle;
    }

    private static Circle circleFromBoundary(List<Point2D> boundary) {
        switch (boundary.size()) {
            case 0:
                return new Circle(0, 0, 0);
            case 1:
                Point2D p = boundary.get(0);
                return new Circle(p.x, p.z, 0);
            case 2:
                return circleFromTwoPoints(boundary.get(0), boundary.get(1));
            case 3:
                return circleFromThreePoints(boundary.get(0), boundary.get(1), boundary.get(2));
            default:
                throw new IllegalStateException("Boundary cannot have more than 3 points");
        }
    }

    private static Circle circleFromTwoPoints(Point2D a, Point2D b) {
        double centerX = (a.x + b.x) / 2.0d;
        double centerZ = (a.z + b.z) / 2.0d;
        double radius = Math.hypot(a.x - centerX, a.z - centerZ);
        return new Circle(centerX, centerZ, radius);
    }

    private static Circle circleFromThreePoints(Point2D a, Point2D b, Point2D c) {
        double d = 2 * (a.x * (b.z - c.z) + b.x * (c.z - a.z) + c.x * (a.z - b.z));
        if (Math.abs(d) < EPSILON) {
            return circleFromCollinearPoints(a, b, c);
        }

        double ax2 = a.x * a.x + a.z * a.z;
        double bx2 = b.x * b.x + b.z * b.z;
        double cx2 = c.x * c.x + c.z * c.z;

        double centerX = (ax2 * (b.z - c.z) + bx2 * (c.z - a.z) + cx2 * (a.z - b.z)) / d;
        double centerZ = (ax2 * (c.x - b.x) + bx2 * (a.x - c.x) + cx2 * (b.x - a.x)) / d;
        double radius = Math.hypot(centerX - a.x, centerZ - a.z);
        return new Circle(centerX, centerZ, radius);
    }

    private static Circle circleFromCollinearPoints(Point2D a, Point2D b, Point2D c) {
        Circle circleAB = circleFromTwoPoints(a, b);
        Circle circleAC = circleFromTwoPoints(a, c);
        Circle circleBC = circleFromTwoPoints(b, c);

        Circle maxCircle = circleAB;
        if (circleAC.radius > maxCircle.radius) {
            maxCircle = circleAC;
        }
        if (circleBC.radius > maxCircle.radius) {
            maxCircle = circleBC;
        }
        return maxCircle;
    }

    private static class Circle {

        private static final double EPSILON = 1e-9;
        final double x;
        final double z;
        final double radius;

        Circle(double x, double z, double radius) {
            this.x = x;
            this.z = z;
            this.radius = Math.max(radius, 0);
        }

        boolean contains(Point2D point) {
            double dx = point.x - x;
            double dz = point.z - z;
            return dx * dx + dz * dz <= radius * radius + EPSILON;
        }
    }

    public record Point2D(double x, double z) {}

    private static final double EPSILON = 1e-9;
}
