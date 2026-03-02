package supersymmetry.common.mui.widget;

import static supersymmetry.api.capability.SuSyDataCodes.LATE_INIT_WIDGET;
import static supersymmetry.api.capability.SuSyDataCodes.STATE_UPDATE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Tuple;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.gui.widgets.AbstractWidgetGroup;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import supersymmetry.api.SusyLog;

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
}
