package supersymmetry.api.sound;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import supersymmetry.Supersymmetry;

public class SusySounds {

    public static SoundEvent ROCKET_LOOP;
    public static SoundEvent DRONE_TAKEOFF;
    public static SoundEvent ROCKET_LAUNCH;
    public static SoundEvent JET_ENGINE_LOOP;
    public static SoundEvent LOCKED_CRATE;
    public static SoundEvent COMPLEX_ALARM;
    public static SoundEvent METAL_DOOR_CLOSE;
    public static SoundEvent METAL_DRAWER_OPEN;
    public static SoundEvent OXYGEN_SENSOR_BEEP;
    public static SoundEvent MAIN_MENU_MUSIC;
    public static SoundEvent QUEST_MUSIC_TRIGGER;
    public static SoundEvent QUEST_MUSIC;
    public static SoundEvent QUEST_MUSIC_MV_TRIGGER;
    public static SoundEvent QUEST_MUSIC_MV;
    public static SoundEvent QUEST_MUSIC_HV_TRIGGER;
    public static SoundEvent QUEST_MUSIC_HV;
    public static SoundEvent QUEST_MUSIC_EV_TRIGGER;
    public static SoundEvent QUEST_MUSIC_EV;
    public static SoundEvent QUEST_MUSIC_IV_TRIGGER;
    public static SoundEvent QUEST_MUSIC_IV;

    public static void registerSounds() {
        ROCKET_LOOP = registerSound("entity.rocket_loop");
        DRONE_TAKEOFF = registerSound("entity.drone_takeoff");
        ROCKET_LAUNCH = registerSound("entity.rocket_launch");
        JET_ENGINE_LOOP = registerSound("item.jet_wingpack_engine_active");
        LOCKED_CRATE = registerSound("block.locked_crate");
        COMPLEX_ALARM = registerSound("block.complex_alarm");
        METAL_DOOR_CLOSE = registerSound("block.metal_door_close");
        METAL_DRAWER_OPEN = registerSound("block.metal_drawer_open");
        OXYGEN_SENSOR_BEEP = registerSound("item.oxygen_sensor_beep");
        MAIN_MENU_MUSIC = registerSound("music.main_menu");
        QUEST_MUSIC_TRIGGER = registerSound("music.quest_trigger");
        QUEST_MUSIC = registerSound("music.quest");
        QUEST_MUSIC_MV_TRIGGER = registerSound("music.quest_trigger_mv");
        QUEST_MUSIC_MV = registerSound("music.quest_mv");
        QUEST_MUSIC_HV_TRIGGER = registerSound("music.quest_trigger_hv");
        QUEST_MUSIC_HV = registerSound("music.quest_hv");
        QUEST_MUSIC_EV_TRIGGER = registerSound("music.quest_trigger_ev");
        QUEST_MUSIC_EV = registerSound("music.quest_ev");
        QUEST_MUSIC_IV_TRIGGER = registerSound("music.quest_trigger_iv");
        QUEST_MUSIC_IV = registerSound("music.quest_iv");
    }

    private static SoundEvent registerSound(String soundNameIn) {
        ResourceLocation location = new ResourceLocation(Supersymmetry.MODID, soundNameIn);
        SoundEvent event = new SoundEvent(location);
        event.setRegistryName(location);
        ForgeRegistries.SOUND_EVENTS.register(event);
        return event;
    }
}
