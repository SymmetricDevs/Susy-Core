package supersymmetry.api.gui;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class MachineGuiLayoutTest {

    @Test
    void emptyInputsHaveAnEmptyGrid() {
        assertEquals(0, MachineGuiLayout.inputXShift(0, 0));
        assertEquals(0, MachineGuiLayout.recipeYShift(0, 0, 0));
        assertEquals(0, MachineGuiLayout.controlsYShift(0, 0, 0, 3));
    }

    @Test
    void normalCircuitAssemblerGeometryIsPreserved() {
        assertEquals(1, MachineGuiLayout.recipeYShift(6, 4, 9));
        assertEquals(3, MachineGuiLayout.controlsYShift(6, 4, 9, 4));
    }
}
