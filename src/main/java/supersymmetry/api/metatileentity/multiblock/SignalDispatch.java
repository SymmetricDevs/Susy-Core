package supersymmetry.api.metatileentity.multiblock;

import java.util.ArrayList;
import java.util.List;

public class SignalDispatch {

    public record Signal(String name, Runnable action) {}

    private final List<Signal> signals = new ArrayList<>();

    public void add(Runnable action) {
        signals.add(new Signal("", action));
    }

    public void add(String name, Runnable action) {
        signals.add(new Signal(name, action));
    }

    public int ceiling() {
        return signals.size() - 1;
    }

    public void pulse(int sig) {
        if (sig >= 0 && sig < signals.size()) {
            signals.get(sig).action().run();
        }
    }

    public String translationKey(int sig) {
        if (sig >= 0 && sig < signals.size()) {
            return signals.get(sig).name();
        }
        return "";
    }
}
