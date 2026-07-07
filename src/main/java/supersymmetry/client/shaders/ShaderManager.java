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

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL43;

import codechicken.lib.render.shader.ShaderObject;
import codechicken.lib.render.shader.ShaderProgram;
import supersymmetry.Supersymmetry;
import supersymmetry.api.SusyLog;

@SideOnly(Side.CLIENT)
public class ShaderManager {

    public static ShaderObject IMAGE_V;
    public static ShaderObject EMISSIVE_MASK_F;
    public static ShaderObject S_BLUR_F;
    public static ShaderObject COMPOSITE_F;

    private static final Map<ShaderObject, ShaderProgram> FULL_IMAGE_PROGRAMS = new HashMap<>();
    private static final Map<String, Integer> RAW_PROGRAM_CACHE = new HashMap<>();
    private static boolean initialised = false;
    private static BooleanSupplier isShaderPackLoaded;

    static {
        try {
            Class<?> cl = Class.forName("net.optifine.shaders.Shaders");
            Field field = cl.getDeclaredField("shaderPackLoaded");
            field.setAccessible(true);
            isShaderPackLoaded = () -> {
                try {
                    return field.getBoolean(null);
                } catch (IllegalAccessException e) {
                    SusyLog.logger.warn("Lost access to OptiFine shaderPackLoaded field", e);
                    isShaderPackLoaded = null;
                    return false;
                }
            };
        } catch (ClassNotFoundException e) {
            // OptiFine not present
        } catch (NoSuchFieldException | NoClassDefFoundError e) {
            SusyLog.logger.warn("OptiFine present but incompatible", e);
        }
    }

    public static boolean shadersAllowed() {
        return OpenGlHelper.shadersSupported;
    }

    public static void ensureInitialised() {
        if (!initialised && shadersAllowed()) {
            initialised = true;
            initShaders();
        }
    }

    public static boolean isOptiFineShaderPackLoaded() {
        return isShaderPackLoaded != null && isShaderPackLoaded.getAsBoolean();
    }

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

    public static int getRawProgram(String vertFile, String fragFile) {
        String key = vertFile + "_" + fragFile;
        Integer cached = RAW_PROGRAM_CACHE.get(key);
        if (cached != null) return cached;

        try {
            String vertSrc = readShader(getStream(path(vertFile)));
            String fragSrc = readShader(getStream(path(fragFile)));

            int v = compileShader(GL20.GL_VERTEX_SHADER, vertFile, vertSrc);
            int f = compileShader(GL20.GL_FRAGMENT_SHADER, fragFile, fragSrc);
            if (v <= 0 || f <= 0) return -1;

            int p = GL20.glCreateProgram();
            GL20.glAttachShader(p, v);
            GL20.glAttachShader(p, f);
            GL20.glLinkProgram(p);
            GL20.glDeleteShader(v);
            GL20.glDeleteShader(f);

            if (GL20.glGetProgrami(p, GL20.GL_LINK_STATUS) == 0) {
                SusyLog.logger.error("getRawProgram: link failed [{} + {}]", vertFile, fragFile);
                GL20.glDeleteProgram(p);
                RAW_PROGRAM_CACHE.put(key, -1);
                return -1;
            }
            RAW_PROGRAM_CACHE.put(key, p);
            return p;
        } catch (Exception e) {
            SusyLog.logger.error("getRawProgram [{} + {}]", vertFile, fragFile, e);
            return -1;
        }
    }

    public static int getRawComputeProgram(String compFile) {
        String key = "compute_" + compFile;
        Integer cached = RAW_PROGRAM_CACHE.get(key);
        if (cached != null) return cached;

        try {
            String src = readShader(getStream(path(compFile)));

            int shader = compileShader(GL43.GL_COMPUTE_SHADER, compFile, src);
            if (shader <= 0) return -1;

            int p = GL20.glCreateProgram();
            GL20.glAttachShader(p, shader);
            GL20.glLinkProgram(p);
            GL20.glDeleteShader(shader);

            if (GL20.glGetProgrami(p, GL20.GL_LINK_STATUS) == 0) {
                SusyLog.logger.error("getRawComputeProgram: link failed [{}]", compFile);
                GL20.glDeleteProgram(p);
                RAW_PROGRAM_CACHE.put(key, -1);
                return -1;
            }
            RAW_PROGRAM_CACHE.put(key, p);
            return p;
        } catch (Exception e) {
            SusyLog.logger.error("getRawComputeProgram [{}]", compFile, e);
            return -1;
        }
    }

    private static String path(String file) {
        return String.format("/assets/%s/shaders/%s", Supersymmetry.MODID, file);
    }

    private static int compileShader(int type, String name, String src) {
        int id = GL20.glCreateShader(type);
        GL20.glShaderSource(id, src);
        GL20.glCompileShader(id);
        if (GL20.glGetShaderi(id, GL20.GL_COMPILE_STATUS) == 0) {
            SusyLog.logger.error("compileShader: failed [{}]", name);
            GL20.glDeleteShader(id);
            return -1;
        }
        return id;
    }

    public static void initShaders() {
        IMAGE_V = initShader(IMAGE_V, VERTEX, "image.vert");
        EMISSIVE_MASK_F = initShader(EMISSIVE_MASK_F, FRAGMENT, "emissive_mask.frag");
        S_BLUR_F = initShader(S_BLUR_F, FRAGMENT, "seperable_blur.frag");
        COMPOSITE_F = initShader(COMPOSITE_F, FRAGMENT, "composite.frag");

        FULL_IMAGE_PROGRAMS.clear();
        for (int id : RAW_PROGRAM_CACHE.values()) {
            if (id > 0) GL20.glDeleteProgram(id);
        }
        RAW_PROGRAM_CACHE.clear();
    }

    private static ShaderObject initShader(ShaderObject old, ShaderObject.ShaderType type, String file) {
        if (old != null) old.disposeObject();
        try {
            return new ShaderObject(type, readShader(getStream(path(file)))).compileShader();
        } catch (Exception e) {
            SusyLog.logger.error("Failed to compile shader '{}'", file, e);
            return null;
        }
    }
}
