package supersymmetry.api.sound;

import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import supersymmetry.Supersymmetry;

public class SusySounds {

    public static SoundEvent ROCKET_LOOP;
    public static SoundEvent DRONE_TAKEOFF;

    public static void registerSounds() {
        ROCKET_LOOP = registerSound("entity.rocket_loop");
        DRONE_TAKEOFF = registerSound("entity.drone_takeoff");
    }

    private static SoundEvent registerSound(String soundNameIn) {
        ResourceLocation location = new ResourceLocation(Supersymmetry.MODID, soundNameIn);
        SoundEvent event = new SoundEvent(location);
        event.setRegistryName(location);
        ForgeRegistries.SOUND_EVENTS.register(event);
        return event;
    }

}
