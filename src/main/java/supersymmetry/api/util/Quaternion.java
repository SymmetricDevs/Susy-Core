package supersymmetry.api.util;

import java.util.Objects;

import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;

// immutable just because i saw someone else implement it like that, so i may be wrong
public final class Quaternion {

    public static final Quaternion IDENTITY = new Quaternion();

    public static Quaternion fromEulerZYX(double yaw, double pitch, double roll) {
        double cy = Math.cos(yaw * 0.5);
        double sy = Math.sin(yaw * 0.5);
        double cp = Math.cos(pitch * 0.5);
        double sp = Math.sin(pitch * 0.5);
        double cr = Math.cos(roll * 0.5);
        double sr = Math.sin(roll * 0.5);

        double w = cr * cp * cy + sr * sp * sy;
        double x = sr * cp * cy - cr * sp * sy;
        double y = cr * sp * cy + sr * cp * sy;
        double z = cr * cp * sy - sr * sp * cy;

        return new Quaternion(w, x, y, z).normalised();
    }

    public static Quaternion fromAxisAngle(Vec3d axis, double angle) {
        double len = axis.length();
        if (len < 1e-40) return IDENTITY;
        double s = Math.sin(angle * 0.5) / len;
        return new Quaternion(Math.cos(angle * 0.5), axis.x * s, axis.y * s, axis.z * s);
    }

    public final double w;

    public final double x;

    public final double y;

    public final double z;

    // w=1 x=0 y=0 z=0
    public Quaternion() {
        this.w = 1;
        this.x = 0;
        this.y = 0;
        this.z = 0;
    }

    public Quaternion(double w, double x, double y, double z) {
        this.w = w;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Quaternion(org.lwjgl.util.vector.Quaternion q) {
        this.w = q.w;
        this.x = q.x;
        this.y = q.y;
        this.z = q.z;
    }

    public org.lwjgl.util.vector.Quaternion toLWJGL() {
        return new org.lwjgl.util.vector.Quaternion(
                (float) this.x, (float) this.y, (float) this.z, (float) this.w);
    }

    public double magnitude() {
        return Math.sqrt(w * w + x * x + y * y + z * z);
    }

    public Quaternion normalised() {
        double mag = magnitude();
        return new Quaternion(w / mag, x / mag, y / mag, z / mag);
    }

    public Quaternion conjugate() {
        return new Quaternion(w, -x, -y, -z);
    }

    public Quaternion multiply(Quaternion other) {
        double nw = w * other.w - x * other.x - y * other.y - z * other.z;
        double nx = w * other.x + x * other.w + y * other.z - z * other.y;
        double ny = w * other.y - x * other.z + y * other.w + z * other.x;
        double nz = w * other.z + x * other.y - y * other.x + z * other.w;
        return new Quaternion(nw, nx, ny, nz);
    }

    public double dot(Quaternion other) {
        return w * other.w + x * other.x + y * other.y + z * other.z;
    }

    public Vec3d rotatePoint(Vec3d point) {
        return rotatePoint(point, Vec3d.ZERO);
    }

    public Vec3d rotatePoint(Vec3d point, Vec3d center) {
        double px = point.x - center.x;
        double py = point.y - center.y;
        double pz = point.z - center.z;
        Quaternion pQuat = new Quaternion(0, px, py, pz);
        Quaternion q = this.normalised();
        Quaternion qConj = q.conjugate();
        Quaternion rotated = q.multiply(pQuat).multiply(qConj);
        return new Vec3d(rotated.x + center.x, rotated.y + center.y, rotated.z + center.z);
    }

    public AxisAlignedBB rotateAABB(AxisAlignedBB box) {
        double minX = Double.POSITIVE_INFINITY;
        double minY = Double.POSITIVE_INFINITY;
        double minZ = Double.POSITIVE_INFINITY;
        double maxX = Double.NEGATIVE_INFINITY;
        double maxY = Double.NEGATIVE_INFINITY;
        double maxZ = Double.NEGATIVE_INFINITY;
        for (int cx = 0; cx <= 1; cx++) {
            for (int cy = 0; cy <= 1; cy++) {
                for (int cz = 0; cz <= 1; cz++) {
                    Vec3d corner = new Vec3d(cx == 0 ? box.minX : box.maxX, cy == 0 ? box.minY : box.maxY,
                            cz == 0 ? box.minZ : box.maxZ);
                    Vec3d r = rotatePoint(corner);
                    minX = Math.min(minX, r.x);
                    minY = Math.min(minY, r.y);
                    minZ = Math.min(minZ, r.z);
                    maxX = Math.max(maxX, r.x);
                    maxY = Math.max(maxY, r.y);
                    maxZ = Math.max(maxZ, r.z);
                }
            }
        }
        return new AxisAlignedBB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    // this*(1-t)->other*t
    public Quaternion lerp(Quaternion other, double t) {
        double tt = 1 - t;
        return new Quaternion(
                this.w * tt + other.w * t,
                this.x * tt + other.x * t,
                this.y * tt + other.y * t,
                this.z * tt + other.z * t);
    }

    // spherical linear interpolation where t is 0..1
    public Quaternion slerp(Quaternion other, double t) {
        if (t <= 0.0) return this;
        if (t >= 1.0) return other;

        double dot = this.dot(other);

        Quaternion q2 = other;
        if (dot < 0.0) {
            q2 = new Quaternion(-other.w, -other.x, -other.y, -other.z);
            dot = -dot;
        }

        final double DOT_THRESHOLD = 0.9999;
        if (dot > DOT_THRESHOLD) {
            double w = this.w + t * (q2.w - this.w);
            double x = this.x + t * (q2.x - this.x);
            double y = this.y + t * (q2.y - this.y);
            double z = this.z + t * (q2.z - this.z);
            return new Quaternion(w, x, y, z).normalised();
        }

        double theta_0 = Math.acos(dot);
        double theta = theta_0 * t;
        double sin_theta = Math.sin(theta);
        double sin_theta_0 = Math.sin(theta_0);

        double s1 = Math.cos(theta) - dot * sin_theta / sin_theta_0;
        double s2 = sin_theta / sin_theta_0;

        double w = s1 * this.w + s2 * q2.w;
        double x = s1 * this.x + s2 * q2.x;
        double y = s1 * this.y + s2 * q2.y;
        double z = s1 * this.z + s2 * q2.z;

        return new Quaternion(w, x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Quaternion)) return false;
        Quaternion q = (Quaternion) o;
        return w == q.w && x == q.x && y == q.y && z == q.z;
    }

    public boolean RoughlyEquals(Quaternion other, double epsilon) {
        return 1.0 - Math.abs(this.dot(other)) < epsilon;
    }

    @Override
    public int hashCode() {
        return Objects.hash(w, x, y, z);
    }

    @Override
    public String toString() {
        return String.format("quaternion {w=%.6f, x=%.6f, y=%.6f, z=%.6f}", w, x, y, z);
    }

    public double getW() {
        return w;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double[][] toMatrix3x3() {
        double w = this.w, x = this.x, y = this.y, z = this.z;

        double xx = x * x, yy = y * y, zz = z * z;
        double xy = x * y, xz = x * z, yz = y * z;
        double wx = w * x, wy = w * y, wz = w * z;

        double[][] m = new double[3][3];

        m[0][0] = 1.0 - 2.0 * (yy + zz);
        m[0][1] = 2.0 * (xy - wz);
        m[0][2] = 2.0 * (xz + wy);

        m[1][0] = 2.0 * (xy + wz);
        m[1][1] = 1.0 - 2.0 * (xx + zz);
        m[1][2] = 2.0 * (yz - wx);

        m[2][0] = 2.0 * (xz - wy);
        m[2][1] = 2.0 * (yz + wx);
        m[2][2] = 1.0 - 2.0 * (xx + yy);

        return m;
    }

    public double[] toMatrix3x3Flat() {
        double w = this.w, x = this.x, y = this.y, z = this.z;

        double xx = x * x, yy = y * y, zz = z * z;
        double xy = x * y, xz = x * z, yz = y * z;
        double wx = w * x, wy = w * y, wz = w * z;

        double[] m = new double[9];

        m[0] = 1.0 - 2.0 * (yy + zz);
        m[1] = 2.0 * (xy - wz);
        m[2] = 2.0 * (xz + wy);

        m[3] = 2.0 * (xy + wz);
        m[4] = 1.0 - 2.0 * (xx + zz);
        m[5] = 2.0 * (yz - wx);

        m[6] = 2.0 * (xz - wy);
        m[7] = 2.0 * (yz + wx);
        m[8] = 1.0 - 2.0 * (xx + yy);

        return m;
    }

    public double[][] toMatrix4x4() {
        double w = this.w, x = this.x, y = this.y, z = this.z;

        double xx = x * x, yy = y * y, zz = z * z;
        double xy = x * y, xz = x * z, yz = y * z;
        double wx = w * x, wy = w * y, wz = w * z;

        double[][] m = new double[4][4];

        m[0][0] = 1.0 - 2.0 * (yy + zz);
        m[0][1] = 2.0 * (xy - wz);
        m[0][2] = 2.0 * (xz + wy);

        m[1][0] = 2.0 * (xy + wz);
        m[1][1] = 1.0 - 2.0 * (xx + zz);
        m[1][2] = 2.0 * (yz - wx);

        m[2][0] = 2.0 * (xz - wy);
        m[2][1] = 2.0 * (yz + wx);
        m[2][2] = 1.0 - 2.0 * (xx + yy);

        m[0][3] = 0.0;
        m[1][3] = 0.0;
        m[2][3] = 0.0;
        m[3][0] = 0.0;
        m[3][1] = 0.0;
        m[3][2] = 0.0;
        m[3][3] = 1.0;

        return m;
    }

    public double[] toMatrix4x4Flat() {
        double w = this.w, x = this.x, y = this.y, z = this.z;

        double xx = x * x, yy = y * y, zz = z * z;
        double xy = x * y, xz = x * z, yz = y * z;
        double wx = w * x, wy = w * y, wz = w * z;

        double[] m = new double[16];

        m[0] = 1.0 - 2.0 * (yy + zz);
        m[1] = 2.0 * (xy - wz);
        m[2] = 2.0 * (xz + wy);

        m[4] = 2.0 * (xy + wz);
        m[5] = 1.0 - 2.0 * (xx + zz);
        m[6] = 2.0 * (yz - wx);

        m[8] = 2.0 * (xz - wy);
        m[9] = 2.0 * (yz + wx);
        m[10] = 1.0 - 2.0 * (xx + yy);

        m[3] = 0.0f;
        m[7] = 0.0f;
        m[11] = 0.0f;
        m[12] = 0.0f;
        m[13] = 0.0f;
        m[14] = 0.0f;
        m[15] = 1.0f;
        return m;
    }
}
