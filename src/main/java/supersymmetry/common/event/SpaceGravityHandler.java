package supersymmetry.common.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import supersymmetry.api.space.dimension.WorldProviderSpace;

public class SpaceGravityHandler {

    private static final double VANILLA_GRAVITY = 0.08;

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();

        WorldProvider provider = entity.world.provider;
        if (!(provider instanceof WorldProviderSpace)) return;

        float customGravity = ((WorldProviderSpace) provider).getGravity();

        if (entity.isRiding() || entity.isInWater() || entity.isInLava()) return;

        // Vanilla will subtract VANILLA_GRAVITY this tick unconditionally.
        // Pre-correct so the net result equals our custom gravity.
        entity.motionY += VANILLA_GRAVITY;    // undo vanilla (which hasn't run yet at HIGHEST)
        entity.motionY -= customGravity;      // apply ours
    }
}
