package supersymmetry.client.molecule;

/* Adapted from gregtech-molecule-drawings by Ruben Vergauwen. SPDX-License-Identifier: LGPL-3.0-only */
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class Circle implements MoleculeElement {

    private final float xx, xy, yx, yy;
    private final int[] atoms;

    Circle(float xx, float xy, float yx, float yy, int[] atoms) {
        this.xx = xx;
        this.xy = xy;
        this.yx = yx;
        this.yy = yy;
        this.atoms = atoms;
    }

    public float getXX() {
        return xx;
    }

    public float getXY() {
        return xy;
    }

    public float getYX() {
        return yx;
    }

    public float getYY() {
        return yy;
    }

    public int[] getAtoms() {
        return atoms.clone();
    }

    @Override
    public int[] getAtomIndices() {
        return getAtoms();
    }

    static Circle fromJson(JsonObject o) {
        float xx, xy, yx, yy;
        if (o.has("x") && o.has("y")) {
            xx = o.get("x").getAsFloat();
            xy = 0;
            yx = 0;
            yy = o.get("y").getAsFloat();
        } else {
            xx = o.get("a00").getAsFloat();
            xy = o.get("a01").getAsFloat();
            yx = o.get("a10").getAsFloat();
            yy = o.get("a11").getAsFloat();
        }
        JsonArray a = o.getAsJsonArray("atoms");
        int[] ids = new int[a.size()];
        for (int i = 0; i < ids.length; i++) ids[i] = a.get(i).getAsInt();
        return new Circle(xx, xy, yx, yy, ids);
    }
}
