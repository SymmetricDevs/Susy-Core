package supersymmetry.common.world.sky;

import net.minecraft.util.math.Vec3d;

public class SkyColorData {

    private final Vec3d sunriseColor;
    private final Vec3d noonColor;
    private final Vec3d sunsetColor;
    private final Vec3d midnightColor;

    // Fog colors (optional)
    private final Vec3d fogColor;
    private final boolean useFogColor;

    private SkyColorData(Builder builder) {
        this.sunriseColor = builder.sunriseColor;
        this.noonColor = builder.noonColor;
        this.sunsetColor = builder.sunsetColor;
        this.midnightColor = builder.midnightColor;
        this.fogColor = builder.fogColor;
        this.useFogColor = builder.useFogColor;
    }

    /**
     * Get the sky color for a given celestial angle (0.0 to 1.0)
     * 0.0 = sunrise, 0.25 = noon, 0.5 = sunset, 0.75 = midnight
     */
    public Vec3d getSkyColor(float celestialAngle) {
        // Normalize angle to 0-1 range
        celestialAngle = celestialAngle % 1.0F;
        if (celestialAngle < 0) celestialAngle += 1.0F;

        // Determine which phase we're in and interpolate
        if (celestialAngle < 0.25F) {
            // Sunrise to Noon (0.0 to 0.25)
            float t = celestialAngle / 0.25F;
            return lerpColor(sunriseColor, noonColor, t);
        } else if (celestialAngle < 0.5F) {
            // Noon to Sunset (0.25 to 0.5)
            float t = (celestialAngle - 0.25F) / 0.25F;
            return lerpColor(noonColor, sunsetColor, t);
        } else if (celestialAngle < 0.75F) {
            // Sunset to Midnight (0.5 to 0.75)
            float t = (celestialAngle - 0.5F) / 0.25F;
            return lerpColor(sunsetColor, midnightColor, t);
        } else {
            // Midnight to Sunrise (0.75 to 1.0)
            float t = (celestialAngle - 0.75F) / 0.25F;
            return lerpColor(midnightColor, sunriseColor, t);
        }
    }

    /**
     * Get sunrise/sunset colors (vanilla Minecraft style)
     * Returns null if no sunrise/sunset colors should be rendered
     */
    public float[] getSunriseSunsetColors(float celestialAngle) {
        // Only show sunrise/sunset colors near those times
        celestialAngle = celestialAngle % 1.0F;
        if (celestialAngle < 0) celestialAngle += 1.0F;

        // Sunrise period (around 0.0, from -0.1 to 0.1 normalized)
        if (celestialAngle > 0.9F || celestialAngle < 0.1F) {
            float normalizedAngle;
            if (celestialAngle > 0.9F) {
                normalizedAngle = (celestialAngle - 0.9F) / 0.2F; // 0.9 to 1.0
            } else {
                normalizedAngle = (celestialAngle + 0.1F) / 0.2F; // 0.0 to 0.1
            }

            // Peak intensity at exactly sunrise
            float intensity = 1.0F - Math.abs(normalizedAngle - 0.5F) * 2.0F;

            return new float[] {
                    (float) sunriseColor.x * intensity,
                    (float) sunriseColor.y * intensity,
                    (float) sunriseColor.z * intensity,
                    intensity
            };
        }

        // Sunset period (around 0.5, from 0.4 to 0.6)
        if (celestialAngle > 0.4F && celestialAngle < 0.6F) {
            float normalizedAngle = (celestialAngle - 0.4F) / 0.2F;
            float intensity = 1.0F - Math.abs(normalizedAngle - 0.5F) * 2.0F;

            return new float[] {
                    (float) sunsetColor.x * intensity,
                    (float) sunsetColor.y * intensity,
                    (float) sunsetColor.z * intensity,
                    intensity
            };
        }

        // No sunrise/sunset colors during other times
        return null;
    }

    public Vec3d getFogColor() {
        return fogColor;
    }

    public boolean useFogColor() {
        return useFogColor;
    }

    /**
     * Linear interpolation between two colors
     */
    private Vec3d lerpColor(Vec3d start, Vec3d end, float t) {
        return new Vec3d(
                start.x + (end.x - start.x) * t,
                start.y + (end.y - start.y) * t,
                start.z + (end.z - start.z) * t);
    }

    public static class Builder {

        private Vec3d sunriseColor = new Vec3d(1.0, 0.5, 0.2); // Orange
        private Vec3d noonColor = new Vec3d(0.5, 0.7, 1.0);     // Light blue
        private Vec3d sunsetColor = new Vec3d(1.0, 0.3, 0.1);   // Red-orange
        private Vec3d midnightColor = new Vec3d(0.0, 0.0, 0.1); // Dark blue
        private Vec3d fogColor = new Vec3d(0.8, 0.8, 0.8);      // Light gray
        private boolean useFogColor = false;

        public Builder sunriseColor(double r, double g, double b) {
            this.sunriseColor = new Vec3d(r, g, b);
            return this;
        }

        public Builder noonColor(double r, double g, double b) {
            this.noonColor = new Vec3d(r, g, b);
            return this;
        }

        public Builder sunsetColor(double r, double g, double b) {
            this.sunsetColor = new Vec3d(r, g, b);
            return this;
        }

        public Builder midnightColor(double r, double g, double b) {
            this.midnightColor = new Vec3d(r, g, b);
            return this;
        }

        public Builder fogColor(double r, double g, double b) {
            this.fogColor = new Vec3d(r, g, b);
            this.useFogColor = true;
            return this;
        }

        public Builder noFog() {
            this.useFogColor = false;
            return this;
        }

        public SkyColorData build() {
            return new SkyColorData(this);
        }
    }
}
