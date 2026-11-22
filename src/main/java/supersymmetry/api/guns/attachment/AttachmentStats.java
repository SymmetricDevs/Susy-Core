package supersymmetry.api.guns.attachment;

import lombok.Getter;

/** The stats a gun ends up with, and the multipliers an attachment applies to them. */
@Getter
public final class AttachmentStats {

    public static final float MIN_DAMAGE = 0.1f;
    public static final double MIN_AIM_TIME = 1.0;
    public static final float MIN_ZOOM = 1.0f;

    private final float damage;
    private final double spread;
    private final float recoil;
    private final double aimTime;
    private final float zoom;
    private final int magazineSize;

    public AttachmentStats(float damage, double spread, float recoil, double aimTime, float zoom, int magazineSize) {
        this.damage = Math.max(damage, MIN_DAMAGE);
        this.spread = Math.max(spread, 0);
        this.recoil = Math.max(recoil, 0);
        this.aimTime = Math.max(aimTime, MIN_AIM_TIME);
        this.zoom = Math.max(zoom, MIN_ZOOM);
        this.magazineSize = Math.max(magazineSize, 0);
    }

    public AttachmentStats apply(Modifiers modifiers) {
        if (modifiers == null || modifiers.isNone()) return this;
        return new AttachmentStats(damage * modifiers.getDamageMultiplier(), spread * modifiers.getSpreadMultiplier(),
                recoil * modifiers.getRecoilMultiplier(), aimTime * modifiers.getAimTimeMultiplier(),
                zoom * modifiers.getZoomMultiplier(), magazineSize);
    }

    /** Stat changes of one attachment, as multipliers against the gun. */
    @Getter
    public static final class Modifiers {

        public static final Modifiers NONE = new Modifiers(1f, 1f, 1f, 1f, 1f);

        private final float damageMultiplier;
        private final float spreadMultiplier;
        private final float recoilMultiplier;
        private final float aimTimeMultiplier;
        private final float zoomMultiplier;

        public Modifiers(float damageMultiplier, float spreadMultiplier, float recoilMultiplier,
                         float aimTimeMultiplier, float zoomMultiplier) {
            for (float multiplier : new float[] { damageMultiplier, spreadMultiplier, recoilMultiplier,
                    aimTimeMultiplier, zoomMultiplier }) {
                if (!(multiplier > 0) || Float.isInfinite(multiplier)) {
                    throw new IllegalArgumentException("multiplier must be positive and finite, got " + multiplier);
                }
            }
            this.damageMultiplier = damageMultiplier;
            this.spreadMultiplier = spreadMultiplier;
            this.recoilMultiplier = recoilMultiplier;
            this.aimTimeMultiplier = aimTimeMultiplier;
            this.zoomMultiplier = zoomMultiplier;
        }

        public Modifiers combined(Modifiers other) {
            if (other == null) return this;
            return new Modifiers(damageMultiplier * other.damageMultiplier,
                    spreadMultiplier * other.spreadMultiplier, recoilMultiplier * other.recoilMultiplier,
                    aimTimeMultiplier * other.aimTimeMultiplier, zoomMultiplier * other.zoomMultiplier);
        }

        public boolean isNone() {
            return damageMultiplier == 1f && spreadMultiplier == 1f && recoilMultiplier == 1f &&
                    aimTimeMultiplier == 1f && zoomMultiplier == 1f;
        }
    }
}
