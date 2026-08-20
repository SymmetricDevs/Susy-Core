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
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.PhantomFluidWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import supersymmetry.api.rocketry.fuels.LiquidRocketFuelEntry;

public class FuelRegistrySelectorWidget extends AbstractWidgetGroup {

    public static final int limit = 9;
    public final List<FluidStack> stacks;
    public int slots;
    public Consumer<LiquidRocketFuelEntry> cb;

    public FuelRegistrySelectorWidget(int x, int y, int w, int h, List<FluidStack> stacks,
                                      @Nullable Consumer<LiquidRocketFuelEntry> cb) {
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

    // the consumer is also fed a null when nothing matches, so that the listener can
    // drop a fuel the player just emptied the slots of
    public void searchRegistry() {
        if (cb != null) {
            cb.accept(search().orElse(null));
        }
    }

    public Optional<LiquidRocketFuelEntry> search() {
        List<Fluid> userFluids = this.stacks.stream().filter(x -> x != null).map(FluidStack::getFluid)
                .collect(Collectors.toList());

        return LiquidRocketFuelEntry.search(userFluids);
    }

    private PhantomFluidWidget newWidget(final int index) {
        int x = index * 20 + 2;
        int y = (int) Math.floor((double) x / (double) this.getSize().width) * 20;
        x = x % this.getSize().width;

        Supplier<FluidStack> supplier = () -> stacks.get(index);
        // run the supplier/updater on the client so the player's selection is captured
        // there
        return new PhantomFluidWidget(x, y, 18, 18, supplier, (stack) -> {
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
