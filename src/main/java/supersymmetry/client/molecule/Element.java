package supersymmetry.client.molecule;

/* Adapted from gregtech-molecule-drawings by Ruben Vergauwen. SPDX-License-Identifier: LGPL-3.0-only */
public final class Element {

    private final String symbol;
    private final int color;
    private final int count;
    private final boolean invisible;

    public Element(String symbol) {
        this(symbol, defaultColor(symbol), 1);
    }

    public Element(String symbol, int color) {
        this(symbol, color, 1);
    }

    public Element(String symbol, int color, int count) {
        this(symbol, color, count, symbol == null || symbol.isEmpty());
    }

    public Element(String symbol, int color, int count, boolean invisible) {
        this.symbol = symbol == null ? "" : symbol;
        this.color = color;
        this.count = count;
        this.invisible = invisible;
    }

    public String getSymbol() {
        return symbol;
    }

    public int getColor() {
        return color;
    }

    public int getCount() {
        return count;
    }

    public String getDisplaySymbol() {
        return symbol + (count == 1 ? "" : toSubscript(count));
    }

    public boolean isInvisible() {
        return invisible;
    }

    private static String toSubscript(int n) {
        StringBuilder b = new StringBuilder();
        for (char c : Integer.toString(n).toCharArray()) b.append("₀₁₂₃₄₅₆₇₈₉".charAt(c - '0'));
        return b.toString();
    }

    private static int defaultColor(String s) {
        if ("C".equals(s)) return 0xff909090;
        if ("O".equals(s)) return 0xffff0d0d;
        if ("N".equals(s)) return 0xff3050f8;
        if ("H".equals(s)) return 0xffffffff;
        if ("S".equals(s)) return 0xffffff30;
        if ("P".equals(s)) return 0xffff8000;
        if ("F".equals(s)) return 0xff90e050;
        if ("Cl".equals(s)) return 0xff1ff01f;
        if ("Br".equals(s)) return 0xffa62929;
        if ("I".equals(s)) return 0xff940094;
        return 0xffeeeeee;
    }
}
