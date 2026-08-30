package supersymmetry.api.pipelike;

import org.jspecify.annotations.NonNull;

import dev.tianmi.sussypatches.api.annotation.MixinExtension;
import gregtech.api.pipenet.tile.IPipeTile;

@MixinExtension(IPipeTile.class)
public interface ConnectablePipe {

    /// @param other pipe instance of a DIFFERENT type
    /// @return whether this can connect with that
    default boolean canConnectWith(@NonNull IPipeTile<?, ?> other) {
        return false;
    }
}
