package supersymmetry.integration.findme;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import org.jetbrains.annotations.Nullable;

/**
 * The fluid handlers a tile entity offers on its six faces, as one read-only handler. Used for FindMe's fluid
 * search, which only asks without a side and so misses blocks that answer for a real side only.
 */
public final class FaceFluidHandlers {

    private FaceFluidHandlers() {}

    /** @return the handler across the faces, or null if no face has one */
    @Nullable
    public static IFluidHandler of(TileEntity te) {
        List<IFluidHandler> found = new ArrayList<>(2);
        for (EnumFacing face : EnumFacing.VALUES) {
            IFluidHandler handler = te.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, face);
            if (handler != null && !found.contains(handler)) {
                found.add(handler);
            }
        }
        if (found.isEmpty()) return null;
        return found.size() == 1 ? found.get(0) : new Merged(found);
    }

    /** Tank properties of several handlers in one list; fill and drain do nothing. */
    private static final class Merged implements IFluidHandler {

        private final List<IFluidHandler> handlers;

        private Merged(List<IFluidHandler> handlers) {
            this.handlers = handlers;
        }

        @Override
        public IFluidTankProperties[] getTankProperties() {
            List<IFluidTankProperties> all = new ArrayList<>();
            for (IFluidHandler handler : this.handlers) {
                IFluidTankProperties[] properties = handler.getTankProperties();
                if (properties == null) continue;
                for (IFluidTankProperties property : properties) {
                    all.add(property);
                }
            }
            return all.toArray(new IFluidTankProperties[0]);
        }

        @Override
        public int fill(FluidStack resource, boolean doFill) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, boolean doDrain) {
            return null;
        }

        @Override
        public FluidStack drain(int maxDrain, boolean doDrain) {
            return null;
        }
    }
}
