package supersymmetry.client.molecule;

/* Adapted from gregtech-molecule-drawings by Ruben Vergauwen. SPDX-License-Identifier: LGPL-3.0-only */
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class Parens implements MoleculeElement {

    private final String sub, sup;
    private final int[] atoms;

    Parens(String sub, String sup, int[] atoms) {
        this.sub = sub;
        this.sup = sup;
        this.atoms = atoms;
    }

    public String getSub() {
        return sub;
    }

    public String getSup() {
        return sup;
    }

    public int[] getAtoms() {
        return atoms.clone();
    }

    @Override
    public int[] getAtomIndices() {
        return getAtoms();
    }

    static Parens fromJson(JsonObject o) {
        JsonArray a = o.getAsJsonArray("atoms");
        int[] atoms = new int[a.size()];
        for (int i = 0; i < atoms.length; i++) atoms[i] = a.get(i).getAsInt();
        return new Parens(Molecule.string(o, "sub", ""), Molecule.string(o, "sup", ""), atoms);
    }
}
