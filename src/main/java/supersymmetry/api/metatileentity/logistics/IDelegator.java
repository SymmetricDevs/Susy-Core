package supersymmetry.api.metatileentity.logistics;

import net.minecraft.util.EnumFacing;
import org.jetbrains.annotations.Nullable;

// Mostly a marker interface
public interface IDelegator {

    /**
     * @return the facing that the input facing in delegating
     */
    @Nullable
    EnumFacing getDelegatingFacing(EnumFacing facing);
}
