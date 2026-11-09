package supersymmetry.common.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import supersymmetry.SusyConfig;

class WorldgenSettingsTest {

    private final SusyConfig.WorldGeneration original = SusyConfig.worldGeneration;

    @AfterEach
    void restoreSettings() {
        SusyConfig.worldGeneration = original;
    }

    @Test
    void defaultsEnableAllGenerationFixes() {
        SusyConfig.worldGeneration = new SusyConfig.WorldGeneration();
        assertTrue(WorldgenFixes.gridMetadata());
        assertTrue(WorldgenFixes.deferIndicators());
        assertTrue(WorldgenFixes.recurrentComplex());
        assertTrue(WorldgenFixes.adaptiveCache());
    }

    @Test
    void masterSwitchDisablesEveryGenerationFix() {
        SusyConfig.worldGeneration = new SusyConfig.WorldGeneration();
        SusyConfig.worldGeneration.enabled = false;
        assertFalse(WorldgenFixes.gridMetadata());
        assertFalse(WorldgenFixes.deferIndicators());
        assertFalse(WorldgenFixes.recurrentComplex());
        assertFalse(WorldgenFixes.adaptiveCache());
    }

    @Test
    void individualOptionsAreIndependent() {
        SusyConfig.worldGeneration = new SusyConfig.WorldGeneration();
        SusyConfig.worldGeneration.deferIndicators = false;
        SusyConfig.worldGeneration.adaptiveCache = false;
        assertTrue(WorldgenFixes.gridMetadata());
        assertFalse(WorldgenFixes.deferIndicators());
        assertTrue(WorldgenFixes.recurrentComplex());
        assertFalse(WorldgenFixes.adaptiveCache());
        SusyConfig.worldGeneration.gridMetadata = false;
        SusyConfig.worldGeneration.recurrentComplex = false;
        SusyConfig.worldGeneration.deferIndicators = true;
        SusyConfig.worldGeneration.adaptiveCache = true;
        assertFalse(WorldgenFixes.gridMetadata());
        assertTrue(WorldgenFixes.deferIndicators());
        assertFalse(WorldgenFixes.recurrentComplex());
        assertTrue(WorldgenFixes.adaptiveCache());
    }
}
