package supersymmetry.client.audio;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;

import supersymmetry.api.sound.SusySounds;
import supersymmetry.common.entities.EntityLander;

public class MovingSoundLander extends MovingSound {

    private final EntityLander lander;
    private float distance = 0.0F;

    public MovingSoundLander(EntityLander lander) {
        super(SusySounds.ROCKET_LOOP, SoundCategory.NEUTRAL);
        this.lander = lander;
        this.repeat = true;
        this.repeatDelay = 0;
        this.volume = 0.5F;
    }

    public void startPlaying() {
        this.volume = 0.5F;
    }

    public void stopPlaying() {
        this.volume = 0.0F;
    }

    @Override
    public void update() {
        if (this.lander.isDead) {
            this.donePlaying = true;
        } else {
            this.xPosF = (float) this.lander.posX;
            this.yPosF = (float) this.lander.posY;
            this.zPosF = (float) this.lander.posZ;

            this.distance = MathHelper.clamp(this.distance + 0.0025F, 0.0F, 1.0F);
        }
    }
}
