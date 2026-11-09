package supersymmetry.common.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

public class PendingIndexTest {

    @Test
    public void failedEntryRetriesAfterOtherTasksInTheSameChunk() {
        PendingIndex<String, String> q = new PendingIndex<>();
        q.add(1, "bad", "a");
        q.add(1, "good", "b");
        q.rotateEntry(1, "bad");
        assertEquals("good", q.firstEntry(1).getKey());
        q.remove(1, "good");
        assertEquals("bad", q.firstEntry(1).getKey());
        assertEquals(1, q.size());
        q.rotateEntry(1, "absent");
        assertEquals(1, q.size());
    }

    @Test
    public void duplicatesCannotMultiplyRemoteWork() {
        PendingIndex<String, String> q = new PendingIndex<>();
        assertTrue(q.add(1L, "rock", "first"));
        assertFalse(q.add(1L, "rock", "replacement"));
        assertEquals(1, q.size());
        assertEquals("first", q.firstEntry(1L).getValue());
    }

    @Test
    public void differentDestinationChunksRemainIndependent() {
        PendingIndex<String, String> q = new PendingIndex<>();
        q.add(-1, "same", "a");
        q.add(0, "same", "b");
        q.remove(-1, "same");
        assertEquals(1, q.size());
        assertEquals("b", q.firstEntry(0).getValue());
    }

    @Test
    public void retryRotationDoesNotStarveLaterChunksOrLoseEntries() {
        PendingIndex<Integer, Integer> q = new PendingIndex<>();
        for (int i = 0; i < 100; i++) q.add(i, i, i);
        for (int i = 0; i < 1000; i++) {
            long key = q.firstChunk();
            assertEquals(i % 100, key);
            q.rotate(key);
        }
        assertEquals(100, q.size());
        assertEquals(100, q.chunkCount());
    }

    @Test
    public void removingLastTaskReleasesDestinationBucket() {
        PendingIndex<String, String> q = new PendingIndex<>();
        q.add(1, "a", "a");
        q.remove(1, "a");
        q.remove(1, "a");
        assertTrue(q.isEmpty());
        assertNull(q.firstChunk());
        assertEquals(0, q.chunkCount());
    }

    @Test
    public void independentWorldIndexesNeverShareEntries() {
        PendingIndex<String, String> a = new PendingIndex<>(), b = new PendingIndex<>();
        a.add(1, "ore", "rock");
        assertTrue(b.isEmpty());
        a.clear();
        assertTrue(a.isEmpty());
    }
}
