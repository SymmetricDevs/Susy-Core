package supersymmetry.client.network;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PacketRulesTest {

    @Test
    void slotsAreValidatedAgainstTheDestination() {
        assertTrue(PacketRules.invalidSlot(-1, 46));
        assertFalse(PacketRules.invalidSlot(0, 46));
        assertFalse(PacketRules.invalidSlot(45, 46));
        assertTrue(PacketRules.invalidSlot(46, 46));
        assertTrue(PacketRules.invalidSlot(30, 0));
    }

    @Test
    void nonWorldGameModeAndDemoUpdatesArePreserved() {
        assertFalse(PacketRules.needsWorld(3));
        assertFalse(PacketRules.needsWorld(5));
        assertTrue(PacketRules.needsWorld(1));
        assertTrue(PacketRules.needsWorld(7));
        assertTrue(PacketRules.needsWorld(8));
    }
}
