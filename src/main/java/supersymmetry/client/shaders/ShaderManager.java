package supersymmetry.client.shaders;

import static codechicken.lib.render.shader.ShaderHelper.getStream;
import static codechicken.lib.render.shader.ShaderHelper.readShader;
import static codechicken.lib.render.shader.ShaderObject.ShaderType.FRAGMENT;
import static codechicken.lib.render.shader.ShaderObject.ShaderType.VERTEX;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL20;

import codechicken.lib.render.shader.ShaderObject;
import codechicken.lib.render.shader.ShaderProgram;

/**
 * Manages GLSL shaders for Supersymmetry.
 *
 * Pipeline overview:
 *
 * Bloom (Unreal-style):
 * 1. scene → emissive mask (EMISSIVE_MASK_F: discards non-bright pixels)
 * 2. mask → horiz blur (S_BLUR_F: separable Gaussian, horizontal pass)
 * 3. horiz → vert blur (S_BLUR_F: separable Gaussian, vertical pass)
 * 4. blurred + scene → composite (COMPOSITE_F: additive blend)
 *
 * Emissive mesh:
 * Rendered with EMISSIVE_V + EMISSIVE_F before the bloom pass so the
 * unlit, alpha-preserved colour feeds into the bloom threshold.
 */
@SideOnly(Side.CLIENT)
public class ShaderManager {

    public static final String MODID = "susy";
    private static final Logger LOGGER = LogManager.getLogger("ShaderManager");

    public static ShaderObject IMAGE_V;
    public static ShaderObject IMAGE_F;

    public static ShaderObject EMISSIVE_MASK_F;
    public static ShaderObject S_BLUR_F;
    public static ShaderObject COMPOSITE_F;

    public static ShaderObject EMISSIVE_V;
    public static ShaderObject EMISSIVE_F;

    public static ShaderObject TEST_V;
    public static ShaderObject TEST_F;

    public static ShaderObject SUN_V;
    public static ShaderObject SUN_F;
    public static ShaderObject ATMOSPHERE_V;
    public static ShaderObject ATMOSPHERE_F;

    private static final Map<ShaderObject, ShaderProgram> FULL_IMAGE_PROGRAMS = new HashMap<>();
    private static final Map<Long, ShaderProgram> PROGRAM_CACHE = new HashMap<>();
    private static final Minecraft MC = Minecraft.getMinecraft();

    private static BooleanSupplier isShaderPackLoaded;

    static {
        try {
            final Class<?> cl = Class.forName("net.optifine.shaders.Shaders");
            final Field field = cl.getDeclaredField("shaderPackLoaded");
            field.setAccessible(true);
            isShaderPackLoaded = () -> {
                try {
                    return field.getBoolean(null);
                } catch (final IllegalAccessException e) {
                    LOGGER.warn("Lost access to OptiFine shaderPackLoaded field – disabling integration.", e);
                    isShaderPackLoaded = null;
                    return false;
                }
            };
            LOGGER.info("OptiFine detected – shader-pack integration active.");
        } catch (ClassNotFoundException e) {
            LOGGER.info("OptiFine not found – shader-pack integration disabled.");
        } catch (NoSuchFieldException | NoClassDefFoundError e) {
            LOGGER.warn("OptiFine present but incompatible – shader-pack integration disabled.", e);
        }
    }

    public static void initShaders() {
        IMAGE_V = init(IMAGE_V, VERTEX, "image.vert");
        IMAGE_F = init(IMAGE_F, FRAGMENT, "image.frag");
        EMISSIVE_MASK_F = init(EMISSIVE_MASK_F, FRAGMENT, "emissive_mask.frag");
        S_BLUR_F = init(S_BLUR_F, FRAGMENT, "seperable_blur.frag");
        COMPOSITE_F = init(COMPOSITE_F, FRAGMENT, "composite.frag");
        EMISSIVE_V = init(EMISSIVE_V, VERTEX, "emissive.vert");
        EMISSIVE_F = init(EMISSIVE_F, FRAGMENT, "emissive.frag");
        TEST_V = init(TEST_V, VERTEX, "test.vert");
        TEST_F = init(TEST_F, FRAGMENT, "test.frag");
        SUN_V = init(SUN_V, VERTEX, "sun.vert");
        SUN_F = init(SUN_F, FRAGMENT, "sun.frag");
        ATMOSPHERE_V = init(ATMOSPHERE_V, VERTEX, "atmosphere_orbital.vert");
        ATMOSPHERE_F = init(ATMOSPHERE_F, FRAGMENT, "atmosphere_orbital.frag");
        FULL_IMAGE_PROGRAMS.clear();
        PROGRAM_CACHE.clear();
        // Delete existing raw GL programs before clearing cache
        for (int progId : RAW_PROGRAM_CACHE.values()) {
            if (progId > 0) GL20.glDeleteProgram(progId);
        }
        RAW_PROGRAM_CACHE.clear();
        rawTestProgram = -1;
    }

    public static boolean shadersAllowed() {
        // We intentionally do NOT gate on isOptiFineShaderPackLoaded() here.
        // The space renderer needs its own shaders even when OF is active because
        // it renders into a custom sky pass that OF doesn't touch.
        return OpenGlHelper.shadersSupported;
    }

    /**
     * Call this once per frame before using any shader.
     * Safe to call every frame – only compiles shaders on the first call.
     * This defers GL work until the context and assets are definitely ready.
     */
    public static void ensureInitialised() {
        if (!initialised && shadersAllowed()) {
            initialised = true;
            initShaders();
            LOGGER.info("ShaderManager: lazy init complete on render thread.");
        }
    }

    private static boolean initialised = false;

    /** Returns true when an OptiFine shader pack is active (shaders would conflict). */
    public static boolean isOptiFineShaderPackLoaded() {
        return isShaderPackLoaded != null && isShaderPackLoaded.getAsBoolean();
    }

    public static Minecraft getMC() {
        return MC;
    }

    /**
     * Renders a full-screen quad into {@code fbo} using {@code frag} as the
     * fragment shader. The uniform {@code u_resolution} is always set;
     * additional uniforms can be supplied via {@code uniformCache}.
     *
     * @param fbo          target framebuffer (returned unchanged on error)
     * @param frag         fragment shader to use
     * @param uniformCache additional uniform setter, may be null
     * @return the same {@code fbo} for chaining
     */
    public static Framebuffer renderFullImageInFBO(
                                                   Framebuffer fbo,
                                                   ShaderObject frag,
                                                   Consumer<ShaderProgram.UniformCache> uniformCache) {
        if (fbo == null || frag == null || !shadersAllowed()) return fbo;

        fbo.bindFramebuffer(true);

        ShaderProgram program = FULL_IMAGE_PROGRAMS.computeIfAbsent(frag, f -> {
            ShaderProgram p = new ShaderProgram();
            p.attachShader(IMAGE_V);
            p.attachShader(f);
            return p;
        });

        program.useShader(cache -> {
            cache.glUniform2F("u_resolution", fbo.framebufferWidth, fbo.framebufferHeight);
            if (uniformCache != null) uniformCache.accept(cache);
        });

        // Draw the full-screen quad (NDC)
        Tessellator tess = Tessellator.getInstance();
        BufferBuilder buf = tess.getBuffer();
        buf.begin(7, DefaultVertexFormats.POSITION_TEX);
        buf.pos(-1, 1, 0).tex(0, 0).endVertex();
        buf.pos(-1, -1, 0).tex(0, 1).endVertex();
        buf.pos(1, -1, 0).tex(1, 1).endVertex();
        buf.pos(1, 1, 0).tex(1, 0).endVertex();
        tess.draw();

        program.releaseShader();
        return fbo;
    }

    /**
     * Returns (creating if necessary) a {@link ShaderProgram} that pairs the
     * given vertex and fragment shaders. Used by space renderers that need
     * their own vert shader rather than the shared IMAGE_V quad shader.
     *
     * Keyed by object identity so that if initShaders() replaces a ShaderObject
     * with a fresh instance the old (now dead) program is not reused.
     */
    public static ShaderProgram getOrCreateProgram(ShaderObject vert, ShaderObject frag) {
        if (vert == null || frag == null) return null;
        // Identity hash is fine here: initShaders() clears PROGRAM_CACHE so stale
        // entries from old ShaderObject instances never survive a reload.
        long key = ((long) System.identityHashCode(vert) << 32) | (System.identityHashCode(frag) & 0xFFFFFFFFL);
        return PROGRAM_CACHE.computeIfAbsent(key, k -> {
            ShaderProgram p = new ShaderProgram();
            p.attachShader(vert);
            p.attachShader(frag);
            return p;
        });
    }

    // Maps ShaderObject identity pair → raw GL program ID.
    // Used instead of CCL ShaderProgram to avoid state management issues.
    private static final Map<String, Integer> RAW_PROGRAM_CACHE = new HashMap<>();
    private static int rawTestProgram = -1;

    /**
     * Compiles and links a raw GL program from shader source files in assets.
     * Completely bypasses CCL – reads the .vert/.frag files directly and
     * compiles them via GL20. Cached by filename pair.
     */
    public static int getRawProgram(String vertFile, String fragFile) {
        String key = vertFile + "|" + fragFile;
        if (RAW_PROGRAM_CACHE.containsKey(key)) {
            return RAW_PROGRAM_CACHE.get(key);
        }
        try {
            String vertSrc = readShader(getStream(
                    String.format("/assets/%s/shaders/%s", MODID, vertFile)));
            String fragSrc = readShader(getStream(
                    String.format("/assets/%s/shaders/%s", MODID, fragFile)));

            int vId = compileRaw(GL20.GL_VERTEX_SHADER, vertFile, vertSrc);
            int fId = compileRaw(GL20.GL_FRAGMENT_SHADER, fragFile, fragSrc);
            if (vId <= 0 || fId <= 0) return -1;

            int p = GL20.glCreateProgram();
            GL20.glAttachShader(p, vId);
            GL20.glAttachShader(p, fId);
            GL20.glLinkProgram(p);
            GL20.glDeleteShader(vId);
            GL20.glDeleteShader(fId);

            String log = GL20.glGetProgramInfoLog(p, 512).trim();
            if (!log.isEmpty()) LOGGER.warn("getRawProgram [{}+{}] link log: {}", vertFile, fragFile, log);
            if (GL20.glGetProgrami(p, GL20.GL_LINK_STATUS) == 0) {
                LOGGER.error("getRawProgram: link failed [{} + {}]", vertFile, fragFile);
                GL20.glDeleteProgram(p);
                RAW_PROGRAM_CACHE.put(key, -1);
                return -1;
            }
            LOGGER.info("getRawProgram: linked [{} + {}] id={}", vertFile, fragFile, p);
            RAW_PROGRAM_CACHE.put(key, p);
            return p;
        } catch (Exception e) {
            LOGGER.error("getRawProgram exception [{} + {}]", vertFile, fragFile, e);
            return -1;
        }
    }

    private static int compileRaw(int type, String name, String src) {
        int id = GL20.glCreateShader(type);
        GL20.glShaderSource(id, src);
        GL20.glCompileShader(id);
        String log = GL20.glGetShaderInfoLog(id, 512).trim();
        if (!log.isEmpty()) LOGGER.warn("compileRaw [{}] log: {}", name, log);
        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == 0) {
            LOGGER.error("compileRaw: failed to compile [{}]", name);
            GL20.glDeleteShader(id);
            return -1;
        }
        return id;
    }

    /**
     * Returns a raw GL program ID for the minimal test.vert + test.frag shaders,
     * compiled and linked entirely via GL20 with no CCL involvement.
     * Returns -1 if compilation or linking failed.
     */
    public static int getRawTestProgram() {
        if (rawTestProgram != -1) return rawTestProgram;
        try {
            String vertSrc = "#version 330 core\n" +
                    "layout(location = 0) in vec2 aPos;\n" +
                    "void main() { gl_Position = vec4(aPos, 0.9999, 1.0); }\n";
            String fragSrc = "#version 330 core\n" +
                    "out vec4 FragColor;\n" +
                    "void main() { FragColor = vec4(1.0, 0.0, 0.0, 1.0); }\n";

            int v = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
            GL20.glShaderSource(v, vertSrc);
            GL20.glCompileShader(v);
            String vLog = GL20.glGetShaderInfoLog(v, 512);
            if (!vLog.trim().isEmpty()) LOGGER.warn("[RawTest] vert log: {}", vLog);
            if (GL20.glGetShaderi(v, GL20.GL_COMPILE_STATUS) == 0) {
                LOGGER.error("[RawTest] vert compile failed");
                return -1;
            }

            int f = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
            GL20.glShaderSource(f, fragSrc);
            GL20.glCompileShader(f);
            String fLog = GL20.glGetShaderInfoLog(f, 512);
            if (!fLog.trim().isEmpty()) LOGGER.warn("[RawTest] frag log: {}", fLog);
            if (GL20.glGetShaderi(f, GL20.GL_COMPILE_STATUS) == 0) {
                LOGGER.error("[RawTest] frag compile failed");
                return -1;
            }

            int p = GL20.glCreateProgram();
            GL20.glAttachShader(p, v);
            GL20.glAttachShader(p, f);
            GL20.glLinkProgram(p);
            String pLog = GL20.glGetProgramInfoLog(p, 512);
            if (!pLog.trim().isEmpty()) LOGGER.warn("[RawTest] link log: {}", pLog);
            if (GL20.glGetProgrami(p, GL20.GL_LINK_STATUS) == 0) {
                LOGGER.error("[RawTest] link failed");
                return -1;
            }

            GL20.glDeleteShader(v);
            GL20.glDeleteShader(f);
            rawTestProgram = p;
            LOGGER.info("[RawTest] program compiled and linked, id={}", p);
            return p;
        } catch (Exception e) {
            LOGGER.error("[RawTest] exception during compile", e);
            return -1;
        }
    }

    private static ShaderObject init(ShaderObject old, ShaderObject.ShaderType type, String file) {
        if (old != null) old.disposeObject();
        try {
            String path = String.format("/assets/%s/shaders/%s", MODID, file);
            return new ShaderObject(type, readShader(getStream(path))).compileShader();
        } catch (Exception e) {
            LOGGER.error("Failed to compile shader '{}': {}", file, e.getMessage(), e);
            return null;
        }
    }
}
