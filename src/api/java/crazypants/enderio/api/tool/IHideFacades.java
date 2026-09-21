package crazypants.enderio.api.tool;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.jspecify.annotations.NonNull;

public interface IHideFacades {

    boolean shouldHideFacades(@NonNull ItemStack stack, @NonNull EntityPlayer player);
}
