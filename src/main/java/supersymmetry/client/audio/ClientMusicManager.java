package supersymmetry.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.client.event.sound.PlaySoundEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import supersymmetry.Supersymmetry;
import supersymmetry.api.sound.SusySounds;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = Side.CLIENT)
public final class ClientMusicManager {

    private static final Logger LOGGER = LogManager.getLogger(Supersymmetry.MODID);

    private enum Context {
        NONE,
        MAIN_MENU
    }

    private static Context context = Context.NONE;
    private static ISound currentTrack;
    private static boolean loggedMenuStart;

    private ClientMusicManager() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getMinecraft();
        SoundHandler sounds = minecraft.getSoundHandler();
        Context desired = getContext(minecraft);

        if (desired != context) {
            stopCurrentTrack(sounds);
            context = desired;
            loggedMenuStart = false;
            if (context == Context.MAIN_MENU) {
                // A vanilla track may still be playing after returning from a world.
                sounds.stop(null, SoundCategory.MUSIC);
            }
        }

        if (context == Context.NONE || minecraft.gameSettings.getSoundLevel(SoundCategory.MUSIC) <= 0.0F) {
            stopCurrentTrack(sounds);
            return;
        }

        if (currentTrack == null || !sounds.isSoundPlaying(currentTrack)) {
            currentTrack = new PositionedSoundRecord(SusySounds.MAIN_MENU_MUSIC.getSoundName(),
                    SoundCategory.MUSIC, 1.0F, 1.0F, true, 0, ISound.AttenuationType.NONE, 0.0F, 0.0F, 0.0F);
            sounds.playSound(currentTrack);
            if (!loggedMenuStart) {
                LOGGER.info("Started main menu music: {}", SusySounds.MAIN_MENU_MUSIC.getSoundName());
                loggedMenuStart = true;
            }
        }
    }

    @SubscribeEvent
    public static void onPlaySound(PlaySoundEvent event) {
        Minecraft minecraft = Minecraft.getMinecraft();
        if (getContext(minecraft) == Context.MAIN_MENU && event.getSound() != null &&
                SoundEvents.MUSIC_MENU.getSoundName().equals(event.getSound().getSoundLocation())) {
            event.setResultSound(null);
        }
    }

    private static Context getContext(Minecraft minecraft) {
        return minecraft.world == null && minecraft.getAmbientMusicType() == MusicTicker.MusicType.MENU ? Context.MAIN_MENU :
                Context.NONE;
    }

    private static void stopCurrentTrack(SoundHandler sounds) {
        if (currentTrack != null) {
            sounds.stopSound(currentTrack);
            currentTrack = null;
        }
    }
}
