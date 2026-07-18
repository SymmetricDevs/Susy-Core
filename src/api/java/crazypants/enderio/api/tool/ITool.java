package crazypants.enderio.api.tool;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import org.jspecify.annotations.NonNull;

public interface ITool extends IHideFacades {

    boolean canUse(@NonNull EnumHand stack, @NonNull EntityPlayer player, @NonNull BlockPos pos);

    void used(@NonNull EnumHand stack, @NonNull EntityPlayer player, @NonNull BlockPos pos);
}
