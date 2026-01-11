package supersymmetry.common.metatileentities.multi.rocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

import org.jetbrains.annotations.Nullable;

import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.gui.widgets.PhantomFluidWidget;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;

public class FuelRegistrySelectorWidget extends AbstractWidgetGroup {

    public static final int limit = 9;
    public List<FluidStack> stacks;
    public int slots = 1;
    public Consumer<RocketFuelEntry> cb;

    public FuelRegistrySelectorWidget(
            int x, int y, int w, int h, @Nullable Consumer<RocketFuelEntry> cb) {
        super(new Position(x, y), new Size(w, h));
        this.stacks = new ArrayList<>(limit);
        PhantomFluidWidget initial = this.newWidget(0);
        slots = 1;
        this.addWidget(0, initial);
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
                    this.addWidget(slots, newWidget(slots));
                    slots++;
                }
            } else {
                stacks.set(index, stack);
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
        List<FluidStack> stacksNotNull = this.stacks.stream().filter(x -> x != null).collect(Collectors.toList());

        for (RocketFuelEntry entry : RocketFuelEntry.getFuelRegistry().values()) {
                List<Fluid> fluids = stacksNotNull.stream()
                        .map(
                                x -> {
                                    return x.getFluid();
                                })
                        .collect(Collectors.toList());

                if (entry.getSides().stream()
                        .map(
                                x -> {
                                    Material mat = x.getFirst();
                                    Fluid l = mat.getFluid(FluidStorageKeys.LIQUID);
                                    if (l != null) {
                                        return l;
                                    }
                                    FluidProperty fluidprop = mat.getProperty(PropertyKey.FLUID);
                                    if (fluidprop != null) {
                                        @Nullable
                                        Fluid fluid = fluidprop.get(fluidprop.getPrimaryKey());
                                        if (fluid != null) {
                                            return fluid;
                                        }
                                    }
                                    return mat.getFluid();
                                })
                        .allMatch(
                                x -> {
                                    return fluids.contains(x);
                                })) {
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

        return new PhantomFluidWidget(
                x,
                y,
                18,
                18,
                () -> {
                    final int i = Integer.valueOf(index).intValue();
                    return stacks.get(i);
                },
                (stack) -> {
                    final int i = Integer.valueOf(index).intValue();
                    // SusyLog.logger.info(
                    // "{{}} -> slots[{}]", stack == null ? "null" : stack.getUnlocalizedName(), i);
                    this.onFluidChanged(stack, i);
                    writeUpdateInfo(
                            10000,
                            (buf) -> {
                                buf.writeInt(i);
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
