package supersymmetry.common.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import net.minecraft.nbt.NBTTagCompound;

import org.junit.jupiter.api.Test;

public class IndicatorPersistenceTest {

    @Test
    public void propertyOrderingDoesNotDuplicateSavedMarkers() {
        NBTTagCompound a = new NBTTagCompound(), b = new NBTTagCompound(), ap = new NBTTagCompound(),
                bp = new NBTTagCompound();
        a.setString("Name", "test:rock");
        b.setString("Name", "test:rock");
        ap.setString("variant", "granite");
        ap.setString("shape", "tiny");
        bp.setString("shape", "tiny");
        bp.setString("variant", "granite");
        a.setTag("Properties", ap);
        b.setTag("Properties", bp);
        assertEquals(new IndicatorData.Task(1, (byte) 0, a).key(), new IndicatorData.Task(1, (byte) 0, b).key());
    }

    @Test
    public void savedTasksRoundTripWithNegativePositionsAndBlockRegistryNames() {
        IndicatorData original = new IndicatorData();
        NBTTagCompound state = new NBTTagCompound();
        state.setString("Name", "gregtech:surface_rock_steel");
        IndicatorData.Task first = new IndicatorData.Task(-1234, (byte) 0, state);
        IndicatorData.Task second = new IndicatorData.Task(Long.MIN_VALUE, (byte) 1, state);
        original.pending.add(-5, first.key(), first);
        original.pending.add(7, second.key(), second);
        NBTTagCompound saved = original.writeToNBT(new NBTTagCompound());
        IndicatorData restored = new IndicatorData();
        restored.readFromNBT(saved);
        assertEquals(2, restored.pending.size());
        assertEquals(-1234, restored.pending.firstEntry(-5).getValue().position);
        assertEquals("gregtech:surface_rock_steel", restored.pending.firstEntry(-5).getValue().state.getString("Name"));
        assertFalse(restored.pending.add(-5, first.key(), first));
        assertEquals(Long.MIN_VALUE, restored.pending.firstEntry(7).getValue().position);
    }

    @Test
    public void completedTasksDoNotReturnAfterSaveAndReload() {
        IndicatorData data = new IndicatorData();
        NBTTagCompound state = new NBTTagCompound();
        state.setString("Name", "test:rock");
        IndicatorData.Task task = new IndicatorData.Task(42, (byte) 0, state);
        data.pending.add(1, task.key(), task);
        data.pending.remove(1, task.key());
        IndicatorData restored = new IndicatorData();
        restored.readFromNBT(data.writeToNBT(new NBTTagCompound()));
        assertTrue(restored.pending.isEmpty());
    }
}
