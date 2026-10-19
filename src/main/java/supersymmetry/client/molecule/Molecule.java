package supersymmetry.client.molecule;

/* Adapted from gregtech-molecule-drawings by Ruben Vergauwen. SPDX-License-Identifier: LGPL-3.0-only */
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

/** Parsed schematic for a chemical molecule. */
public final class Molecule {

    private final List<MoleculeElement> contents = new ArrayList<MoleculeElement>();
    private final List<Atom> atoms = new ArrayList<Atom>();
    private final List<Bond> bonds = new ArrayList<Bond>();
    private final List<Float> spinGroups = new ArrayList<Float>();

    public List<MoleculeElement> getContents() {
        return Collections.unmodifiableList(contents);
    }

    public List<Atom> getAtoms() {
        return Collections.unmodifiableList(atoms);
    }

    public List<Bond> getBonds() {
        return Collections.unmodifiableList(bonds);
    }

    public List<Float> getSpinGroups() {
        return Collections.unmodifiableList(spinGroups);
    }

    public static Molecule fromJson(JsonObject root) {
        if (root == null || !root.has("contents") || !root.get("contents").isJsonArray())
            throw new JsonParseException("Molecule JSON must contain a contents array");
        Molecule molecule = new Molecule();
        JsonArray entries = root.getAsJsonArray("contents");
        for (JsonElement entry : entries) {
            if (!entry.isJsonObject()) throw new JsonParseException("Molecule contents must be objects");
            JsonObject object = entry.getAsJsonObject();
            String type = string(object, "type", "");
            if ("atom".equals(type)) {
                Atom atom = Atom.fromJson(object);
                molecule.atoms.add(atom);
                molecule.contents.add(atom);
            } else if ("bond".equals(type)) {
                Bond bond = Bond.fromJson(object);
                molecule.bonds.add(bond);
                molecule.contents.add(bond);
            } else if ("parens".equals(type)) molecule.contents.add(Parens.fromJson(object));
            else if ("circle".equals(type)) molecule.contents.add(Circle.fromJson(object));
            else if ("benzene".equals(type)) {
                Benzene ring = Benzene.fromJson(object);
                molecule.contents.add(ring);
                float[] points = ring.getPoints();
                int[] indices = ring.getIndices();
                for (int i = 0; i < indices.length; i++) {
                    molecule.atoms.add(new Atom(indices[i], new Element(""), points[i * 2], points[i * 2 + 1], 0));
                    molecule.bonds.add(new Bond(indices[i], indices[(i + 1) % indices.length], false,
                            i % 2 == 0 ? new String[] { "solid", "solid" } : new String[] { "solid" }));
                }
            } else throw new JsonParseException("Unknown molecule content type: " + type);
        }
        if (root.has("spin")) {
            JsonElement spin = root.get("spin");
            if (spin.isJsonArray())
                for (JsonElement value : spin.getAsJsonArray()) molecule.spinGroups.add(value.getAsFloat());
            else if (spin.isJsonPrimitive() && spin.getAsJsonPrimitive().isBoolean()) {
                if (spin.getAsBoolean()) molecule.spinGroups.add(.25f);
            } else if (spin.isJsonPrimitive() && spin.getAsJsonPrimitive().isNumber())
                molecule.spinGroups.add(spin.getAsFloat());
            else throw new JsonParseException("Invalid spin value");
        }
        return molecule;
    }

    static String string(JsonObject o, String key, String fallback) {
        return o.has(key) ? o.get(key).getAsString() : fallback;
    }

    static int integer(JsonObject o, String key, int fallback) {
        return o.has(key) ? o.get(key).getAsInt() : fallback;
    }

    static float number(JsonObject o, String key, float fallback) {
        return o.has(key) ? o.get(key).getAsFloat() : fallback;
    }
}
