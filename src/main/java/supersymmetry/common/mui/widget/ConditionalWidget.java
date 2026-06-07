package supersymmetry.common.mui.widget;

import static supersymmetry.api.capability.SuSyDataCodes.LATE_INIT_WIDGET;
import static supersymmetry.api.capability.SuSyDataCodes.STATE_UPDATE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import gregtech.api.fluids.store.FluidStorageKeys;
import gregtech.api.gui.widgets.PhantomFluidWidget;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.properties.FluidProperty;
import gregtech.api.unification.material.properties.PropertyKey;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;
import supersymmetry.api.SusyLog;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.api.rocketry.fuels.RocketFuelEntry;
import supersymmetry.api.util.DataStorageLoader;

// very laggy i think..
public class ConditionalWidget extends AbstractWidgetGroup {

    private HashMap<Widget, BooleanSupplier> tests = new HashMap<>();
    private List<Tuple<BooleanSupplier, Supplier<Widget>>> lateInit = new ArrayList<>();
    private List<Integer> lateInitDone = new ArrayList<>();
    private BooleanSupplier defaultPredicate;

    public ConditionalWidget(int x, int y, int w, int h, BooleanSupplier state_predicate) {
        super(new Position(x, y), new Size(w, h));
        defaultPredicate = state_predicate;
    }

    public void addWidgetWithTest(Widget widget, BooleanSupplier test) {
        tests.put(widget, test);
        addWidget(widget);
    }

    // everything in ceu is private :C :C
    public void addWidget(Widget widget) {
        super.addWidget(widget);
    }

    // jdtls provides the option to suppress warnings but not to automatically insert the fix even
    // when it suggests one :C :C

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addWidgetConditionalInit(BooleanSupplier test, Supplier<Widget> init) {
        lateInit.add(new Tuple(test, init));
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        if (!this.isActive()) {
            return;
        }
        if (lateInit.size() > lateInitDone.size()) {
            for (Tuple<BooleanSupplier, Supplier<Widget>> w : lateInit) {
                if (lateInitDone.contains(lateInit.indexOf(w))) {
                    continue;
                }
                if (w.getFirst().getAsBoolean()) {
                    addWidgetWithTest(w.getSecond().get(), w.getFirst());
                    int id = lateInit.indexOf(w);
                    if (id == -1) {
                        throw new RuntimeException();
                    }
                    // SusyLog.logger.info("lateInit on i:{}", id);
                    // lateInit.remove(w);
                    lateInitDone.add(id);
                    writeUpdateInfo(LATE_INIT_WIDGET, buf -> buf.writeInt(id));
                    break; // i think it will error if you continue going after deleting an item from a list
                }
            }
        }
        for (Widget w : widgets) {
            updateState(w, tests.getOrDefault(w, defaultPredicate).getAsBoolean());
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == STATE_UPDATE) {
            boolean state = buffer.readBoolean();
            int index = buffer.readInt();
            if (index >= widgets.size()) {
                SusyLog.logger.error(
                        "widgets.size()->[{}]: tried to {} widget:{}",
                        widgets.size(),
                        state ? "turn on" : "turn off",
                        id);
                // SusyLog.logger.info("client; wsize:{}, {}", widgets.size(), widgets);
                // ui state
                // writeClientAction(0xddd, (buf) -> {});
            }
            Widget w = widgets.get(index);
            w.setActive(state);
            w.setVisible(state);
        }
        if (id == LATE_INIT_WIDGET) {
            Tuple<BooleanSupplier, Supplier<Widget>> c = lateInit.get(buffer.readInt());
            BooleanSupplier test = c.getFirst();
            Supplier<Widget> supplier = c.getSecond();
            if (test.getAsBoolean()) {
                addWidgetWithTest(supplier.get(), test);
            } else {
                SusyLog.logger.error(
                        "ui state desynced, the ui is likely to implode soon. supplier/test {}/{}",
                        supplier,
                        test);
                // decided to make it add anyways so that it does the error in the log
                addWidgetWithTest(supplier.get(), test);
            }
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        GlStateManager.color(
                gui.getRColorForOverlay(), gui.getGColorForOverlay(), gui.getBColorForOverlay(), 1.0F);
        for (Widget widget : widgets) {
            if (widget.isVisible() && tests.getOrDefault(widget, defaultPredicate).getAsBoolean()) {
                widget.drawInBackground(mouseX, mouseY, partialTicks, context);
                GlStateManager.color(
                        gui.getRColorForOverlay(), gui.getGColorForOverlay(), gui.getBColorForOverlay(), 1.0F);
            }
        }
    }

    @Override
    public void drawInForeground(int mouseX, int mouseY) {
        GlStateManager.color(1, 1, 1, 1);
        for (Widget widget : widgets) {
            if (widget.isVisible() && tests.getOrDefault(widget, defaultPredicate).getAsBoolean()) {
                widget.drawInForeground(mouseX, mouseY);
                GlStateManager.color(1, 1, 1, 1);
            }
        }
    }

    private void updateState(Widget w, boolean ns) {
        if (!(w.isVisible() == ns && w.isActive() == ns)) {
            w.setVisible(ns);
            w.setActive(ns);
            writeUpdateInfo(
                    STATE_UPDATE,
                    buf -> {
                        buf.writeBoolean(ns);
                        buf.writeInt(widgets.indexOf(w));
                    });
        }
    }

    public static class BlueprintRowState {

        public final List<DataStorageLoader> slots;
        public final int[] validMultiplierValues;
        public boolean shortView = false;
        public int multiplierIndex = 0;

        public BlueprintRowState(List<DataStorageLoader> slots, int[] validMultiplierValues) {
            this.slots = slots;
            this.validMultiplierValues = validMultiplierValues;
        }

        public int getMultiplier() {
            return validMultiplierValues[multiplierIndex];
        }

        /**
         * Returns null on INVALID_CARD; empty list is a legitimate "no components" result.
         */
        public List<AbstractComponent<?>> materializeComponents() {
            if (shortView) {
                ItemStack firstItem = slots.isEmpty() ? ItemStack.EMPTY : slots.get(0).getStackInSlot(0);
                NBTTagCompound firstnbt = firstItem.hasTagCompound() ? firstItem.getTagCompound() : null;
                if (firstnbt == null || !firstnbt.hasKey("name")) return null;
                AbstractComponent<?> proto = AbstractComponent.getComponentFromName(firstnbt.getString("name"));
                if (proto == null) return null;
                Optional<?> templateOpt = proto.readFromNBT(firstnbt);
                if (!templateOpt.isPresent()) return null;
                @SuppressWarnings("unchecked")
                AbstractComponent<?> template = (AbstractComponent<?>) templateOpt.get();
                int count = validMultiplierValues[multiplierIndex];
                return Stream.generate(() -> template).limit(count).collect(Collectors.toList());
            } else {
                return slots.stream()
                        .map(s -> s.getStackInSlot(0))
                        .filter(ItemStack::hasTagCompound)
                        .map(ItemStack::getTagCompound)
                        .filter(t -> t.hasKey("name"))
                        .map(t -> {
                            AbstractComponent<?> proto = AbstractComponent.getComponentFromName(t.getString("name"));
                            if (proto == null) return Optional.<AbstractComponent<?>>empty();
                            return proto.readFromNBT(t);
                        })
                        .filter(Optional::isPresent)
                        .map(opt -> (AbstractComponent<?>) opt.get())
                        .collect(Collectors.toList());
            }
        }

        public NBTTagCompound writeStateToNBT() {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setBoolean("shortView", shortView);
            tag.setInteger("multiplierIndex", multiplierIndex);
            NBTTagList slotList = new NBTTagList();
            for (int i = 0; i < slots.size(); i++) {
                ItemStack stack = slots.get(i).getStackInSlot(0);
                if (!stack.isEmpty()) {
                    NBTTagCompound slotTag = new NBTTagCompound();
                    slotTag.setInteger("slot", i);
                    stack.writeToNBT(slotTag);
                    slotList.appendTag(slotTag);
                }
            }
            tag.setTag("slots", slotList);
            return tag;
        }

        public void readStateFromNBT(NBTTagCompound tag) {
            shortView = tag.getBoolean("shortView");
            int idx = tag.getInteger("multiplierIndex");
            multiplierIndex = (idx >= 0 && idx < validMultiplierValues.length) ? idx : 0;
            for (DataStorageLoader slot : slots) {
                slot.setStackInSlot(0, ItemStack.EMPTY);
            }
            NBTTagList slotList = tag.getTagList("slots", Constants.NBT.TAG_COMPOUND);
            for (int i = 0; i < slotList.tagCount(); i++) {
                NBTTagCompound slotTag = slotList.getCompoundTagAt(i);
                int slot = slotTag.getInteger("slot");
                if (slot >= 0 && slot < slots.size()) {
                    slots.get(slot).setStackInSlot(0, new ItemStack(slotTag));
                }
            }
        }
    }

    public static class FuelRegistrySelectorWidget extends AbstractWidgetGroup {

        // client -> server action id used to forward a phantom slot change to the server
        private static final int SYNC_FLUID = 10000;

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

        private static List<Fluid> getMaterialFluids(Material mat) {
            List<Fluid> fluids = new ArrayList<>();
            Fluid fl1 = mat.getFluid(FluidStorageKeys.LIQUID);
            if (fl1 != null) fluids.add(fl1);
            FluidProperty fluidprop = mat.getProperty(PropertyKey.FLUID);
            if (fluidprop != null) {
                Fluid fl2 = fluidprop.get(fluidprop.getPrimaryKey());
                if (fl2 != null) fluids.add(fl2);
            }
            Fluid fl3 = mat.getFluid();
            if (fl3 != null) fluids.add(fl3);
            return fluids;
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
            stacks.add(index, null);

            final int i = index;
            Supplier<FluidStack> supplier = () -> stacks.get(i);
            PhantomFluidWidget widget = new PhantomFluidWidget(
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
                        this.onFluidChanged(stack, i);
                        this.writeUpdateInfo(10000, (buffer) -> {
                            buffer.writeInt(i);
                            buffer.writeBoolean(stack == null);
                            if (stack != null) {
                                buffer.writeCompoundTag(stack.writeToNBT(new NBTTagCompound()));
                            }
                        });
                    });
            // run the supplier/updater on the client so the player's selection is captured there
            return widget;
        }

        @Override
        public void readUpdateInfo(int id, PacketBuffer buffer) {
            super.readUpdateInfo(id, buffer);
            if (id == 10000) {
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
}
