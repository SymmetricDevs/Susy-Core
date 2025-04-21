package supersymmetry.api.metatileentity.logistics;

import gregtech.client.utils.TooltipHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Mostly a marker interface
public interface IDelegator {

    /**
     * @return the facing that the input facing in delegating
     */
    @Nullable
    EnumFacing getDelegatingFacing(EnumFacing facing);

    default void addRecursiveWarning(ItemStack stack, @Nullable World world, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(TooltipHelper.BLINKING_RED + I18n.format("gregtech.machine.delegator.tooltip.non_recursion"));
    }
}
