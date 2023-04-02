package supersymmetry.client.audio;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import supersymmetry.api.sound.SusySounds;
import supersymmetry.common.entities.EntityDropPod;

public class MovingSoundDropPod extends MovingSound {

    private final EntityDropPod dropPod;
    private float distance = 0.0F;
    private boolean play;

    public MovingSoundDropPod(EntityDropPod dropPod) {
        super(SusySounds.ROCKET_LOOP, SoundCategory.BLOCKS);
        this.dropPod = dropPod;
        this.repeat = true;
        this.repeatDelay = 0;
        this.play = true;
    }

    public void startPlaying() {
        this.play = true;
    }

    public void stopPlaying() {
        this.play = false;
    }

    @Override
    public void update() {
        if (this.dropPod.isDead || !this.play) {
            this.donePlaying = true;
        } else {
            this.xPosF = (float) this.dropPod.posX;
            this.yPosF = (float) this.dropPod.posY;
            this.zPosF = (float) this.dropPod.posZ;

            this.distance = MathHelper.clamp(this.distance + 0.0025F, 0.0F, 1.0F);
            this.volume = 1.0F;
        }
    }

}
