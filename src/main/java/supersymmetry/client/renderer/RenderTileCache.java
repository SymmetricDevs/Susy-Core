package supersymmetry.client.renderer;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.function.Supplier;

/** Concurrent render lookups, with weak worlds and weak values to avoid retaining unloaded worlds. */
public final class RenderTileCache<W, T> {

    private final Map<W, ConcurrentHashMap<Long, ConcurrentHashMap<Long, WeakReference<T>>>> worlds = new WeakHashMap<>();

    private ConcurrentHashMap<Long, ConcurrentHashMap<Long, WeakReference<T>>> world(W world, boolean create) {
        synchronized (worlds) {
            ConcurrentHashMap<Long, ConcurrentHashMap<Long, WeakReference<T>>> map = worlds.get(world);
            if (map == null && create) {
                map = new ConcurrentHashMap<>();
                worlds.put(world, map);
            }
            return map;
        }
    }

    public T get(W world, long chunk, long position, Supplier<T> loader, Predicate<T> valid) {
        ConcurrentHashMap<Long, ConcurrentHashMap<Long, WeakReference<T>>> sections = world(world, true);
        ConcurrentHashMap<Long, WeakReference<T>> entries = sections.computeIfAbsent(chunk,
                key -> new ConcurrentHashMap<>());
        WeakReference<T> reference = entries.get(position);
        T tile = reference == null ? null : reference.get();
        if (tile != null && valid.test(tile)) return tile;
        // World access stays outside cache locks and ConcurrentHashMap mapping functions.
        tile = loader.get();
        if (tile == null || !valid.test(tile)) {
            if (reference != null) entries.remove(position, reference);
            return null;
        }
        entries.put(position, new WeakReference<>(tile));
        return tile;
    }

    public void remove(W world, long chunk, long position) {
        ConcurrentHashMap<Long, ConcurrentHashMap<Long, WeakReference<T>>> sections = world(world, false);
        if (sections != null) {
            ConcurrentHashMap<Long, WeakReference<T>> entries = sections.get(chunk);
            if (entries != null) entries.remove(position);
        }
    }

    public void unloadChunk(W world, long chunk) {
        ConcurrentHashMap<Long, ConcurrentHashMap<Long, WeakReference<T>>> sections = world(world, false);
        if (sections != null) sections.remove(chunk);
    }

    public void unloadWorld(W world) {
        synchronized (worlds) {
            worlds.remove(world);
        }
    }

    public void clear() {
        synchronized (worlds) {
            worlds.clear();
        }
    }
}
