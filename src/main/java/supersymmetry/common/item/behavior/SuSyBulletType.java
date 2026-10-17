package supersymmetry.common.item.behavior;

public final class SuSyBulletType {

    private final float damageMultiplier;
    private final double velocityMultiplier;
    private final int armorPenetration;

    public SuSyBulletType(float damageMultiplier, double velocityMultiplier, int armorPenetration) {
        this.damageMultiplier = damageMultiplier;
        this.velocityMultiplier = velocityMultiplier;
        this.armorPenetration = armorPenetration;
    }

    public float damageMultiplier() {
        return damageMultiplier;
    }

    public double velocityMultiplier() {
        return velocityMultiplier;
    }

    public int armorPenetration() {
        return armorPenetration;
    }

    // Catalog
    // damageMult, velMult, armorPen
    public static final SuSyBulletType PISTOL  = new SuSyBulletType(1.0f,  1.0, 0);
    public static final SuSyBulletType RIFLE   = new SuSyBulletType(1.2f,  1.1, 2);
    public static final SuSyBulletType SHOTGUN = new SuSyBulletType(0.9f,  0.8, 0);
    public static final SuSyBulletType SMG     = new SuSyBulletType(0.85f, 1.0, 0);
}