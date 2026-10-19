package supersymmetry.client.molecule;

/* Adapted from gregtech-molecule-drawings by Ruben Vergauwen. SPDX-License-Identifier: LGPL-3.0-only */
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class Benzene implements MoleculeElement {

    private final int[] indices;
    private final float x0, y0, x1, y1, angle;

    Benzene(int[] indices, float x0, float y0, float x1, float y1, float angle) {
        this.indices = indices;
        this.x0 = x0;
        this.y0 = y0;
        this.x1 = x1;
        this.y1 = y1;
        this.angle = angle;
    }

    public int[] getIndices() {
        return indices.clone();
    }

    public float getX0() {
        return x0;
    }

    public float getY0() {
        return y0;
    }

    public float getX1() {
        return x1;
    }

    public float getY1() {
        return y1;
    }

    public float getAngle() {
        return angle;
    }

    @Override
    public int[] getAtomIndices() {
        return getIndices();
    }

    public float[] getPoints() {
        float[] p = new float[indices.length * 2];
        p[0] = x0;
        p[1] = y0;
        if (indices.length > 1) {
            p[2] = x1;
            p[3] = y1;
        }
        float dx = x1 - x0, dy = y1 - y0;
        for (int i = 2; i < indices.length; i++) {
            float nx = (float) (dx * Math.cos(Math.PI * 2 / indices.length) -
                    dy * Math.sin(Math.PI * 2 / indices.length));
            float ny = (float) (dx * Math.sin(Math.PI * 2 / indices.length) +
                    dy * Math.cos(Math.PI * 2 / indices.length));
            if (angle != 0) {
                float c = (float) Math.cos(angle), s = (float) Math.sin(angle);
                float rx = nx * c - ny * s, ry = nx * s + ny * c;
                nx = rx;
                ny = ry;
            }
            dx = nx;
            dy = ny;
            p[i * 2] = p[(i - 1) * 2] + dx;
            p[i * 2 + 1] = p[(i - 1) * 2 + 1] + dy;
        }
        return p;
    }

    static Benzene fromJson(JsonObject o) {
        int[] ids;
        if (o.has("indices")) {
            JsonArray a = o.getAsJsonArray("indices");
            ids = new int[a.size()];
            for (int i = 0; i < ids.length; i++) ids[i] = a.get(i).getAsInt();
        } else {
            int n = Molecule.integer(o, "count", 6), start = o.get("start_index").getAsInt();
            ids = new int[n];
            for (int i = 0; i < n; i++) ids[i] = start + i;
        }
        float x0, y0, x1, y1;
        if (o.has("u0") && o.has("v0")) {
            float u = o.get("u0").getAsFloat(), v = o.get("v0").getAsFloat();
            x0 = (float) (Math.sqrt(3) / 2 * (u + v));
            y0 = (u - v) * .5f;
        } else {
            x0 = o.get("x0").getAsFloat();
            y0 = o.get("y0").getAsFloat();
        }
        if (o.has("u1") && o.has("v1")) {
            float u = o.get("u1").getAsFloat(), v = o.get("v1").getAsFloat();
            x1 = (float) (Math.sqrt(3) / 2 * (u + v));
            y1 = (u - v) * .5f;
        } else {
            x1 = o.get("x1").getAsFloat();
            y1 = o.get("y1").getAsFloat();
        }
        float angle = o.has("angle") ? o.get("angle").getAsFloat() :
                (o.has("clockwise") && !o.get("clockwise").getAsBoolean() ? 0f : (float) Math.PI);
        return new Benzene(ids, x0, y0, x1, y1, angle);
    }
}
