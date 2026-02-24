package supersymmetry.api.space;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;

public class QuadSphere {

    public static class Vertex {

        public float x, y, z;      // position (on unit sphere)
        public float nx, ny, nz;   // accumulated normal (averaged across shared quads)

        // Internal accumulator — not part of the public normal yet
        float accNx, accNy, accNz;
        int accCount;

        public Vertex(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
            // Initialize normal as the sphere surface normal (radial direction).
            // This is already correct for a sphere, but we'll average face normals
            // across shared vertices so seams stay smooth.
            float len = (float) Math.sqrt(x * x + y * y + z * z);
            this.nx = x / len;
            this.ny = y / len;
            this.nz = z / len;
        }

        /** Accumulate a face normal contribution. Call finalizeNormal() when done. */
        void accumulateNormal(float fnx, float fny, float fnz) {
            accNx += fnx;
            accNy += fny;
            accNz += fnz;
            accCount++;
        }

        /** Average all accumulated face normals and write to nx/ny/nz. */
        void finalizeNormal() {
            if (accCount == 0) return;
            float len = (float) Math.sqrt(accNx * accNx + accNy * accNy + accNz * accNz);
            if (len > 1e-6f) {
                nx = accNx / len;
                ny = accNy / len;
                nz = accNz / len;
            }
        }
    }

    private final List<Vertex> vertices = new ArrayList<>();
    private final List<int[]> quads = new ArrayList<>();
    private final List<int[]> triangles = new ArrayList<>();

    public final int subdivisions;

    /**
     * Key for vertex deduplication: three floats quantised to a fixed precision
     * so that floating-point positions that should be identical compare equal.
     */
    private final Map<Long, Integer> vertexCache = new HashMap<>();

    public QuadSphere(int subdivisions) {
        this.subdivisions = Math.max(1, subdivisions);
        build();
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public List<int[]> getQuads() {
        return quads;
    }

    public List<int[]> getTriangles() {
        return triangles;
    }

    // ===========================================
    // Core quad-sphere generation
    // ===========================================
    private void build() {
        makeFace(new Vector3f(1, 0, 0));
        makeFace(new Vector3f(-1, 0, 0));
        makeFace(new Vector3f(0, 1, 0));
        makeFace(new Vector3f(0, -1, 0));
        makeFace(new Vector3f(0, 0, 1));
        makeFace(new Vector3f(0, 0, -1));

        smoothNormals();
    }

    private void makeFace(Vector3f normal) {
        Vector3f axisA = perpendicular(normal);
        Vector3f axisB = Vector3f.cross(normal, axisA, null);

        int N = subdivisions;
        float step = 2f / N;

        // Grid of vertex indices for this face
        int[][] grid = new int[(N + 1)][(N + 1)];

        for (int y = 0; y <= N; y++) {
            for (int x = 0; x <= N; x++) {
                float sx = -1f + x * step;
                float sy = -1f + y * step;

                Vector3f p = new Vector3f(normal);
                Vector3f.add(p, scaled(axisA, sx), p);
                Vector3f.add(p, scaled(axisB, sy), p);
                normalize(p);

                grid[y][x] = getOrCreateVertex(p.x, p.y, p.z);
            }
        }

        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                int i0 = grid[y][x];
                int i1 = grid[y][x + 1];
                int i2 = grid[y + 1][x];
                int i3 = grid[y + 1][x + 1];

                quads.add(new int[] { i0, i1, i3, i2 });
                triangles.add(new int[] { i0, i1, i3 });
                triangles.add(new int[] { i0, i3, i2 });
            }
        }
    }

    /**
     * For every quad, compute its face normal and accumulate it onto each
     * of the quad's four vertices. Then average and normalize per vertex.
     *
     * On a perfect unit sphere every vertex normal is already radial, so the
     * result will be identical to the sphere normal — but this method also
     * works correctly for displaced/heightmapped variants.
     */
    private void smoothNormals() {
        for (int[] quad : quads) {
            Vertex v0 = vertices.get(quad[0]);
            Vertex v1 = vertices.get(quad[1]);
            Vertex v2 = vertices.get(quad[2]);
            Vertex v3 = vertices.get(quad[3]);

            // Face normal from the two diagonals (cross product of diagonals
            // gives a stable average normal for a planar or near-planar quad)
            float dx0 = v2.x - v0.x, dy0 = v2.y - v0.y, dz0 = v2.z - v0.z; // diagonal 0→2
            float dx1 = v3.x - v1.x, dy1 = v3.y - v1.y, dz1 = v3.z - v1.z; // diagonal 1→3

            float fnx = dy0 * dz1 - dz0 * dy1;
            float fny = dz0 * dx1 - dx0 * dz1;
            float fnz = dx0 * dy1 - dy0 * dx1;

            v0.accumulateNormal(fnx, fny, fnz);
            v1.accumulateNormal(fnx, fny, fnz);
            v2.accumulateNormal(fnx, fny, fnz);
            v3.accumulateNormal(fnx, fny, fnz);
        }

        for (Vertex v : vertices) {
            v.finalizeNormal();
        }
    }

    // ===========================================
    // Vertex deduplication
    // ===========================================

    /**
     * Quantise a float to a fixed grid (1/65536 units) and pack three values
     * into a single long so they can be used as a HashMap key.
     *
     * Range: [-2, 2] fits comfortably inside a signed 16-bit slot when scaled.
     */
    private static long vertexKey(float x, float y, float z) {
        int qx = Math.round(x * 65536f);
        int qy = Math.round(y * 65536f);
        int qz = Math.round(z * 65536f);
        // pack three 20-bit signed values — sufficient for our coordinate range
        return ((long) (qx & 0xFFFFF) << 40) | ((long) (qy & 0xFFFFF) << 20) | (long) (qz & 0xFFFFF);
    }

    private int getOrCreateVertex(float x, float y, float z) {
        long key = vertexKey(x, y, z);
        Integer existing = vertexCache.get(key);
        if (existing != null) return existing;

        int index = vertices.size();
        vertices.add(new Vertex(x, y, z));
        vertexCache.put(key, index);
        return index;
    }

    // ===========================================
    // Helpers
    // ===========================================

    private static Vector3f scaled(Vector3f v, float s) {
        return new Vector3f(v.x * s, v.y * s, v.z * s);
    }

    private static Vector3f perpendicular(Vector3f v) {
        return (Math.abs(v.x) < 0.5f) ? new Vector3f(0, -v.z, v.y) : new Vector3f(-v.y, v.x, 0);
    }

    private static void normalize(Vector3f v) {
        float l = (float) Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
        v.x /= l;
        v.y /= l;
        v.z /= l;
    }
}
