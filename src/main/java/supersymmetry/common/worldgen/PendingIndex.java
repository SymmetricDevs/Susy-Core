package supersymmetry.common.worldgen;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Unique tasks indexed by destination. Retries rotate fairly and never drop work. */
public final class PendingIndex<K, V> {

    private final LinkedHashMap<Long, LinkedHashMap<K, V>> byChunk = new LinkedHashMap<>();
    private int size;

    public boolean add(long chunk, K key, V value) {
        Map<K, V> entries = byChunk.computeIfAbsent(chunk, c -> new LinkedHashMap<>());
        if (entries.containsKey(key)) return false;
        entries.put(key, value);
        size++;
        return true;
    }

    public int size() {
        return size;
    }

    public int chunkCount() {
        return byChunk.size();
    }

    public Long firstChunk() {
        return byChunk.isEmpty() ? null : byChunk.keySet().iterator().next();
    }

    public Map.Entry<K, V> firstEntry(long chunk) {
        Map<K, V> entries = byChunk.get(chunk);
        return entries == null || entries.isEmpty() ? null : entries.entrySet().iterator().next();
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public List<Long> chunkKeys() {
        return new ArrayList<>(byChunk.keySet());
    }

    public List<Map.Entry<K, V>> entries(long chunk) {
        Map<K, V> entries = byChunk.get(chunk);
        return entries == null ? new ArrayList<>() : new ArrayList<>(entries.entrySet());
    }

    public void remove(long chunk, K key) {
        Map<K, V> entries = byChunk.get(chunk);
        if (entries != null && entries.remove(key) != null) {
            size--;
            if (entries.isEmpty()) byChunk.remove(chunk);
        }
    }

    public void rotate(long chunk) {
        LinkedHashMap<K, V> entries = byChunk.remove(chunk);
        if (entries != null) byChunk.put(chunk, entries);
    }

    public void rotateEntry(long chunk, K key) {
        LinkedHashMap<K, V> entries = byChunk.get(chunk);
        if (entries != null && entries.containsKey(key)) {
            V value = entries.remove(key);
            entries.put(key, value);
        }
    }

    public void clear() {
        byChunk.clear();
        size = 0;
    }
}
