package supersymmetry.api.space;

import org.lwjgl.util.vector.Vector3f;
import java.util.ArrayList;
import java.util.List;

public class QuadSphere {

    public static class Vertex {
        public final float x, y, z;     // position
        public final float nx, ny, nz;  // normal (cubemap direction)

        public Vertex(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;

            // normalize for cubemap lookup
            float len = (float)Math.sqrt(x*x + y*y + z*z);
            this.nx = x / len;
            this.ny = y / len;
            this.nz = z / len;
        }
    }

    private final List<Vertex> vertices = new ArrayList<>();
    private final List<int[]> quads = new ArrayList<>();   // 4 indices per quad
    private final List<int[]> triangles = new ArrayList<>(); // 3 indices per triangle

    public final int subdivisions;

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
        // 6 cube faces
        makeFace(new Vector3f( 1,  0,  0)); // +X
        makeFace(new Vector3f(-1,  0,  0)); // -X
        makeFace(new Vector3f( 0,  1,  0)); // +Y
        makeFace(new Vector3f( 0, -1,  0)); // -Y
        makeFace(new Vector3f( 0,  0,  1)); // +Z
        makeFace(new Vector3f( 0,  0, -1)); // -Z
    }

    // Make one cube face subdivided into quads
    private void makeFace(Vector3f normal) {
        Vector3f axisA = perpendicular(normal);
        Vector3f axisB = Vector3f.cross(normal, axisA, null);

        int startIndex = vertices.size();

        int N = subdivisions;

        float step = 2f / N;

        // Generate vertices
        for (int y = 0; y <= N; y++) {
            for (int x = 0; x <= N; x++) {

                float sx = -1f + x * step;
                float sy = -1f + y * step;

                Vector3f p = new Vector3f(normal);
                p.scale(1f);

                Vector3f aa = new Vector3f(axisA);
                aa.scale(sx);

                Vector3f bb = new Vector3f(axisB);
                bb.scale(sy);

                Vector3f.add(p, aa, p);
                Vector3f.add(p, bb, p);

                // project cube → sphere
                normalize(p);

                vertices.add(new Vertex(p.x, p.y, p.z));
            }
        }

        // Build quads + triangles
        for (int y = 0; y < N; y++) {
            for (int x = 0; x < N; x++) {

                int i0 = startIndex + y * (N + 1) + x;
                int i1 = i0 + 1;
                int i2 = i0 + (N + 1);
                int i3 = i2 + 1;

                quads.add(new int[]{i0, i1, i3, i2});

                // Optional triangles
                triangles.add(new int[]{i0, i1, i3});
                triangles.add(new int[]{i0, i3, i2});
            }
        }
    }

    // Find a consistent perpendicular vector
    private static Vector3f perpendicular(Vector3f v) {
        if (Math.abs(v.x) < 0.5f)
            return new Vector3f(0, -v.z, v.y);
        else
            return new Vector3f(-v.y, v.x, 0);
    }

    private static void normalize(Vector3f v) {
        float l = (float)Math.sqrt(v.x*v.x + v.y*v.y + v.z*v.z);
        v.x /= l;
        v.y /= l;
        v.z /= l;
    }
}
