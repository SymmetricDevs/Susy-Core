package supersymmetry.client.renderer.gun;

import java.nio.FloatBuffer;
import java.util.Objects;

import javax.vecmath.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.client.event.RenderSpecificHandEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;

import org.lwjgl.BufferUtils;

import gregtech.api.GregTechAPI;
import supersymmetry.Supersymmetry;
import supersymmetry.api.input.SuSyKeyBind;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.network.CPacketGunInput;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = Side.CLIENT)
public final class GunClientHandler {

    private static InputState lastState = null;

    private static final FloatBuffer MATRIX_BUFFER = BufferUtils.createFloatBuffer(16);

    private GunClientHandler() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;

        if (player == null || mc.world == null) {
            lastState = null;
            return;
        }

        boolean unloadPressed = SuSyKeyBind.UNLOAD_GUN.isPressed();
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
        boolean unload = inputAllowed && unloadPressed;
        boolean reload = inputAllowed && reloadPressed;
        boolean cycleMode = inputAllowed && cyclePressed;

        InputState current = new InputState(trigger, aim, unload, hand);
        boolean pulse = reload || cycleMode || unload;

        if (pulse || !current.equals(lastState)) {
            GregTechAPI.networkHandler.sendToServer(new CPacketGunInput(trigger, aim, reload, cycleMode, unload, hand));
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

        if (event.getHand() != EnumHand.MAIN_HAND || mc.player.isInvisible())
            return;

        // The vanilla arm chain is replaced by the ones the model holds, so both arms are drawn with the gun, which
        // is drawn by the main hand event this runs inside of.
        EnumHandSide primary = mc.player.getPrimaryHand();
        renderArm(mc, event, primary, primary);
        if (mc.player.getHeldItemOffhand().isEmpty()) {
            renderArm(mc, event, primary, primary.opposite());
        }
    }

    // draw the arm exactly where it is in blockbench relative to the gun
    private static void renderArm(Minecraft mc, RenderSpecificHandEvent event, EnumHandSide primary,
                                  EnumHandSide side) {
        Matrix4f transform = GunItemStackRenderer.firstPersonArmTransform(event.getItemStack(), side, primary,
                event.getEquipProgress());
        if (transform == null)
            return;

        // renderArmFirstPerson applies its own transforms without pushing, and it runs before the item's own push, so
        // the state has to be restored here or the gun would inherit the arm's transforms.
        GlStateManager.pushMatrix();
        multMatrix(transform);
        mc.getItemRenderer().renderArmFirstPerson(event.getEquipProgress(), 0, side);
        GlStateManager.popMatrix();
    }

    private static void multMatrix(Matrix4f matrix) {
        MATRIX_BUFFER.clear();
        float[] column = new float[4];
        for (int i = 0; i < 4; i++) {
            matrix.getColumn(i, column);
            MATRIX_BUFFER.put(column);
        }
        MATRIX_BUFFER.flip();
        GlStateManager.multMatrix(MATRIX_BUFFER);
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
                new CPacketGunInput(false, false, false, false, false, lastState.hand));
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
        private final boolean unload;
        private final EnumHand hand;

        private InputState(boolean trigger, boolean aim, boolean unload, EnumHand hand) {
            this.trigger = trigger;
            this.aim = aim;
            this.hand = hand;
            this.unload = unload;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (!(o instanceof InputState))
                return false;

            InputState other = (InputState) o;

            return trigger == other.trigger && aim == other.aim && hand == other.hand && unload == other.unload;
        }

        @Override
        public int hashCode() {
            return Objects.hash(trigger, aim, unload, hand);
        }
    }
}
