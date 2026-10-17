package supersymmetry.common.item.behavior;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class SuSyGunType {

    private final double muzzleVelocity;
    private final double spread;
    private final int pellets;
    private final float damage;
    private final int cooldownTicks;
    private final List<GunFireMode> fireModes; // first one is the default, the others are selectable
    private final int burstCount;
    private final int burstIntervalTicks;

    // no burst gun shortcut
    public SuSyGunType(double muzzleVelocity, double spread, int pellets, float damage,
                       int cooldownTicks, GunFireMode... fireModes) {
        this(muzzleVelocity, spread, pellets, damage, cooldownTicks, 1, 1, fireModes);
    }

    public SuSyGunType(double muzzleVelocity, double spread, int pellets, float damage,
                       int cooldownTicks, int burstCount, int burstIntervalTicks,
                       GunFireMode... fireModes) {
        if (fireModes.length == 0) {
            throw new IllegalArgumentException("gun needs at least one fire mode");
        }
        List<GunFireMode> modes = new ArrayList<>();
        for (GunFireMode mode : fireModes) {
            Objects.requireNonNull(mode, "fireMode");
            if (modes.contains(mode)) {
                throw new IllegalArgumentException("duplicate fire mode " + mode);
            }
            modes.add(mode);
        }
        if (modes.contains(GunFireMode.BOLT_ACTION) && modes.size() > 1) {
            throw new IllegalArgumentException("BOLT_ACTION cannot be combined with other modes");
        }
        if (modes.contains(GunFireMode.BURST) != (burstCount > 1)) {
            throw new IllegalArgumentException(
                    "BURST needs burstCount > 1, guns without BURST need burstCount == 1 (got " +
                            modes + ", " + burstCount + ")");
        }
        this.muzzleVelocity = muzzleVelocity;
        this.spread = spread;
        this.pellets = pellets;
        this.damage = damage;
        this.cooldownTicks = cooldownTicks;
        this.fireModes = Collections.unmodifiableList(modes);
        this.burstCount = burstCount;
        this.burstIntervalTicks = burstIntervalTicks;
    }

    public double muzzleVelocity() {
        return muzzleVelocity;
    }

    public double spread() {
        return spread;
    }

    public int pellets() {
        return pellets;
    }

    public float damage() {
        return damage;
    }

    public int cooldownTicks() {
        return cooldownTicks;
    }

    public List<GunFireMode> fireModes() {
        return fireModes;
    }

    public int burstCount() {
        return burstCount;
    }

    public int burstIntervalTicks() {
        return burstIntervalTicks;
    }

    // Catalog
    // velocity, spread, pellets, damage, cooldown (ticks), mode(s)
    public static final SuSyGunType PISTOL  = new SuSyGunType(20.0, 1.5, 1, 6f, 8,  GunFireMode.SEMI_AUTO);
    public static final SuSyGunType RIFLE   = new SuSyGunType(60.0, 0.13, 1, 12f, 20, GunFireMode.BOLT_ACTION);
    public static final SuSyGunType SHOTGUN = new SuSyGunType(30.0, 10, 8, 4f, 20, GunFireMode.SEMI_AUTO);
    public static final SuSyGunType SMG     = new SuSyGunType(30.0, 1, 1, 4f, 4, 3, 2, GunFireMode.FULL_AUTO, GunFireMode.BURST, GunFireMode.SEMI_AUTO);

}
