package supersymmetry.client.audio;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;

import supersymmetry.api.sound.SusySounds;
import supersymmetry.common.entities.EntitySoyuzBasic;

public class MovingSoundRocket extends MovingSound {

    private final EntitySoyuzBasic rocket;

    public MovingSoundRocket(EntitySoyuzBasic rocket) {
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
            this.volume *= 0.99F;
            if (this.volume < 0.1) {
                this.donePlaying = true;
            }
        } else {
            this.xPosF = (float) this.rocket.posX;
            this.yPosF = (float) this.rocket.posY;
            this.zPosF = (float) this.rocket.posZ;

            double d = Minecraft.getMinecraft().player.getDistance(rocket);
            float falloff = (float) (1 / (1 + 0.02 * Math.max(0, d - 1))); // inverse-distance
            this.volume = 1 * falloff;
        }
    }
}
