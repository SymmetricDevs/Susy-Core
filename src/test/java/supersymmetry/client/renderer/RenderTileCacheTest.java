package supersymmetry.client.renderer;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;

import org.junit.jupiter.api.Test;

public class RenderTileCacheTest {

    static final class Tile {

        volatile boolean valid = true;
    }

    @Test
    public void reuseInvalidationAndMisses() {
        RenderTileCache<Object, Tile> cache = new RenderTileCache<>();
        Object world = new Object();
        AtomicInteger loads = new AtomicInteger();
        Tile first = new Tile(), second = new Tile();
        assertSame(first, cache.get(world, 0, 1, () -> {
            loads.incrementAndGet();
            return first;
        }, t -> t.valid));
        assertSame(first, cache.get(world, 0, 1, () -> { throw new AssertionError("cached lookup loaded again"); },
                t -> t.valid));
        first.valid = false;
        assertSame(second, cache.get(world, 0, 1, () -> {
            loads.incrementAndGet();
            return second;
        }, t -> t.valid));
        cache.remove(world, 0, 1);
        assertSame(first, cache.get(world, 0, 1, () -> {
            first.valid = true;
            loads.incrementAndGet();
            return first;
        }, t -> t.valid));
        assertEquals(3, loads.get());
        for (int i = 0; i < 2; i++) assertNull(cache.get(world, 0, 2, () -> {
            loads.incrementAndGet();
            return null;
        }, t -> t.valid));
        assertEquals(5, loads.get());
    }

    @Test
    public void worldAndChunkUnloadsDoNotMixCaches() {
        RenderTileCache<Object, Tile> cache = new RenderTileCache<>();
        Object a = new Object(), b = new Object();
        Tile ta = new Tile(), tb = new Tile(), other = new Tile(), replacement = new Tile();
        assertSame(ta, cache.get(a, -1, 1, () -> ta, t -> true));
        assertSame(tb, cache.get(b, -1, 1, () -> tb, t -> true));
        assertSame(other, cache.get(a, 2, 1, () -> other, t -> true));
        cache.unloadChunk(a, -1);
        assertSame(replacement, cache.get(a, -1, 1, () -> replacement, t -> true));
        assertSame(tb, cache.get(b, -1, 1, () -> { throw new AssertionError(); }, t -> true));
        assertSame(other, cache.get(a, 2, 1, () -> { throw new AssertionError(); }, t -> true));
        cache.unloadWorld(a);
        assertSame(ta, cache.get(a, 2, 1, () -> ta, t -> true));
        assertSame(tb, cache.get(b, -1, 1, () -> { throw new AssertionError(); }, t -> true));
    }

    @Test
    public void unloadDuringLookupCannotRepublishTheOldWorldCache() throws Exception {
        for (boolean chunkOnly : new boolean[] { false, true }) {
            RenderTileCache<Object, Tile> cache = new RenderTileCache<>();
            Object world = new Object();
            Tile old = new Tile(), fresh = new Tile();
            CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
            ExecutorService executor = Executors.newSingleThreadExecutor();
            try {
                Future<Tile> task = executor.submit(() -> cache.get(world, 0, 0, () -> {
                    entered.countDown();
                    try {
                        if (!release.await(3, TimeUnit.SECONDS)) throw new AssertionError("release timeout");
                    } catch (InterruptedException ex) {
                        throw new AssertionError(ex);
                    }
                    return old;
                }, t -> true));
                assertTrue(entered.await(3, TimeUnit.SECONDS));
                if (chunkOnly) cache.unloadChunk(world, 0);
                else cache.unloadWorld(world);
                release.countDown();
                assertSame(old, task.get(3, TimeUnit.SECONDS));
                assertSame(fresh, cache.get(world, 0, 0, () -> fresh, t -> true));
            } finally {
                release.countDown();
                executor.shutdownNow();
            }
        }
    }

    @Test
    public void collisionHeavyConcurrentQueriesCompleteWithoutCorruption() throws Exception {
        RenderTileCache<Object, Tile> cache = new RenderTileCache<>();
        Object world = new Object();
        Tile[] source = new Tile[1024];
        for (int i = 0; i < source.length; i++) source[i] = new Tile();
        ExecutorService executor = Executors.newFixedThreadPool(12);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> jobs = new ArrayList<>();
        try {
            for (int worker = 0; worker < 12; worker++) {
                final int offset = worker;
                jobs.add(executor.submit(() -> {
                    try {
                        start.await();
                    } catch (InterruptedException ex) {
                        throw new AssertionError(ex);
                    }
                    for (int pass = 0; pass < 4; pass++) for (int n = 0; n < source.length; n++) {
                        int index = (n + offset) % source.length;
                        long key = ((long) index << 32) | (index & 0xffffffffL); // All Long keys have hash 0.
                        assertSame(source[index], cache.get(world, 0, key, () -> source[index], t -> true));
                    }
                }));
            }
            start.countDown();
            for (Future<?> job : jobs) job.get(15, TimeUnit.SECONDS);
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    public void lookupsDoNotHoldTheWorldLockWhileLoading() throws Exception {
        RenderTileCache<Object, Tile> cache = new RenderTileCache<>();
        Object world = new Object();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        try {
            Tile tile = cache.get(world, 0, 0, () -> {
                try {
                    executor.submit(() -> cache.unloadWorld(world)).get(2, TimeUnit.SECONDS);
                } catch (Exception ex) {
                    throw new AssertionError("world lock held across external lookup", ex);
                }
                return new Tile();
            }, t -> true);
            assertNotNull(tile);
        } finally {
            executor.shutdownNow();
        }
    }
}
