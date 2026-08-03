package supersymmetry.api.space.dimension;

import javax.annotation.Nullable;

import org.eclipse.lsp4j.jsonrpc.validation.NonNull;

import supersymmetry.api.space.RenderableCelestialObject;

/**
 * Represents a cosmic environment for a Minecraft dimension:
 * - The objects visible in the sky (stars, planets, moons)
 * - The main body you're orbiting (e.g. Earth, Mars)
 * - Static or moving background stars
 * - Space environmental settings for rendering and gameplay
 */
public class SpaceEnvironment {

    /** The main object the player is orbiting (could be a planet or star) */
    @Nullable
    public RenderableCelestialObject objectToOrbit;

    /** All objects visible in this dimension (planets, moons, stars) */
    @NonNull
    public RenderableCelestialObject[] visibleObjects;

    /** Random background stars or skybox texture (optional) */
    @Nullable
    public int starfieldTextureId = -1;

    /** Whether to render a cubemap/starfield around the player */
    public boolean renderStarfield = true;

    /** Space ambient lighting (0–1) */
    public float ambientLight = 0.02f;

    /** Simulated time scale multiplier for orbital animation */
    public double timeScale = 1.0;

    /** If gravity exists here */
    public boolean hasGravity = false;

    /** Custom gravity amount (ignored if hasGravity = false) */
    public float gravityStrength = 0.08f;

    /** Temperature for environmental effects */
    public float ambientTemperature = 3.0f; // near absolute zero

    /** Should fog be rendered? Space usually has none. */
    public boolean renderFog = false;

    public SpaceEnvironment(
                            @Nullable RenderableCelestialObject objectToOrbit,
                            @NonNull RenderableCelestialObject[] visibleObjects) {
        this.objectToOrbit = objectToOrbit;
        this.visibleObjects = visibleObjects;
    }
}
