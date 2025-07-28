package supersymmetry.api.geology;

public class Vec2d {
    public double x;
    public double y;

    public Vec2d(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double dot(Vec2d other) {
        return this.x * other.x + this.y * other.y;
    }

    public Vec2d scale(double scalar) {
        return new Vec2d(this.x * scalar, this.y * scalar);
    }

    public Vec2d add(Vec2d other) {
        return new Vec2d(this.x + other.x, this.y + other.y);
    }

    public Vec2d sub(Vec2d other) {
        return new Vec2d(this.x - other.x, this.y - other.y);
    }

    public double norm() {
        return Math.sqrt(this.normSquared());
    }

    public double normSquared() {
        return this.x * this.x + this.y * this.y;
    }

    public double distanceSquared(Vec2d other) {
        return this.sub(other).normSquared();
    }

    public double distanceSquared(double x, double y) {
        return this.sub(new Vec2d(x,y)).normSquared();
    }

    public double distance(Vec2d other) {
        return this.sub(other).norm();
    }

    public Vec2d project(Vec2d other) {
        return this.scale(this.projectionScalar(other));
    }

    public double projectionScalar(Vec2d other) {
        return this.dot(other) / this.normSquared();
    }

    public Vec2d perp() {
        return new Vec2d(-this.y, this.x);
    }

    /**
     * @param a Point a.
     * @param b Point b.
     * @param c Point c.
     * @return Center of circle that goes through all points.
     * Done by calculating the intersection of two vectors perpendicular to the sides of the triangle defined by the points.
     */
    public static Vec2d circumcenter(Vec2d a, Vec2d b, Vec2d c) {
        Vec2d midAB = new Vec2d((a.x + b.x) / 2.0, (a.y + b.y) / 2.0);
        Vec2d midAC = new Vec2d((a.x + c.x) / 2.0, (a.y + c.y) / 2.0);

        Vec2d dirAB = b.sub(a);
        Vec2d dirAC = c.sub(a);

        Vec2d perpAB = dirAB.perp();
        Vec2d perpAC = dirAC.perp();

        double a1 = perpAB.x;
        double b1 = -perpAC.x;
        double c1 = midAC.x - midAB.x;

        double a2 = perpAB.y;
        double b2 = -perpAC.y;
        double c2 = midAC.y - midAB.y;

        double det = a1 * b2 - a2 * b1;

        if (Math.abs(det) < 1e-10) {
            // The points are collinear or too close to being collinear
            throw new IllegalArgumentException("Cannot compute circumcenter of collinear points");
        }

        double t1 = (c1 * b2 - c2 * b1) / det;

        // circumcenter = midAB + perpAB * t1
        return midAB.add(perpAB.scale(t1));
    }

    public Vec2d normalize() {
        return this.scale(1/this.norm());
    }
}
