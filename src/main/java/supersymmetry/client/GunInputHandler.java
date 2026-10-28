package supersymmetry.client;

import java.util.Objects;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import gregtech.api.GregTechAPI;
import supersymmetry.Supersymmetry;
import supersymmetry.api.input.SuSyKeyBind;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.network.CPacketGunInput;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = Side.CLIENT)
public final class GunInputHandler {

    private static InputState lastState = null;

    // X, Y, Z, Scale
    private static final float[] DEFAULT = new float[] { -0.56f, 0.19f, 0.10f, 1.0f };
    private static final float[][] SPECIFIC = new float[100][4];

    static {
        for (int i = 0; i < SPECIFIC.length; i++) {
            SPECIFIC[i] = DEFAULT;
        }
        SPECIFIC[31] = new float[] { -0.56f, 0.19f, 0.10f, 1.0f };
        SPECIFIC[33] = new float[] { -0.60f, 0.28f, 0.12f, 1.1f };
        SPECIFIC[35] = new float[] { -0.58f, 0.25f, 0.10f, 1.0f };
        SPECIFIC[37] = new float[] { -0.56f, 0.19f, 0.08f, 1.0f };
    }

    private GunInputHandler() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;

        if (player == null || mc.world == null) {
            lastState = null;
            return;
        }

        boolean reloadPressed = SuSyKeyBind.RELOAD_GUN.isPressed();
        boolean cyclePressed = SuSyKeyBind.CYCLE_FIRE_MODE.isPressed();

        EnumHand hand = findGunHand(player);

        if (hand == null) {
            releaseIfNeeded();
            return;
        }

        boolean inputAllowed = mc.currentScreen == null;
        boolean trigger = inputAllowed && mc.gameSettings.keyBindAttack.isKeyDown();
        boolean aim = inputAllowed && SuSyKeyBind.AIM.isKeyDown();
        boolean reload = inputAllowed && reloadPressed;
        boolean cycleMode = inputAllowed && cyclePressed;

        InputState current = new InputState(trigger, aim, hand);
        boolean pulse = reload || cycleMode;

        if (pulse || !current.equals(lastState)) {
            GregTechAPI.networkHandler.sendToServer(
                    new CPacketGunInput(trigger, aim, reload, cycleMode, hand));
            lastState = current;
        }
    }

    @SubscribeEvent
    public static void onRenderHand(RenderSpecificHandEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null)
            return;

        ItemStack stack = event.getItemStack();
        if (SuSyMetaItems.getGunBehaviour(stack) == null)
            return;

        if (SuSyKeyBind.AIM.isKeyDown() && mc.currentScreen == null) {
            int meta = stack.getMetadata();
            float[] value = (meta >= 0 && meta < SPECIFIC.length) ? SPECIFIC[meta] : DEFAULT;

            float handSide = (event.getHand() == EnumHand.MAIN_HAND) ? 1.0F : -1.0F;

            if (value[3] != 1.0F) {
                GlStateManager.scale(value[3], value[3], value[3]);
            }

            GlStateManager.translate(value[0] * handSide, value[1], value[2]);
        }
    }

    @SubscribeEvent
    public static void onLeftClick(PlayerInteractEvent.LeftClickBlock event) {
        ItemStack stack = event.getItemStack();
        if (isGun(stack)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        ItemStack stack = event.getItemStack();
        if (isGun(stack)) {
            event.getEntityPlayer().resetCooldown();
        }
    }

    private static void releaseIfNeeded() {
        if (lastState == null)
            return;
        GregTechAPI.networkHandler.sendToServer(
                new CPacketGunInput(false, false, false, false, lastState.hand));
        lastState = null;
    }

    private static EnumHand findGunHand(EntityPlayerSP player) {
        if (isGun(player.getHeldItem(EnumHand.MAIN_HAND)))
            return EnumHand.MAIN_HAND;
        if (isGun(player.getHeldItem(EnumHand.OFF_HAND)))
            return EnumHand.OFF_HAND;
        return null;
    }

    private static boolean isGun(ItemStack stack) {
        return stack != null && !stack.isEmpty() && SuSyMetaItems.getGunBehaviour(stack) != null;
    }

    private static final class InputState {

        private final boolean trigger;
        private final boolean aim;
        private final EnumHand hand;

        private InputState(boolean trigger, boolean aim, EnumHand hand) {
            this.trigger = trigger;
            this.aim = aim;
            this.hand = hand;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof InputState))
                return false;

            InputState other = (InputState) o;

            return trigger == other.trigger && aim == other.aim && hand == other.hand;
        }

        @Override
        public int hashCode() {
            return Objects.hash(trigger, aim, hand);
        }
    }
}
