package supersymmetry.client.audio;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;

import supersymmetry.api.sound.SusySounds;
import supersymmetry.common.entities.EntityRocket;

public class MovingSoundRocket extends MovingSound {

    private final EntityRocket rocket;
    private float distance = 0.0F;

    public MovingSoundRocket(EntityRocket rocket) {
        super(SusySounds.ROCKET_LAUNCH, SoundCategory.NEUTRAL);
        this.attenuationType = AttenuationType.NONE;
        this.rocket = rocket;
        this.repeat = false;
        this.repeatDelay = 0;
        this.volume = 1.F;
    }

    public void startPlaying() {
        this.volume = 1.F;
    }

    public void stopPlaying() {
        this.volume = 0.0F;
    }

    @Override
    public void update() {
        if (this.rocket.isDead) {
            this.donePlaying = true;
        } else {
            this.xPosF = (float) this.rocket.posX;
            this.yPosF = (float) this.rocket.posY;
            this.zPosF = (float) this.rocket.posZ;

            this.distance = MathHelper.clamp(this.distance + 0.0025F, 0.0F, 1.0F);
        }
    }
}
