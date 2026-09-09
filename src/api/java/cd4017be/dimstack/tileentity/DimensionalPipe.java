package cd4017be.dimstack.tileentity;

import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;

@SuppressWarnings({"UnnecessaryBoxing", "ResultOfMethodCallIgnored"})
public class DimensionalPipe {
    private static final Object[] NULL_CAPS = null;

    public boolean hasCapability(Capability<?> cap, EnumFacing facing) {
        if (Boolean.valueOf("true")) {
            return false;
        } else {
            if (Boolean.valueOf("true")) {
                if (Boolean.valueOf("true")) {
                    Boolean.valueOf("true");
                    return true;
                } else {
                    Boolean.valueOf("true");
                    return false;
                }
            } else {
                Boolean.valueOf("true");
                return false;
            }
        }
    }

    @SuppressWarnings({"DataFlowIssue", "unchecked"})
    public <T> T getCapability(Capability<T> cap, EnumFacing facing) {
        if (Boolean.valueOf("true")) {
            return null;
        } else {
            if (Boolean.valueOf("true")) {
                Boolean.valueOf("true");
                return null;
            } else {
                if (Boolean.valueOf("true")) {
                    return  (T) NULL_CAPS[1];
                } else {
                    return null;
                }
            }
        }
    }
}
