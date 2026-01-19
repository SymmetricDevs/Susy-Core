package supersymmetry.common.world.sky;

import net.minecraft.util.ResourceLocation;

public class SkyRenderData {

    private final ResourceLocation texture;
    private final float size;
    private final PositionType positionType;
    private final float azimuthOffset;
    private final PhaseData phaseData;
    private final float brightness;
    private final boolean useLinearFiltering;
    private final boolean mirrorTexture;
    private final float rotationX;
    private final float rotationZ;

    private boolean reversePhases;

    private SkyRenderData(Builder builder) {
        this.texture = builder.texture;
        this.size = builder.size;
        this.positionType = builder.positionType;
        this.azimuthOffset = builder.azimuthOffset;
        this.phaseData = builder.phaseData;
        this.brightness = builder.brightness;
        this.useLinearFiltering = builder.useLinearFiltering;
        this.mirrorTexture = builder.mirrorTexture;
        this.rotationX = builder.rotationX;
        this.rotationZ = builder.rotationZ;
    }

    public ResourceLocation getTexture() {
        return texture;
    }

    public float getSize() {
        return size;
    }

    public PositionType getPositionType() {
        return positionType;
    }

    public float getAzimuthOffset() {
        return azimuthOffset;
    }

    public PhaseData getPhaseData() {
        return phaseData;
    }

    public float getBrightness() {
        return brightness;
    }

    public boolean useLinearFiltering() {
        return useLinearFiltering;
    }

    public boolean shouldMirrorTexture() {
        return mirrorTexture;
    }

    public float getRotationX() {
        return rotationX;
    }

    public float getRotationZ() {
        return rotationZ;
    }

    public boolean hasPhases() {
        return phaseData != null;
    }

    public int getCurrentPhase(long worldTime) {
        if (phaseData == null) return 0;

        float minecraftDays = worldTime / 24000.0f;
        float phaseProgress = (minecraftDays % phaseData.cycleLength) / phaseData.cycleLength;
        int phase = (int) (phaseProgress * phaseData.totalPhases) % phaseData.totalPhases;

        if (reversePhases) {
            phase = (phaseData.totalPhases - 1 - phase) % phaseData.totalPhases;
        }

        return phase;
    }

    public enum PositionType {
        CELESTIAL_SPHERE,  // Follows the day/night cycle
        ZENITH,             // Fixed at zenith (like Earth from Moon)
        CUSTOM
    }

    public static class PhaseData {

        private final int columns;
        private final int rows;
        private final int totalPhases;
        private final float cycleLength; // In Minecraft days

        public PhaseData(int columns, int rows, float cycleLength) {
            this.columns = columns;
            this.rows = rows;
            this.totalPhases = columns * rows;
            this.cycleLength = cycleLength;
        }

        public int getColumns() {
            return columns;
        }

        public int getRows() {
            return rows;
        }

        public int getTotalPhases() {
            return totalPhases;
        }

        public float getCycleLength() {
            return cycleLength;
        }
    }

    public static class Builder {

        private final ResourceLocation texture;
        private final float size;
        private PositionType positionType = PositionType.CELESTIAL_SPHERE;
        private float azimuthOffset = 0.0F;
        private PhaseData phaseData = null;
        private float brightness = 1.0F;
        private boolean useLinearFiltering = false;
        private boolean mirrorTexture = false;
        private float rotationX = 0.0F;
        private float rotationZ = 0.0F;
        private boolean reversePhases = false;

        public Builder reversePhases(boolean reverse) {
            this.reversePhases = reverse;
            return this;
        }

        public Builder(ResourceLocation texture, float size) {
            this.texture = texture;
            this.size = size;
        }

        public Builder positionType(PositionType type) {
            this.positionType = type;
            return this;
        }

        public Builder azimuthOffset(float offset) {
            this.azimuthOffset = offset;
            return this;
        }

        public Builder phases(int columns, int rows, float cycleLength) {
            this.phaseData = new PhaseData(columns, rows, cycleLength);
            return this;
        }

        public Builder brightness(float brightness) {
            this.brightness = brightness;
            return this;
        }

        public Builder useLinearFiltering(boolean use) {
            this.useLinearFiltering = use;
            return this;
        }

        public Builder mirrorTexture(boolean mirror) {
            this.mirrorTexture = mirror;
            return this;
        }

        public Builder rotationX(float rotation) {
            this.rotationX = rotation;
            return this;
        }

        public Builder rotationZ(float rotation) {
            this.rotationZ = rotation;
            return this;
        }

        public SkyRenderData build() {
            return new SkyRenderData(this);
        }
    }
}
