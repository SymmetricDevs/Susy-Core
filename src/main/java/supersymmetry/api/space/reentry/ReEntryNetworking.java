package supersymmetry.api.space.reentry;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

import io.netty.buffer.ByteBuf;

/**
 * Networking glue for the re-entry sequence.
 * Packets sent server → client:
 * 0 OrbitProgressPacket – normalised orbit progress (0-1)
 * 1 StartReEntryPacket – triggers startReEntry() on the client renderer
 * 2 PlasmaPacket – plasma/heat shield intensity (0-1)
 * 3 DescentProgressPacket – normalised descent progress (0-1)
 */
public class ReEntryNetworking {

    private static SimpleNetworkWrapper CHANNEL;
    private static boolean initialised = false;

    public static void init() {
        if (initialised) return;
        CHANNEL = NetworkRegistry.INSTANCE.newSimpleChannel("susy_reentry");
        CHANNEL.registerMessage(OrbitProgressHandler.class, OrbitProgressPacket.class, 0, Side.CLIENT);
        CHANNEL.registerMessage(StartReEntryHandler.class, StartReEntryPacket.class, 1, Side.CLIENT);
        CHANNEL.registerMessage(PlasmaHandler.class, PlasmaPacket.class, 2, Side.CLIENT);
        CHANNEL.registerMessage(DescentProgressHandler.class, DescentProgressPacket.class, 3, Side.CLIENT);
        initialised = true;
    }

    public static void sendOrbitProgress(List<EntityPlayerMP> players, float progress) {
        OrbitProgressPacket pkt = new OrbitProgressPacket(progress);
        for (EntityPlayerMP p : players) CHANNEL.sendTo(pkt, p);
    }

    public static void sendStartReEntry(List<EntityPlayerMP> players) {
        StartReEntryPacket pkt = new StartReEntryPacket();
        for (EntityPlayerMP p : players) CHANNEL.sendTo(pkt, p);
    }

    public static void sendPlasmaIntensity(List<EntityPlayerMP> players, float intensity) {
        PlasmaPacket pkt = new PlasmaPacket(intensity);
        for (EntityPlayerMP p : players) CHANNEL.sendTo(pkt, p);
    }

    public static void sendDescentProgress(List<EntityPlayerMP> players, float descent) {
        DescentProgressPacket pkt = new DescentProgressPacket(descent);
        for (EntityPlayerMP p : players) CHANNEL.sendTo(pkt, p);
    }

    public static class OrbitProgressPacket implements IMessage {

        float progress;

        public OrbitProgressPacket() {}

        public OrbitProgressPacket(float p) {
            this.progress = p;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            progress = buf.readFloat();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeFloat(progress);
        }
    }

    public static class OrbitProgressHandler implements IMessageHandler<OrbitProgressPacket, IMessage> {

        @Override
        public IMessage onMessage(OrbitProgressPacket msg, MessageContext ctx) {
            // Must execute on the main client thread
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                ReEntryClientState.orbitProgress = msg.progress;
                applyToRenderer();
            });
            return null;
        }
    }

    public static class StartReEntryPacket implements IMessage {

        public StartReEntryPacket() {}

        @Override
        public void fromBytes(ByteBuf buf) {}

        @Override
        public void toBytes(ByteBuf buf) {}
    }

    public static class StartReEntryHandler implements IMessageHandler<StartReEntryPacket, IMessage> {

        @Override
        public IMessage onMessage(StartReEntryPacket msg, MessageContext ctx) {
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                ReEntryClientState.reEntryStarted = true;
                applyToRenderer();
            });
            return null;
        }
    }

    public static class PlasmaPacket implements IMessage {

        float intensity;

        public PlasmaPacket() {}

        public PlasmaPacket(float i) {
            this.intensity = i;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            intensity = buf.readFloat();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeFloat(intensity);
        }
    }

    public static class PlasmaHandler implements IMessageHandler<PlasmaPacket, IMessage> {

        @Override
        public IMessage onMessage(PlasmaPacket msg, MessageContext ctx) {
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                ReEntryClientState.plasmaIntensity = msg.intensity;
                applyToRenderer();
            });
            return null;
        }
    }

    public static class DescentProgressPacket implements IMessage {

        float descent;

        public DescentProgressPacket() {}

        public DescentProgressPacket(float d) {
            this.descent = d;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            descent = buf.readFloat();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeFloat(descent);
        }
    }

    public static class DescentProgressHandler implements IMessageHandler<DescentProgressPacket, IMessage> {

        @Override
        public IMessage onMessage(DescentProgressPacket msg, MessageContext ctx) {
            net.minecraft.client.Minecraft.getMinecraft().addScheduledTask(() -> {
                ReEntryClientState.descentProgress = msg.descent;
                applyToRenderer();
            });
            return null;
        }
    }

    private static void applyToRenderer() {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getMinecraft();
        if (mc.world == null) return;
        net.minecraft.world.WorldProvider provider = mc.world.provider;
        if (!(provider instanceof WorldProviderReEntry)) return;

        WorldProviderReEntry wrp = (WorldProviderReEntry) provider;
        ReEntryDimension cfg = wrp.getConfig();
        if (cfg == null || cfg.renderer == null) return;

        ReEntryRenderer renderer = cfg.renderer;
        renderer.orbitProgress = ReEntryClientState.orbitProgress;
        renderer.reEntryStarted = ReEntryClientState.reEntryStarted;
        renderer.plasmaIntensity = ReEntryClientState.plasmaIntensity;
        renderer.descentProgress = ReEntryClientState.descentProgress;
        renderer.podRotationT = ReEntryClientState.podRotationT;
    }

    public static class ReEntryClientState {

        public static float orbitProgress = 0f;
        public static boolean reEntryStarted = false;
        public static float plasmaIntensity = 0f;
        public static float descentProgress = 0f;
        public static float podRotationT = 0f; // 0 = sideways orbit, 1 = vertical descent
    }

    public static void sendPodRotation(List<EntityPlayerMP> players, float t) {
        if (CHANNEL == null) return;
        PodRotationPacket pkt = new PodRotationPacket(t);
        for (EntityPlayerMP p : players) CHANNEL.sendTo(pkt, p);
    }

    public static class PodRotationPacket implements IMessage {

        float t;

        public PodRotationPacket() {}

        public PodRotationPacket(float t) {
            this.t = t;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            t = buf.readFloat();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeFloat(t);
        }
    }

    public static class PodRotationHandler implements IMessageHandler<PodRotationPacket, IMessage> {

        @Override
        public IMessage onMessage(PodRotationPacket msg, MessageContext ctx) {
            Minecraft.getMinecraft().addScheduledTask(() -> {
                ReEntryClientState.podRotationT = msg.t;
                applyToRenderer();
            });
            return null;
        }
    }
}
