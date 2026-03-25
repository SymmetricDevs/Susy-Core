package supersymmetry.client.audio;

import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;

import supersymmetry.api.sound.SusySounds;
import supersymmetry.common.entities.EntityDrone;

public class MovingSoundDrone extends MovingSound {

    private final EntityDrone drone;
    private float distance = 0.0F;

    public MovingSoundDrone(EntityDrone drone) {
        super(SusySounds.DRONE_TAKEOFF, SoundCategory.NEUTRAL);
        this.drone = drone;
        this.repeat = false;
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
        if (this.drone.isDead) {
            this.donePlaying = true;
        } else {
            this.xPosF = (float) this.drone.posX;
            this.yPosF = (float) this.drone.posY;
            this.zPosF = (float) this.drone.posZ;

            this.distance = MathHelper.clamp(this.distance + 0.0025F, 0.0F, 1.0F);
        }
    }
}
