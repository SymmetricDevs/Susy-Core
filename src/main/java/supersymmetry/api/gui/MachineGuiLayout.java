package supersymmetry.api.gui;

/** Coordinates shared by the recipe map and simple machine GUI mixins. */
public final class MachineGuiLayout {

    private static final int SLOT_SIZE = 18;
    private static final int INPUT_RIGHT_EDGE = 70;
    private static final int MIN_LEFT = 6;
    private static final int MIN_TOP = 16; // below the label at y=5
    private static final int CONTROL_TOP = 62;
    private static final int CONTROL_GAP = 4;

    private MachineGuiLayout() {}

    public static int inputXShift(int items, int fluids) {
        int count = items == 0 ? fluids : items;
        int columns = columns(count);
        int rows = rows(count, columns);
        int firstX = INPUT_RIGHT_EDGE - columns * SLOT_SIZE;
        int leftmostX = firstX;
        if (items != 0 && fluids > 0) {
            if (rows >= fluids && columns < 3) {
                leftmostX = firstX - SLOT_SIZE;
            } else {
                leftmostX = Math.min(firstX,
                        firstX + (columns - 1 - Math.min(fluids - 1, 2)) * SLOT_SIZE);
            }
        }
        return Math.max(0, MIN_LEFT - leftmostX);
    }

    public static int recipeYShift(int items, int fluids, int baseYOffset) {
        return Math.max(0, MIN_TOP - firstY(items, fluids, baseYOffset));
    }

    public static int fluidColumns(String recipeMapName, int fluids) {
        if ("crystallizer".equals(recipeMapName)) return 2;
        if ("circuit_assembler".equals(recipeMapName) && fluids == 4) return 4;
        return 3;
    }

    public static int fluidLeftOfItemsX(int items) {
        return INPUT_RIGHT_EDGE - columns(items) * SLOT_SIZE;
    }

    public static int controlsYShift(int items, int fluids, int baseYOffset, int fluidColumns) {
        int recipeYOffset = baseYOffset + recipeYShift(items, fluids, baseYOffset);
        return Math.max(0, lastSlotBottom(items, fluids, recipeYOffset, fluidColumns) + CONTROL_GAP -
                (CONTROL_TOP + baseYOffset));
    }

    private static int firstY(int items, int fluids, int yOffset) {
        int count = items == 0 ? fluids : items;
        int rowCount = rows(count, columns(count));
        int y = 33 - (int) (rowCount / 2.0 * SLOT_SIZE) + yOffset;
        if (items + fluids == 12 || items >= 6 && fluids >= 2) {
            y -= 9;
        }
        return y;
    }

    private static int lastSlotBottom(int items, int fluids, int yOffset, int fluidColumns) {
        int count = items == 0 ? fluids : items;
        int columnCount = columns(count);
        int rowCount = rows(count, columnCount);
        int firstY = firstY(items, fluids, yOffset);
        int bottom = firstY + rowCount * SLOT_SIZE;
        if (items != 0 && fluids > 0 && !(rowCount >= fluids && columnCount < 3)) {
            int fluidRows = (fluids + fluidColumns - 1) / fluidColumns;
            bottom = firstY + (items + fluids == 12 ? 2 : 0) +
                    (rowCount + fluidRows) * SLOT_SIZE;
        }
        return bottom;
    }

    private static int columns(int count) {
        int root = (int) Math.sqrt(count);
        if (root * root == count) return root;
        if (count == 3) return 3;
        return root + 1;
    }

    private static int rows(int count, int columns) {
        return (count + columns - 1) / columns;
    }
}
