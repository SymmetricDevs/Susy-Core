package supersymmetry.api.metatileentity.multiblock;

import org.jetbrains.annotations.Nullable;

/**
 * Something which receives a cryogenic property from {@link ICryogenicProvider}.
 */
public interface ICryogenicReceiver {

    /**
     * @return the cryogenic provider for this receiver
     */
    @Nullable
    ICryogenicProvider getCryogenicProvider();

    /**
     * @param cryogenicProvider the cryogenic provider to associate with this
     */
    void setCryogenicProvider(@Nullable ICryogenicProvider cryogenicProvider);
}
