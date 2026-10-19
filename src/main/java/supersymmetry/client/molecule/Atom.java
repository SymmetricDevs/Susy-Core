package supersymmetry.client.molecule;

/* Adapted from gregtech-molecule-drawings by Ruben Vergauwen. SPDX-License-Identifier: LGPL-3.0-only */
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public final class Atom implements MoleculeElement {

    private final int index;
    private final Element element;
    private final Element above, right, below, left;
    private final float x, y, z;
    private final int spinGroup;

    public Atom(int index, Element element, float x, float y, float z) {
        this(index, element, null, null, null, null, x, y, z, 0);
    }

    public Atom(int index, Element element, Element above, Element right, Element below, Element left, float x, float y,
                float z, int spinGroup) {
        this.index = index;
        this.element = element;
        this.above = above;
        this.right = right;
        this.below = below;
        this.left = left;
        this.x = x;
        this.y = y;
        this.z = z;
        this.spinGroup = spinGroup;
    }

    public int getIndex() {
        return index;
    }

    @Override
    public int[] getAtomIndices() {
        return new int[] { index };
    }

    public Element getElement() {
        return element;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public float getZ() {
        return z;
    }

    public Element getAbove() {
        return above;
    }

    public Element getRight() {
        return right;
    }

    public Element getBelow() {
        return below;
    }

    public Element getLeft() {
        return left;
    }

    public int getSpinGroup() {
        return spinGroup;
    }

    public boolean isInvisible() {
        return element.isInvisible();
    }

    static Atom fromJson(JsonObject o) {
        if (!o.has("index")) throw new JsonParseException("Atom JSON must contain index");
        float x, y;
        if (o.has("u") && o.has("v")) {
            float u = o.get("u").getAsFloat(), v = o.get("v").getAsFloat();
            x = (float) (Math.sqrt(3.0) / 2.0 * (u + v));
            y = (u - v) * 0.5f;
        } else if (o.has("x") && o.has("y")) {
            x = o.get("x").getAsFloat();
            y = o.get("y").getAsFloat();
        } else throw new JsonParseException("Atom JSON requires x/y or u/v coordinates");
        Element parsedElement = counted(o.get("element"));
        if (parsedElement == null) parsedElement = new Element("");
        return new Atom(o.get("index").getAsInt(), parsedElement, counted(o.get("above")), counted(o.get("right")),
                counted(o.get("below")), counted(o.get("left")), x, y, Molecule.number(o, "z", 0),
                Molecule.integer(o, "spin_group", 0));
    }

    private static Element counted(com.google.gson.JsonElement json) {
        if (json == null) return null;
        int count = 1;
        if (json.isJsonArray()) {
            com.google.gson.JsonArray a = json.getAsJsonArray();
            if (a.size() > 1) count = a.get(1).getAsInt();
            json = a.get(0);
        }
        String symbol = "";
        int color = 0;
        boolean invisible = false;
        if (json.isJsonPrimitive()) symbol = json.getAsString();
        else if (json.isJsonObject()) {
            JsonObject e = json.getAsJsonObject();
            symbol = Molecule.string(e, "symbol", "");
            invisible = e.has("invisible") && e.get("invisible").getAsBoolean();
            if (e.has("color")) {
                com.google.gson.JsonElement c = e.get("color");
                if (c.isJsonPrimitive()) {
                    try {
                        color = c.getAsInt();
                    } catch (NumberFormatException ex) {
                        color = 0xff000000 | Integer.parseInt(c.getAsString().replace("#", ""), 16);
                    }
                } else if (c.isJsonObject() && c.getAsJsonObject().has("color")) {
                    com.google.gson.JsonElement v = c.getAsJsonObject().get("color");
                    try {
                        color = v.getAsInt();
                    } catch (NumberFormatException ex) {
                        color = 0xff000000 | Integer.parseInt(v.getAsString().replace("#", ""), 16);
                    }
                }
            }
        }
        return new Element(symbol, color == 0 ? new Element(symbol).getColor() : color, count,
                invisible || symbol.isEmpty());
    }
}
