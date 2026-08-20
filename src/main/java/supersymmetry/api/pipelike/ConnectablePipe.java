package supersymmetry.api.pipelike;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.api.pipenet.tile.IPipeTile;
import org.jspecify.annotations.NonNull;

@MixinExtension(IPipeTile.class)
public interface ConnectablePipe {

    /// @param other pipe instance of a DIFFERENT type
    /// @return whether this can connect with that
    default boolean canConnectWith(@NonNull IPipeTile<?, ?> other) {
        return false;
    }
}
