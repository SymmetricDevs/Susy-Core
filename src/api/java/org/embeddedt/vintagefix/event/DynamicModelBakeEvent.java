package org.embeddedt.vintagefix.event;

import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.Event;

/// Adapted and minimized from
/// [VintageFix](https://github.com/embeddedt/VintageFix/blob/main/src/main/java/org/embeddedt/vintagefix/event/DynamicModelBakeEvent.java)
public class DynamicModelBakeEvent extends Event {
    public ResourceLocation location;
    public IBakedModel bakedModel;
}
