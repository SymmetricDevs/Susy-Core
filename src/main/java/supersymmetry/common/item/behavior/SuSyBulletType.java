package supersymmetry.common.item.behavior;

public class SuSyBulletType {

    private final float damageMultiplier;
    private final double speed;
    private final int armorPenetration;
    private final boolean explosive;
    private final boolean incendiary;

    public SuSyBulletType(float damageMultiplier, double speed, int armorPenetration, boolean explosive, boolean incendiary) {
        this.damageMultiplier = damageMultiplier;
        this.speed = speed;
        this.armorPenetration = armorPenetration;
        this.explosive = explosive;
        this.incendiary = incendiary;
    }

    public float getDamageMultiplier() {
        return damageMultiplier;
    }

    public double getSpeed() {
        return speed;
    }

    public int getArmorPenetration() {
        return armorPenetration;
    }

    public boolean isExplosive() {
        return explosive;
    }

    public boolean isIncendiary() {
        return incendiary;
    }

    public static final SuSyBulletType PISTOL  = new SuSyBulletType(1.0f,  1.0, 0, false, false);
    public static final SuSyBulletType RIFLE   = new SuSyBulletType(1.2f,  1.1, 2, false, false);
    public static final SuSyBulletType SHOTGUN = new SuSyBulletType(0.9f,  0.8, 0, false, false);
    public static final SuSyBulletType SMG     = new SuSyBulletType(0.85f, 1.0, 0, false, false);
}