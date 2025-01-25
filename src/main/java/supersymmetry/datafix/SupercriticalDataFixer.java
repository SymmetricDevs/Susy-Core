package supersymmetry.datafix;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.datafix.IFixableData;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class SupercriticalDataFixer implements IFixableData {

    private static final Set<String> PREFIXES = new HashSet<>();

    @Override
    public int getFixVersion() {
        return 3;
    }

    @Override
    public @NotNull NBTTagCompound fixTagCompound(@NotNull NBTTagCompound compound) {
        final short meta = compound.getShort("Damage");
        final String id = compound.getString("id");
        if (id.startsWith("gregtech:meta_")) {
            final String prefix = id.replace("gregtech:meta_", "");
            if (PREFIXES.contains(prefix) && meta == 7047) {
                compound.setString("id", "supercritical:meta_" + prefix);
                compound.setShort("Damage", (short) 506);
            }
        }
        return compound;
    }

    static {
        PREFIXES.add("dust_tiny");
        PREFIXES.add("dust");
        PREFIXES.add("dust_small");
        PREFIXES.add("gem");
        PREFIXES.add("block_compressed_440");
    }
}
