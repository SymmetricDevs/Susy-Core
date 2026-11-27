package supersymmetry.client.renderer.gun;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import software.bernie.geckolib3.geo.render.built.GeoBone;
import software.bernie.geckolib3.geo.render.built.GeoModel;

/**
 * custom reader for geckolib animation files, since the default animation controller doesn't work
 */
public final class GunAnimation {

    private static final String ANIMATIONS = "animations";
    private static final String BONES = "bones";
    private static final String LENGTH = "animation_length";
    private static final String ROTATION = "rotation";
    private static final String POSITION = "position";
    private static final String SCALE = "scale";
    private static final String POST = "post";
    private static final String PRE = "pre";
    private static final String PARTICLE_EFFECTS = "particle_effects";
    private static final String SOUND_EFFECTS = "sound_effects";
    private static final String TIMELINE = "timeline";

    private static final float TICKS_PER_SECOND = 20f;

    private final Map<String, BoneClip> clips;
    private final float length;

    private final List<ParticleEvent> particleEvents = new ArrayList<>();
    private final List<SoundKeyFrame> soundKeyFrames = new ArrayList<>();
    private final List<InstructionEvent> instructionEvents = new ArrayList<>();
    private Consumer<ParticleEvent> particleListener;
    private Consumer<SoundKeyFrame> soundListener;
    private int nextParticleEvent;
    private int nextSoundKeyFrame;
    private float lastAppliedTicks = -1f;

    private final List<GeoBone> bones = new ArrayList<>();
    private final Map<String, GeoBone> bonesByName = new HashMap<>();
    private final Map<String, RestPose> restPoses = new HashMap<>();

    private GunAnimation(GeoModel model, Map<String, BoneClip> clips, float length,
                         List<ParticleEvent> particleEvents, List<SoundKeyFrame> soundKeyFrames,
                         List<InstructionEvent> instructionEvents) {
        this.clips = clips;
        this.length = length;
        this.particleEvents.addAll(particleEvents);
        this.soundKeyFrames.addAll(soundKeyFrames);
        this.instructionEvents.addAll(instructionEvents);
        for (GeoBone bone : model.topLevelBones) {
            collect(bone);
        }
    }

    private void collect(GeoBone bone) {
        this.bones.add(bone);
        this.bonesByName.put(bone.getName(), bone);
        this.restPoses.put(bone.getName(), new RestPose(bone));
        for (GeoBone child : bone.childBones) {
            collect(child);
        }
    }

    /**
     * Length in ticks. The authored {@code animation_length} is in seconds, and GeckoLib works in ticks, so the
     * values here match what everything else expects.
     */
    float length() {
        return this.length;
    }

    /**
     * Whether a timeline instruction has been reached by {@code ticks}. The lookup is stateless and always reflects
     * the current time, so replaying a clip resets it without any listener bookkeeping.
     */
    boolean hasInstruction(String instruction, double ticks) {
        if (instruction == null) {
            return false;
        }
        for (InstructionEvent event : this.instructionEvents) {
            if (event.tick > ticks) {
                return false;
            }
            if (instruction.equals(event.instruction)) {
                return true;
            }
        }
        return false;
    }

    void setParticleListener(Consumer<ParticleEvent> listener) {
        this.particleListener = listener;
    }

    void setSoundListener(Consumer<SoundKeyFrame> listener) {
        this.soundListener = listener;
    }

    void reset() {
        for (GeoBone bone : this.bones) {
            RestPose rest = this.restPoses.get(bone.getName());
            bone.setRotationX(rest.rotationX);
            bone.setRotationY(rest.rotationY);
            bone.setRotationZ(rest.rotationZ);
            bone.setPositionX(rest.positionX);
            bone.setPositionY(rest.positionY);
            bone.setPositionZ(rest.positionZ);
            bone.setScaleX(rest.scaleX);
            bone.setScaleY(rest.scaleY);
            bone.setScaleZ(rest.scaleZ);
        }
    }

    void apply(double ticks) {
        if (ticks < this.lastAppliedTicks) {
            // a new play of this clip started, so its events fire again from the top of the clip
            this.nextParticleEvent = 0;
            this.nextSoundKeyFrame = 0;
        }
        this.lastAppliedTicks = (float) ticks;

        for (Map.Entry<String, BoneClip> entry : this.clips.entrySet()) {
            GeoBone bone = this.bonesByName.get(entry.getKey());
            if (bone == null) {
                continue;
            }

            RestPose rest = this.restPoses.get(entry.getKey());
            BoneClip clip = entry.getValue();

            float[] rotation = clip.sample(ROTATION, ticks);
            if (rotation != null) {
                bone.setRotationX(rest.rotationX + rotation[0]);
                bone.setRotationY(rest.rotationY + rotation[1]);
                bone.setRotationZ(rest.rotationZ + rotation[2]);
            }

            float[] position = clip.sample(POSITION, ticks);
            if (position != null) {
                bone.setPositionX(position[0]);
                bone.setPositionY(position[1]);
                bone.setPositionZ(position[2]);
            }

            float[] scale = clip.sample(SCALE, ticks);
            if (scale != null) {
                bone.setScaleX(scale[0]);
                bone.setScaleY(scale[1]);
                bone.setScaleZ(scale[2]);
            }
        }

        // events fire after the bones are posed, so a locator attached to an animated bone is already in its
        // position for this tick when the listener looks it up
        fireParticleEvents(ticks);
        fireSoundEvents(ticks);
    }

    private void fireParticleEvents(double ticks) {
        while (this.nextParticleEvent < this.particleEvents.size() &&
                this.particleEvents.get(this.nextParticleEvent).tick <= ticks) {
            Consumer<ParticleEvent> listener = this.particleListener;
            if (listener != null) {
                listener.accept(this.particleEvents.get(this.nextParticleEvent));
            }
            this.nextParticleEvent++;
        }
    }

    private void fireSoundEvents(double ticks) {
        while (this.nextSoundKeyFrame < this.soundKeyFrames.size() &&
                this.soundKeyFrames.get(this.nextSoundKeyFrame).tick <= ticks) {
            Consumer<SoundKeyFrame> listener = this.soundListener;
            if (listener != null) {
                listener.accept(this.soundKeyFrames.get(this.nextSoundKeyFrame));
            }
            this.nextSoundKeyFrame++;
        }
    }

    /**
     * An animation that only knows the model's authored pose, so it can undo any clip that was applied to it.
     */
    static GunAnimation rest(GeoModel model) {
        return new GunAnimation(model, new HashMap<>(), 0f, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Loads every animation whose name starts with {@code prefix}, keyed by the part after the prefix. A gun model
     * therefore exposes its clips by suffix ({@code fire}, {@code reload}, ...), and adding another clip to the
     * animation file is enough for the rest of the system to find it.
     * <p>
     * Returns an empty map when the file or all animations are missing, so a model without animations still renders.
     */
    static Map<String, GunAnimation> loadAll(IResourceManager manager, ResourceLocation location, String prefix,
                                             GeoModel model) {
        Map<String, GunAnimation> loaded = new HashMap<>();
        JsonObject root = readJson(manager, location);
        if (root == null) {
            return loaded;
        }

        JsonElement animations = root.get(ANIMATIONS);
        if (animations == null || !animations.isJsonObject()) {
            return loaded;
        }

        for (Map.Entry<String, JsonElement> entry : animations.getAsJsonObject().entrySet()) {
            String name = entry.getKey();
            if (name == null || !name.startsWith(prefix) || name.length() == prefix.length()) {
                continue;
            }
            if (!entry.getValue().isJsonObject()) {
                continue;
            }

            JsonObject clip = entry.getValue().getAsJsonObject();
            Map<String, BoneClip> clips = readBones(clip);
            loaded.put(name.substring(prefix.length()), new GunAnimation(model, clips, readLength(clip, clips),
                    readParticleEvents(clip), readSoundEvents(clip), readInstructionEvents(clip)));
        }
        return loaded;
    }

    private static JsonObject readJson(IResourceManager manager, ResourceLocation location) {
        try (IResource resource = manager.getResource(location);
                Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return new JsonParser().parse(reader).getAsJsonObject();
        } catch (IOException e) {
            return null;
        }
    }

    private static Map<String, BoneClip> readBones(JsonObject clip) {
        Map<String, BoneClip> bones = new HashMap<>();
        JsonElement element = clip.get(BONES);
        if (element == null || !element.isJsonObject()) {
            return bones;
        }

        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            if (!entry.getValue().isJsonObject()) {
                continue;
            }
            BoneClip bone = readBone(entry.getValue().getAsJsonObject());
            if (!bone.isEmpty()) {
                bones.put(entry.getKey(), bone);
            }
        }
        return bones;
    }

    private static BoneClip readBone(JsonObject json) {
        BoneClip bone = new BoneClip();
        bone.put(ROTATION, readChannel(json.get(ROTATION), true));
        bone.put(POSITION, readChannel(json.get(POSITION), false));
        bone.put(SCALE, readChannel(json.get(SCALE), false));
        return bone;
    }

    /**
     * Particle events of a clip, as authored in Blockbench's animation effects. The keys are the times of the effect in
     * seconds, and every entry fires once when the clip reaches that time; the effect names the particle, the locator
     * the bone it is positioned on and the script that tweaks it.
     */
    private static List<ParticleEvent> readParticleEvents(JsonObject clip) {
        List<ParticleEvent> events = new ArrayList<>();
        JsonElement element = clip.get(PARTICLE_EFFECTS);
        if (element == null || !element.isJsonObject()) {
            return events;
        }

        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            Double seconds = parseTime(entry.getKey());
            if (seconds == null || !entry.getValue().isJsonObject()) {
                continue;
            }

            JsonObject json = entry.getValue().getAsJsonObject();
            JsonElement effect = json.get("effect");
            if (effect == null || effect.isJsonNull()) {
                continue;
            }

            events.add(new ParticleEvent((float) (seconds * TICKS_PER_SECOND), effect.getAsString(),
                    readString(json, "locator"), readString(json, "script")));
        }

        events.sort(Comparator.comparingDouble(event -> event.tick));
        return events;
    }

    /**
     * Sound events of a clip, as authored in Blockbench's animation effects. Like GeckoLib, the keys are the times of
     * the sound in seconds and every entry plays once per play of the clip; the value names the sound event.
     */
    private static List<SoundKeyFrame> readSoundEvents(JsonObject clip) {
        List<SoundKeyFrame> events = new ArrayList<>();
        JsonElement element = clip.get(SOUND_EFFECTS);
        if (element == null || !element.isJsonObject()) {
            return events;
        }

        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            Double seconds = parseTime(entry.getKey());
            if (seconds == null || !entry.getValue().isJsonObject()) {
                continue;
            }

            JsonElement effect = entry.getValue().getAsJsonObject().get("effect");
            if (effect == null || effect.isJsonNull()) {
                continue;
            }

            events.add(new SoundKeyFrame((float) (seconds * TICKS_PER_SECOND), effect.getAsString()));
        }

        events.sort(Comparator.comparingDouble(event -> event.tick));
        return events;
    }

    /**
     * Timeline instructions of a clip, as authored in Blockbench's timeline. The keys are the times in seconds and
     * the values are the instruction text, either a single string or an array of strings at the same time. A clip can
     * use them to change render state part way through, such as which magazine is shown during a reload.
     */
    private static List<InstructionEvent> readInstructionEvents(JsonObject clip) {
        List<InstructionEvent> events = new ArrayList<>();
        JsonElement element = clip.get(TIMELINE);
        if (element == null || !element.isJsonObject()) {
            return events;
        }

        for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
            Double seconds = parseTime(entry.getKey());
            if (seconds == null) {
                continue;
            }

            float tick = (float) (seconds * TICKS_PER_SECOND);
            JsonElement value = entry.getValue();
            if (value.isJsonArray()) {
                for (JsonElement child : value.getAsJsonArray()) {
                    String instruction = readInstruction(child);
                    if (instruction != null) {
                        events.add(new InstructionEvent(tick, instruction));
                    }
                }
            } else {
                String instruction = readInstruction(value);
                if (instruction != null) {
                    events.add(new InstructionEvent(tick, instruction));
                }
            }
        }

        events.sort(Comparator.comparingDouble(event -> event.tick));
        return events;
    }

    private static String readInstruction(JsonElement element) {
        if (element == null || element.isJsonNull() || !element.isJsonPrimitive()) {
            return null;
        }
        String instruction = element.getAsString().trim();
        return instruction.isEmpty() ? null : instruction;
    }

    private static String readString(JsonObject json, String name) {
        JsonElement element = json.get(name);
        return element == null || element.isJsonNull() ? "" : element.getAsString();
    }

    private static Channel readChannel(JsonElement element, boolean rotation) {
        if (element == null || element.isJsonNull()) {
            return null;
        }

        List<Keyframe> keyframes = new ArrayList<>();
        if (element.isJsonArray()) {
            // a single constant value applies for the whole clip
            float[] value = readVector(element, rotation);
            if (value != null) {
                keyframes.add(new Keyframe(0f, value));
            }
        } else if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                Double seconds = parseTime(entry.getKey());
                float[] value = readVector(keyframeValue(entry.getValue()), rotation);
                if (seconds != null && value != null) {
                    keyframes.add(new Keyframe((float) (seconds * TICKS_PER_SECOND), value));
                }
            }
        }

        if (keyframes.isEmpty()) {
            return null;
        }
        keyframes.sort(Comparator.comparingDouble(keyframe -> keyframe.time));
        return new Channel(keyframes);
    }

    private static Double parseTime(String key) {
        try {
            return Double.parseDouble(key);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * Blockbench can write a keyframe as {@code {"pre": ..., "post": ..., "lerp_mode": ...}}. Only {@code post} is
     * the value at that time; {@code pre} is the value just before it, which only matters for curved interpolation.
     */
    private static JsonElement keyframeValue(JsonElement element) {
        if (!element.isJsonObject()) {
            return element;
        }

        JsonObject json = element.getAsJsonObject();
        JsonElement post = json.get(POST);
        return post == null ? json.get(PRE) : post;
    }

    private static float[] readVector(JsonElement element, boolean rotation) {
        if (element == null || !element.isJsonArray()) {
            return null;
        }

        JsonArray array = element.getAsJsonArray();
        if (array.size() < 3) {
            return null;
        }

        try {
            float x = array.get(0).getAsFloat();
            float y = array.get(1).getAsFloat();
            float z = array.get(2).getAsFloat();
            if (rotation) {
                // the same conversion GeckoLib applies to both the model file and its animations: the model space
                // is mirrored in X, so X and Y rotations are negated before degrees become radians
                x = (float) Math.toRadians(-x);
                y = (float) Math.toRadians(-y);
                z = (float) Math.toRadians(z);
            }
            return new float[] { x, y, z };
        } catch (RuntimeException e) {
            // molang expressions and other non numeric values are not supported by gun clips
            return null;
        }
    }

    private static float readLength(JsonObject clip, Map<String, BoneClip> clips) {
        JsonElement authored = clip.get(LENGTH);
        if (authored != null && !authored.isJsonNull()) {
            try {
                return (float) (authored.getAsDouble() * TICKS_PER_SECOND);
            } catch (RuntimeException e) {
                // fall through to the last keyframe time
            }
        }

        float longest = 0f;
        for (BoneClip bone : clips.values()) {
            longest = Math.max(longest, bone.lastTime());
        }
        return longest;
    }

    private static final class BoneClip {

        private final Map<String, Channel> channels = new HashMap<>();

        private void put(String name, Channel channel) {
            if (channel != null) {
                this.channels.put(name, channel);
            }
        }

        private boolean isEmpty() {
            return this.channels.isEmpty();
        }

        private float[] sample(String name, double ticks) {
            Channel channel = this.channels.get(name);
            return channel == null ? null : channel.sample(ticks);
        }

        private float lastTime() {
            float longest = 0f;
            for (Channel channel : this.channels.values()) {
                longest = Math.max(longest, channel.lastTime());
            }
            return longest;
        }
    }

    private static final class Channel {

        private final List<Keyframe> keyframes;

        private Channel(List<Keyframe> keyframes) {
            this.keyframes = keyframes;
        }

        /**
         * Linear interpolation between consecutive keyframes. Before the first keyframe and after the last one the
         * closest value is held, so a channel that starts later does not snap the bone around at time zero.
         */
        private float[] sample(double ticks) {
            Keyframe first = this.keyframes.get(0);
            if (ticks <= first.time) {
                return first.value;
            }

            for (int i = 1; i < this.keyframes.size(); i++) {
                Keyframe next = this.keyframes.get(i);
                if (ticks < next.time) {
                    Keyframe previous = this.keyframes.get(i - 1);
                    double span = next.time - previous.time;
                    double progress = span <= 0 ? 1 : (ticks - previous.time) / span;
                    return lerp(previous.value, next.value, progress);
                }
            }
            return this.keyframes.get(this.keyframes.size() - 1).value;
        }

        private float lastTime() {
            return this.keyframes.get(this.keyframes.size() - 1).time;
        }

        private static float[] lerp(float[] start, float[] end, double progress) {
            float t = (float) Math.clamp(progress, 0, 1);
            return new float[] {
                    start[0] + (end[0] - start[0]) * t,
                    start[1] + (end[1] - start[1]) * t,
                    start[2] + (end[2] - start[2]) * t
            };
        }
    }

    private static final class Keyframe {

        private final float time;
        private final float[] value;

        private Keyframe(float time, float[] value) {
            this.time = time;
            this.value = value;
        }
    }

    private static final class RestPose {

        private final float rotationX;
        private final float rotationY;
        private final float rotationZ;
        private final float positionX;
        private final float positionY;
        private final float positionZ;
        private final float scaleX;
        private final float scaleY;
        private final float scaleZ;

        private RestPose(GeoBone bone) {
            this.rotationX = bone.getRotationX();
            this.rotationY = bone.getRotationY();
            this.rotationZ = bone.getRotationZ();
            this.positionX = bone.getPositionX();
            this.positionY = bone.getPositionY();
            this.positionZ = bone.getPositionZ();
            this.scaleX = bone.getScaleX();
            this.scaleY = bone.getScaleY();
            this.scaleZ = bone.getScaleZ();
        }
    }

    /**
     * One animation-driven particle effect: when the clip reaches {@code tick}, the listener fires for it.
     */
    static final class ParticleEvent {

        private final float tick;
        final String effect;
        final String locator;
        final String script;

        private ParticleEvent(float tick, String effect, String locator, String script) {
            this.tick = tick;
            this.effect = effect;
            this.locator = locator;
            this.script = script;
        }
    }

    /**
     * One animation-driven sound: when the clip reaches {@code tick}, the listener plays {@code sound}.
     */
    static final class SoundKeyFrame {

        private final float tick;
        final String sound;

        private SoundKeyFrame(float tick, String sound) {
            this.tick = tick;
            this.sound = sound;
        }
    }

    /**
     * One timeline instruction: when the clip reaches {@code tick}, the instruction is considered to have happened.
     */
    static final class InstructionEvent {

        private final float tick;
        final String instruction;

        private InstructionEvent(float tick, String instruction) {
            this.tick = tick;
            this.instruction = instruction;
        }
    }

    public static final class GunAnimationNames {

        public static final String FIRE = "fire";
        public static final String RELOAD = "reload";
        public static final String EQUIP = "equip";

        private GunAnimationNames() {}
    }

    /**
     * Timeline instruction that swaps the drawn magazine, as authored in Blockbench's timeline.
     */
    public static final String SWITCH_MAG = "switch_mag";
}
