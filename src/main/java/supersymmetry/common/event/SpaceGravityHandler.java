package supersymmetry.common.event;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.world.WorldProvider;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import supersymmetry.api.space.dimension.WorldProviderSpace;

public class SpaceGravityHandler {

    private static final double VANILLA_GRAVITY = 0.08;

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();

        // Only act in space dimensions
        WorldProvider provider = entity.world.provider;
        if (!(provider instanceof WorldProviderSpace)) return;

        WorldProviderSpace spaceProvider = (WorldProviderSpace) provider;
        float customGravity = spaceProvider.getGravity();

        // Skip entities that aren't subject to gravity
        if (entity.isRiding() || entity.onGround || entity.isInWater() || entity.isInLava()) return;

        // Vanilla has already subtracted 0.08 from motionY this tick.
        // Undo it and apply our own value instead.
        entity.motionY += VANILLA_GRAVITY;       // restore
        entity.motionY -= customGravity;         // apply custom (0 = microgravity)
    }
}
