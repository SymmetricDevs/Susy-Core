package supersymmetry.common.faction;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayer;

public final class FactionUnreachableTargets {

    public static final long IGNORE_TICKS = 15L * 20L;
    private static final double REEVALUATE_MOVE_SQ = 10.0D * 10.0D;

    private static final class Entry {

        long expiry;
        int dim;
        double x, y, z;
    }

    private static final Map<UUID, Entry> ENTRIES = new HashMap<>();

    public static void mark(EntityPlayer p, long now) {
        if (ENTRIES.size() > 64) purgeExpired(now);
        Entry e = ENTRIES.computeIfAbsent(p.getUniqueID(), k -> new Entry());
        e.expiry = now + IGNORE_TICKS;
        e.dim = p.dimension;
        e.x = p.posX;
        e.y = p.posY;
        e.z = p.posZ;
    }

    public static boolean isIgnored(EntityPlayer p, long now) {
        Entry e = ENTRIES.get(p.getUniqueID());
        if (e == null) return false;
        boolean stale = now >= e.expiry || e.expiry - now > IGNORE_TICKS          // world time went backwards (new
                                                                                  // world)
                || e.dim != p.dimension || p.getDistanceSq(e.x, e.y, e.z) > REEVALUATE_MOVE_SQ;
        if (stale) {
            ENTRIES.remove(p.getUniqueID());
            return false;
        }
        return true;
    }

    private static void purgeExpired(long now) {
        Iterator<Entry> it = ENTRIES.values().iterator();
        while (it.hasNext()) if (now >= it.next().expiry) it.remove();
    }
}
