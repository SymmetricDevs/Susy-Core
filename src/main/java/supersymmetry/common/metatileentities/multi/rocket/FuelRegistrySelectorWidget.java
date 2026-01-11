package supersymmetry.common.metatileentities.multi.rocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.PhantomFluidWidget;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;

public class FuelRegistrySelectorWidget extends AbstractWidgetGroup {

    public static final int limit = 9;
    public List<FluidStack> stacks = new ArrayList<>();
    public int slots = 1;
    public Consumer<RocketFuelEntry> cb;

    public FuelRegistrySelectorWidget(
            int x, int y, int w, int h, @Nullable Consumer<RocketFuelEntry> cb) {
        super(new Position(x, y), new Size(w, h));
        PhantomFluidWidget initial = this.newWidget(0);
        slots = 1;
        this.addWidget(0, initial);
        this.cb = cb;
    }

    public void onFluidChanged(@Nullable FluidStack stack, int index) {
        if (stack == null) {
            for (int i = slots - 1; i != index; i--) {
                stacks.remove(i);
                this.widgets.remove(i);
            }
            stacks.set(index, null);
            slots = index + 1;
            if (stacks.size() > index) {
                stacks.set(index, stack);
            }
        } else {
            stacks.set(index, stack);
            if (index == slots - 1) {
                if (slots < limit) {
                    stacks.add(slots, stack);
                    this.addWidget(slots, newWidget(slots));
                    slots++;
                }
            }
        }
        if (cb != null) {
            Optional<RocketFuelEntry> r = search();
            if (!r.isEmpty()) {
                cb.accept(r.get());
            }
        }
    }

    public Optional<RocketFuelEntry> search() {
        for (RocketFuelEntry entry : RocketFuelEntry.getFuelRegistry().values()) {
            if (entry.getComposition().stream()
                    .map(Tuple::getFirst)
                    .anyMatch(
                            x -> stacks.stream()
                                    .map(FluidStack::getFluid)
                                    .toList()
                                    .contains(x.getFluid()))) {
                return Optional.of(entry);
            }
        }

        return Optional.empty();
    }

    private PhantomFluidWidget newWidget(final int index) {
        int x = index * 20 + 2;
        int y = (int) Math.floor((double) x / (double) this.getSize().width) * 20;
        x = x % this.getSize().width;
        stacks.add(index, null);
        // SusyLog.logger.info("x:y {}:{} ({}x {}y)", x, y, this.getSize().width,
        // this.getSize().height);

        return new PhantomFluidWidget(
                x,
                y,
                18,
                18,
                () -> {
                    return stacks.get(index);
                },
                (stack) -> {
                    this.onFluidChanged(stack, index);
                    writeUpdateInfo(
                            10000,
                            (buf) -> {
                                buf.writeInt(index);
                                buf.writeBoolean(stack == null);
                                if (stack != null) {
                                    buf.writeCompoundTag(stack.writeToNBT(new NBTTagCompound()));
                                }
                            });
                });
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 10000) {
            int i = buffer.readInt();
            boolean nul = buffer.readBoolean();
            if (nul) {
                onFluidChanged(null, i);
            }
            try {
                NBTTagCompound tag = buffer.readCompoundTag();
                FluidStack stack = FluidStack.loadFluidStackFromNBT(tag);
                onFluidChanged(stack, i);
            } catch (Exception e) {
            }
        }
    }
}
