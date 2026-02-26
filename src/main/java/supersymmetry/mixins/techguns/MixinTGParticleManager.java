package supersymmetry.mixins.techguns;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import techguns.client.particle.ITGParticle;
import techguns.client.particle.TGParticleManager;
import techguns.client.particle.list.ParticleList;

@Mixin(value = TGParticleManager.class, remap = false)
public class MixinTGParticleManager {
    @Shadow
    protected ParticleList<ITGParticle> list;
    @Shadow
    protected ParticleList<ITGParticle> list_nosort;

    /**
     * @author bruberu
     * @reason Checking particle count
     */
    @Overwrite
    public void renderParticles(Entity entityIn, float partialTicks) {
        float f1 = MathHelper.cos(entityIn.rotationYaw * ((float) Math.PI / 180F));
        float f2 = MathHelper.sin(entityIn.rotationYaw * ((float) Math.PI / 180F));
        float f3 = -f2 * MathHelper.sin(entityIn.rotationPitch * ((float) Math.PI / 180F));
        float f4 = f1 * MathHelper.sin(entityIn.rotationPitch * ((float) Math.PI / 180F));
        float f5 = MathHelper.cos(entityIn.rotationPitch * ((float) Math.PI / 180F));
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        TGParticleManager.interpPosX = entityIn.lastTickPosX + (entityIn.posX - entityIn.lastTickPosX) * (double) partialTicks;
        TGParticleManager.interpPosY = entityIn.lastTickPosY + (entityIn.posY - entityIn.lastTickPosY) * (double) partialTicks;
        TGParticleManager.interpPosZ = entityIn.lastTickPosZ + (entityIn.posZ - entityIn.lastTickPosZ) * (double) partialTicks;
        GlStateManager.disableCull();
        final int[] i = {0};
        this.list.forEach((p) -> {
            i[0]++;
            if (i[0] < 100) {
                p.doRender(bufferbuilder, entityIn, partialTicks, f1, f5, f2, f3, f4);
            }
        });
        i[0] = 0;
        this.list_nosort.forEach((p) -> {
            i[0]++;
            if (i[0] < 50) {
                p.doRender(bufferbuilder, entityIn, partialTicks, f1, f5, f2, f3, f4);
            }
        }
        );
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }
}
