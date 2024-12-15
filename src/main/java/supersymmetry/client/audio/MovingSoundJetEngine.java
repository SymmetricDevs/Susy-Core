package supersymmetry.client.audio;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import supersymmetry.api.sound.SusySounds;

@SideOnly(Side.CLIENT)
public class MovingSoundJetEngine extends MovingSound {

    private static final float MAX_VOLUME = 1.0F;
    private boolean isThrottled = false;
    private final EntityPlayer player;
    private float baseVolume = 0.0F;

    public MovingSoundJetEngine(EntityPlayer player) {
        super(SusySounds.JET_ENGINE_LOOP, SoundCategory.PLAYERS);
        this.player = player;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = MAX_VOLUME;
    }

    public void startPlaying() {
        this.baseVolume = MAX_VOLUME;
    }

    public void stopPlaying() {
        this.baseVolume = 0;
    }

    public boolean isThrottled() {
        return this.isThrottled;
    }

    public void setThrottled(boolean isThrottled) {
        this.isThrottled = isThrottled;
    }

    public boolean isPlaying() {
        return volume > 0.0F;
    }

    @Override
    public void update() {
        if (this.player.isDead) {
            this.donePlaying = true;
        } else {
            this.xPosF = (float) (player.posX + player.motionX);
            this.yPosF = (float) (player.posY + player.motionY);
            this.zPosF = (float) (player.posZ + player.motionZ);
        }
        float throttleMultiplier = isThrottled ? 0.2F : 1;
        if (volume < baseVolume * throttleMultiplier) {
            volume += 0.05F;
        } else {
            volume -= 0.02F;
        }
    }
}
