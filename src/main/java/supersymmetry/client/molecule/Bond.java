package supersymmetry.client.molecule;

/* Adapted from gregtech-molecule-drawings by Ruben Vergauwen. SPDX-License-Identifier: LGPL-3.0-only */
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class Bond implements MoleculeElement {

    public enum LineStyle {
        SOLID,
        DOTTED,
        OUTWARD,
        INWARD,
        THICK
    }

    private final int a, b;
    private final boolean centered;
    private final String[] lines;

    Bond(int a, int b, boolean centered, String[] lines) {
        this.a = a;
        this.b = b;
        this.centered = centered;
        this.lines = lines;
    }

    public int getA() {
        return a;
    }

    public int getB() {
        return b;
    }

    @Override
    public int[] getAtomIndices() {
        return new int[] { a, b };
    }

    public boolean isCentered() {
        return centered;
    }

    public String[] getLines() {
        return lines.clone();
    }

    public LineStyle[] getLineStyles() {
        LineStyle[] styles = new LineStyle[lines.length];
        for (int i = 0; i < lines.length; i++) {
            try {
                styles[i] = LineStyle.valueOf(lines[i].toUpperCase(java.util.Locale.ROOT));
            } catch (IllegalArgumentException error) {
                throw new IllegalStateException("Unknown bond line style: " + lines[i], error);
            }
        }
        return styles;
    }

    public int getOrder() {
        return lines.length;
    }

    static Bond fromJson(JsonObject o) {
        JsonArray array = o.has("lines") ? o.getAsJsonArray("lines") : new JsonArray();
        if (!o.has("lines")) array.add("solid");
        String[] lines = new String[array.size()];
        for (int i = 0; i < lines.length; i++) lines[i] = array.get(i).getAsString();
        return new Bond(o.get("a").getAsInt(), o.get("b").getAsInt(),
                o.has("centered") && o.get("centered").getAsBoolean(), lines);
    }
}
