package supersymmetry.client.renderer.gun;

import net.minecraft.item.ItemStack;

/**
 * Plays one named clip at a time on the held gun. Clip names are the suffixes in the model's animation file
 * ({@code fire}, {@code reload}, ...), and their lengths come from that file, so adding an animation does not need
 * any changes here.
 */
final class GunAnimationPlayer {

    static final float NO_TICKS = -1f;

    private String clip;
    private boolean playing;
    private float ticks = NO_TICKS;
    private float lastTicks = NO_TICKS;
    private int pendingMagazine;

    void play(String name) {
        play(name, 0);
    }

    /** Starts a clip, and with it the magazine size the reload of this gun is about to insert. */
    void play(String name, int magazineSize) {
        if (name == null) {
            reset();
            return;
        }
        this.clip = name;
        this.playing = true;
        this.ticks = 0f;
        this.lastTicks = 0f;
        this.pendingMagazine = magazineSize;
    }

    void stop(String name) {
        if (isPlaying(name)) {
            reset();
        }
    }

    /** Whether the named clip is the one currently playing. */
    boolean isPlaying(String name) {
        return this.playing && name != null && name.equals(this.clip);
    }

    void reset() {
        this.clip = null;
        this.playing = false;
        this.ticks = NO_TICKS;
        this.lastTicks = NO_TICKS;
        // the pending magazine survives a clip ending, so the frame that draws the incoming magazine after the
        // reload does not fall back to the gun inventory before it has caught up with the reload
    }

    void update(ItemStack stack) {
        if (!this.playing) {
            return;
        }

        this.lastTicks = this.ticks;
        this.ticks += 1f;

        float length = GunItemStackRenderer.animationLength(stack, this.clip);
        if (length <= 0f || this.ticks >= length) {
            reset();
        }
    }

    /** Clip to apply this frame, or null when nothing is playing. */
    String getClip() {
        return this.playing ? this.clip : null;
    }

    /** Interpolated clip time for rendering, or {@link #NO_TICKS} when nothing is playing. */
    float renderTicks(float partialTicks) {
        if (!this.playing) {
            return NO_TICKS;
        }
        return this.lastTicks + (this.ticks - this.lastTicks) * partialTicks;
    }

    /**
     * Magazine size a reload of the current clip is about to insert, or zero when nothing is being played. Used by
     * the renderer to show the incoming magazine from the clip's {@code switch_mag} instruction on.
     */
    int pendingMagazine() {
        // kept after a clip ends, so the frame that draws the incoming magazine after the reload does not fall back
        // to the gun inventory before it has caught up with the reload
        return this.pendingMagazine;
    }
}
