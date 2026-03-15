package supersymmetry.api.util;

import net.minecraft.util.math.Vec3d;

public class FisherPlane {

    /**
     * @param grid 3D grid with two classes of points.
     * @return  Fisher discriminant normal vector (pointing from 0s to 1s)
     */
    public static Vec3d fisherNormal(boolean[][][] grid) {

        int n = grid.length;

        double sum1x=0, sum1y=0, sum1z=0;
        double sum0x=0, sum0y=0, sum0z=0;

        int c1 = 0, c0 = 0;

        // First pass: compute centroids
        for (int x=0;x<n;x++)
            for (int y=0;y<n;y++)
                for (int z=0;z<n;z++) {

                    if (grid[x][y][z]) {
                        sum1x += x;
                        sum1y += y;
                        sum1z += z;
                        c1++;
                    } else {
                        sum0x += x;
                        sum0y += y;
                        sum0z += z;
                        c0++;
                    }
                }

        if (c1 == 0 || c0 == 0)
            return new Vec3d(0,0,0);

        double m1x = sum1x / c1;
        double m1y = sum1y / c1;
        double m1z = sum1z / c1;

        double m0x = sum0x / c0;
        double m0y = sum0y / c0;
        double m0z = sum0z / c0;

        // Within-class scatter matrix
        double sxx=0, sxy=0, sxz=0;
        double syy=0, syz=0, szz=0;

        for (int x=0;x<n;x++)
            for (int y=0;y<n;y++)
                for (int z=0;z<n;z++) {

                    double dx, dy, dz;

                    if (grid[x][y][z]) {
                        dx = x - m1x;
                        dy = y - m1y;
                        dz = z - m1z;
                    } else {
                        dx = x - m0x;
                        dy = y - m0y;
                        dz = z - m0z;
                    }

                    sxx += dx*dx;
                    sxy += dx*dy;
                    sxz += dx*dz;
                    syy += dy*dy;
                    syz += dy*dz;
                    szz += dz*dz;
                }

        // Invert 3x3 matrix
        double det =
                sxx*(syy*szz - syz*syz)
                        - sxy*(sxy*szz - sxz*syz)
                        + sxz*(sxy*syz - sxz*syy);

        if (Math.abs(det) < 1e-12)
            return new Vec3d(0,0,0);

        double inv00 = (syy*szz - syz*syz)/det;
        double inv01 = (sxz*syz - sxy*szz)/det;
        double inv02 = (sxy*syz - sxz*syy)/det;

        double inv11 = (sxx*szz - sxz*sxz)/det;
        double inv12 = (sxz*sxy - sxx*syz)/det;

        double inv22 = (sxx*syy - sxy*sxy)/det;

        // mean difference
        double dx = m1x - m0x;
        double dy = m1y - m0y;
        double dz = m1z - m0z;

        // Fisher direction
        double wx = inv00*dx + inv01*dy + inv02*dz;
        double wy = inv01 *dx + inv11*dy + inv12*dz;
        double wz = inv02 *dx + inv12 *dy + inv22*dz;

        Vec3d normal = new Vec3d(wx, wy, wz);

        return normal.normalize();
    }

}
