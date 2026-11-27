package supersymmetry.common.guns;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.item.ItemStack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import gregtech.api.items.metaitem.MetaItem;
import supersymmetry.Supersymmetry;

/**
 * Clip lengths for the server. The client reads the animation files through its resource manager, which only exists
 * on the client, so the server reads the same files from the classpath instead. How long an animation locks the gun
 * therefore always comes from the mod's own files: a resource pack can change how a clip looks, but not how long it
 * keeps the player from using the gun.
 * <p>
 * Clips are addressed by suffix ({@code fire}, {@code reload}, {@code equip}, ...), exactly like the client does, so
 * adding a clip to a model's animation file is enough for the server to see it as well.
 */
public final class GunClipLengths {

    private static final String RESOURCE_ROOT = "/assets/" + Supersymmetry.MODID + "/guns/animations/";
    private static final String RESOURCE_SUFFIX = ".animation.json";

    private static final String ANIMATIONS = "animations";
    private static final String BONES = "bones";
    private static final String LENGTH = "animation_length";
    private static final float TICKS_PER_SECOND = 20f;

    // parsing a file is only needed once per model, and the server ticks are all on one thread
    private static final Map<String, Map<String, Float>> CACHE = new HashMap<>();

    private GunClipLengths() {}

    /**
     * Length of a clip on the given gun in ticks, or zero when the gun or the clip does not exist.
     */
    public static float of(ItemStack stack, String clip) {
        if (stack == null || stack.isEmpty() || !(stack.getItem() instanceof MetaItem)) {
            return 0f;
        }
        // a gun's model, and therefore its animation file, is named after the item's unlocalized name
        MetaItem<?>.MetaValueItem item = ((MetaItem<?>) stack.getItem()).getItem(stack);
        return item == null ? 0f : of(item.unlocalizedName, clip);
    }

    /**
     * Length of a clip on the model with the given name in ticks, or zero when the model or the clip does not exist.
     */
    public static float of(String model, String clip) {
        if (model == null || clip == null) {
            return 0f;
        }
        Float length = CACHE.computeIfAbsent(model, GunClipLengths::load).get(clip);
        return length == null ? 0f : length;
    }

    private static Map<String, Float> load(String model) {
        String path = RESOURCE_ROOT + model + RESOURCE_SUFFIX;
        try (InputStream stream = GunClipLengths.class.getResourceAsStream(path)) {
            if (stream == null) {
                return Collections.emptyMap();
            }
            Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8);
            JsonElement root = new JsonParser().parse(reader);
            if (!root.isJsonObject()) {
                return Collections.emptyMap();
            }

            JsonElement animations = root.getAsJsonObject().get(ANIMATIONS);
            if (animations == null || !animations.isJsonObject()) {
                return Collections.emptyMap();
            }

            String prefix = model + ".";
            Map<String, Float> lengths = new HashMap<>();
            for (Map.Entry<String, JsonElement> entry : animations.getAsJsonObject().entrySet()) {
                String name = entry.getKey();
                if (name == null || !name.startsWith(prefix) || name.length() == prefix.length() ||
                        !entry.getValue().isJsonObject()) {
                    continue;
                }
                lengths.put(name.substring(prefix.length()), readLength(entry.getValue().getAsJsonObject()));
            }
            return lengths;
        } catch (IOException | RuntimeException e) {
            // a missing or malformed file simply means that model has no timed clips
            return Collections.emptyMap();
        }
    }

    /**
     * Length in ticks. {@code animation_length} is authored in seconds and is what the client uses as well; without
     * it the clip lasts until its last keyframe, the same fallback the client uses.
     */
    private static float readLength(JsonObject clip) {
        JsonElement authored = clip.get(LENGTH);
        if (authored != null && !authored.isJsonNull()) {
            try {
                return (float) (authored.getAsDouble() * TICKS_PER_SECOND);
            } catch (RuntimeException e) {
                // fall through to the last keyframe time
            }
        }

        JsonElement bones = clip.get(BONES);
        if (bones == null || !bones.isJsonObject()) {
            return 0f;
        }

        float longest = 0f;
        for (Map.Entry<String, JsonElement> bone : bones.getAsJsonObject().entrySet()) {
            if (!bone.getValue().isJsonObject()) {
                continue;
            }
            for (Map.Entry<String, JsonElement> channel : bone.getValue().getAsJsonObject().entrySet()) {
                longest = Math.max(longest, lastKeyframeTime(channel.getValue()));
            }
        }
        return longest;
    }

    private static float lastKeyframeTime(JsonElement channel) {
        if (channel == null || !channel.isJsonObject()) {
            // a constant value applies for the whole clip
            return 0f;
        }

        float longest = 0f;
        for (Map.Entry<String, JsonElement> time : channel.getAsJsonObject().entrySet()) {
            try {
                longest = Math.max(longest, (float) (Double.parseDouble(time.getKey()) * TICKS_PER_SECOND));
            } catch (NumberFormatException e) {
                // molang and the other non numeric keys of a channel do not describe a time
            }
        }
        return longest;
    }
}
