package supersymmetry.client.molecule;

/* Adapted from gregtech-molecule-drawings by Ruben Vergauwen. SPDX-License-Identifier: LGPL-3.0-only */
public interface MoleculeElement {

    /** Atom indices affected by or referenced by this element. */
    default int[] getAtomIndices() {
        return new int[0];
    }
}
