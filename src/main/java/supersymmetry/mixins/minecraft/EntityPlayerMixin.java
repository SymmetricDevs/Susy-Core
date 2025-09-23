package supersymmetry.mixins.minecraft;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;

import supersymmetry.api.util.ElytraFlyingUtils;

@Mixin(EntityPlayer.class)
public abstract class EntityPlayerMixin extends EntityLivingBase {

    public EntityPlayerMixin(World worldIn) {
        super(worldIn);
    }

    @Override
    public boolean isElytraFlying() {
        boolean isElytraFlying = super.isElytraFlying() || ElytraFlyingUtils.isElytraFlying(this);
        this.setFlag(7, isElytraFlying);
        return isElytraFlying;
    }
}
