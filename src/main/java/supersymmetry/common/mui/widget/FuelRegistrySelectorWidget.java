package supersymmetry.common.mui.widget;

import static supersymmetry.api.capability.SuSyDataCodes.SYNC_FLUIDS;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.PhantomFluidWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;

public class FuelRegistrySelectorWidget extends AbstractWidgetGroup {

    public static final int limit = 9;
    public final List<FluidStack> stacks;
    public int slots;
    public Consumer<RocketFuelEntry> cb;

    public FuelRegistrySelectorWidget(
                                      int x, int y, int w, int h, List<FluidStack> stacks,
                                      @Nullable Consumer<RocketFuelEntry> cb) {
        super(new Position(x, y), new Size(w, h));
        this.stacks = stacks;
        slots = stacks.size();

        for (int i = 0; i < Math.min(stacks.size(), limit); i++) {
            PhantomFluidWidget initial = this.newWidget(i);
            this.addWidget(i, initial);
        }
        this.cb = cb;
    }

    public void onFluidChanged(@Nullable FluidStack stack, int index) {
        if (index > limit) {
            return;
        }
        if (stack == null) {
            for (int i = slots - 1; i != index; i--) {
                stacks.remove(i);
                this.widgets.remove(i);
            }

            stacks.set(index, null);
            slots = index + 1;
        } else {
            if (index == slots - 1) {
                if (slots < limit) {
                    stacks.set(index, stack);
                    stacks.add(null);
                    this.addWidget(slots, newWidget(slots));
                    slots++;
                }
            } else {
                stacks.set(index, stack);
            }
        }

        searchRegistry();
    }

    public void searchRegistry() {
        if (cb != null) {
            Optional<RocketFuelEntry> r = search();
            if (!r.isEmpty()) {
                cb.accept(r.get());
            }
        }
    }

    public Optional<RocketFuelEntry> search() {
        List<Fluid> userFluids = this.stacks.stream()
                .filter(x -> x != null)
                .map(FluidStack::getFluid)
                .collect(Collectors.toList());

        for (RocketFuelEntry entry : RocketFuelEntry.getFuelRegistry().values()) {
            boolean matches = entry.getComposition().stream()
                    .map(Tuple::getFirst)
                    .allMatch(userFluids::contains);
            if (matches) {
                return Optional.of(entry);
            }
        }
        return Optional.empty();
    }

    private PhantomFluidWidget newWidget(final int index) {
        int x = index * 20 + 2;
        int y = (int) Math.floor((double) x / (double) this.getSize().width) * 20;
        x = x % this.getSize().width;

        Supplier<FluidStack> supplier = () -> stacks.get(index);
        // run the supplier/updater on the client so the player's selection is captured there
        return new PhantomFluidWidget(
                x,
                y,
                18,
                18,
                supplier,
                (stack) -> {
                    // The phantom slot is edited on the client, so forward the change to the
                    // server with a client action; PhantomFluidWidget also invokes this setter
                    // server-side through its own sync, but there we let the action below be the
                    // single authoritative update instead of applying it twice.
                    this.onFluidChanged(stack, index);
                    this.writeUpdateInfo(SYNC_FLUIDS, (buffer) -> {
                        buffer.writeInt(index);
                        buffer.writeBoolean(stack == null);
                        if (stack != null) {
                            buffer.writeCompoundTag(stack.writeToNBT(new NBTTagCompound()));
                        }
                    });
                });
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == SYNC_FLUIDS) {
            int index = buffer.readInt();
            FluidStack stack;
            try {
                stack = buffer.readBoolean() ? null : FluidStack.loadFluidStackFromNBT(buffer.readCompoundTag());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            onFluidChanged(stack, index);
        }
    }
}
