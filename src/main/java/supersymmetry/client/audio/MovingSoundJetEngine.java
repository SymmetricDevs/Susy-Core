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
    private final EntityPlayer player;

    public MovingSoundJetEngine(EntityPlayer player) {
        super(SusySounds.JET_ENGINE_LOOP, SoundCategory.PLAYERS);
        this.player = player;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = MAX_VOLUME;
    }

    public void startPlaying() {
        this.volume = MAX_VOLUME;
    }

    public void stopPlaying() {
        this.volume = 0.0F;
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
    }
}
