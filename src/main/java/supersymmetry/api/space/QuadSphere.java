package supersymmetry.api.space;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.util.vector.Vector3f;

public class QuadSphere {

    public static class Vertex {

        public float x, y, z;
        public float nx, ny, nz;

        float accNx, accNy, accNz;
        int accCount;

        public Vertex(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
            float len = (float) Math.sqrt(x * x + y * y + z * z);
            this.nx = x / len;
            this.ny = y / len;
            this.nz = z / len;
        }

        void accumulateNormal(float fnx, float fny, float fnz) {
            accNx += fnx;
            accNy += fny;
            accNz += fnz;
            accCount++;
        }

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
    private final List<float[][]> quadUVs = new ArrayList<>();
    private final List<Integer> quadFace = new ArrayList<>();

    /** Precomputed: faceQuadIndices.get(f) = list of quad indices belonging to face f */
    private final List<List<Integer>> faceQuadIndices = new ArrayList<>();

    public final int subdivisions;

    private final Map<Long, Integer> vertexCache = new HashMap<>();
    private int currentFace = 0;

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

    public List<Integer> getQuadFaces() {
        return quadFace;
    }

    public List<float[][]> getQuadUVs() {
        return quadUVs;
    }

    /** Precomputed per-face quad index lists — use this in the renderer instead of getQuadFaces(). */
    public List<List<Integer>> getFaceQuadIndices() {
        return faceQuadIndices;
    }

    // ------------------------------------------------------------------

    private void build() {
        for (int i = 0; i < 6; i++) faceQuadIndices.add(new ArrayList<>());

        makeFace(new Vector3f(1, 0, 0), new Vector3f(0, 0, -1), new Vector3f(0, -1, 0)); // +X
        makeFace(new Vector3f(-1, 0, 0), new Vector3f(0, 0, 1), new Vector3f(0, -1, 0)); // -X
        makeFace(new Vector3f(0, 1, 0), new Vector3f(1, 0, 0), new Vector3f(0, 0, 1)); // +Y
        makeFace(new Vector3f(0, -1, 0), new Vector3f(1, 0, 0), new Vector3f(0, 0, -1)); // -Y
        makeFace(new Vector3f(0, 0, 1), new Vector3f(1, 0, 0), new Vector3f(0, -1, 0)); // +Z
        makeFace(new Vector3f(0, 0, -1), new Vector3f(-1, 0, 0), new Vector3f(0, -1, 0)); // -Z

        smoothNormals();
    }

    private void makeFace(Vector3f normal, Vector3f axisA, Vector3f axisB) {
        int N = subdivisions;
        float step = 2f / N;
        int[][] grid = new int[N + 1][N + 1];

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

        float eps = 0.0005f;

        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {
                int i0 = grid[y][x]; // BL
                int i1 = grid[y][x + 1]; // BR
                int i2 = grid[y + 1][x]; // TL
                int i3 = grid[y + 1][x + 1]; // TR

                int qi = quads.size();
                quads.add(new int[] { i0, i1, i3, i2 }); // BL BR TR TL
                quadFace.add(currentFace);
                faceQuadIndices.get(currentFace).add(qi);

                triangles.add(new int[] { i0, i1, i3 });
                triangles.add(new int[] { i0, i3, i2 });

                float u0 = clamp((float) x / N, eps);
                float u1 = clamp((float) (x + 1) / N, eps);
                float v0 = clamp((float) y / N, eps);
                float v1 = clamp((float) (y + 1) / N, eps);

                // Corner order matches quad: BL BR TR TL
                quadUVs.add(new float[][] {
                        { u0, v0 }, // BL
                        { u1, v0 }, // BR
                        { u1, v1 }, // TR
                        { u0, v1 }, // TL
                });
            }
        }
        currentFace++;
    }

    private static float clamp(float t, float eps) {
        return Math.max(eps, Math.min(1f - eps, t));
    }

    private void smoothNormals() {
        for (int[] quad : quads) {
            Vertex v0 = vertices.get(quad[0]);
            Vertex v1 = vertices.get(quad[1]);
            Vertex v2 = vertices.get(quad[2]);
            Vertex v3 = vertices.get(quad[3]);

            float dx0 = v2.x - v0.x, dy0 = v2.y - v0.y, dz0 = v2.z - v0.z;
            float dx1 = v3.x - v1.x, dy1 = v3.y - v1.y, dz1 = v3.z - v1.z;

            float fnx = dy0 * dz1 - dz0 * dy1;
            float fny = dz0 * dx1 - dx0 * dz1;
            float fnz = dx0 * dy1 - dy0 * dx1;

            v0.accumulateNormal(fnx, fny, fnz);
            v1.accumulateNormal(fnx, fny, fnz);
            v2.accumulateNormal(fnx, fny, fnz);
            v3.accumulateNormal(fnx, fny, fnz);
        }
        for (Vertex v : vertices) v.finalizeNormal();
    }

    private static long vertexKey(float x, float y, float z) {
        int qx = Math.round(x * 65536f);
        int qy = Math.round(y * 65536f);
        int qz = Math.round(z * 65536f);
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

    private static Vector3f scaled(Vector3f v, float s) {
        return new Vector3f(v.x * s, v.y * s, v.z * s);
    }

    private static void normalize(Vector3f v) {
        float l = (float) Math.sqrt(v.x * v.x + v.y * v.y + v.z * v.z);
        v.x /= l;
        v.y /= l;
        v.z /= l;
    }
}
