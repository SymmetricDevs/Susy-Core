package supersymmetry.api.capability;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Logic from EnderIO:
 * <a href="https://github.com/EnderIOu/EnderCore/blob/72a28bf5bc7dc8c7df067f43a7222b25ba594e32/src/main/java/com/enderio/core/common/transform/EnderCoreMethods.java#L200">...</a>
 */
public interface IElytraFlyingProvider {

    boolean isElytraFlying(@NotNull EntityLivingBase entity, @NotNull ItemStack itemstack, boolean shouldStop);
}
