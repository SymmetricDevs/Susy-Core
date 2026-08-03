package crazypants.enderio.api.tool;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.jspecify.annotations.NonNull;

public interface IConduitControl {

    /**
     * Controls whether the overlay is shown and the player can change the display mode.
     *
     * @param stack  The itemstack
     * @param player The player holding the itemstack
     * @return True if the overlay should be rendered and the player should be able to change modes. False otherwise.
     */
    boolean showOverlay(@NonNull ItemStack stack, @NonNull EntityPlayer player);
}
