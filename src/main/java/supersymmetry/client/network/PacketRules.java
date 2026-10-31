package supersymmetry.client.network;

public final class PacketRules {

    private PacketRules() {}

    public static boolean invalidSlot(int slot, int count) {
        return slot < 0 || slot >= count;
    }

    public static boolean needsWorld(int reason) {
        return reason == 1 || reason == 2 || reason == 4 || reason == 6 || reason == 7 || reason == 8 || reason == 10;
    }
}
