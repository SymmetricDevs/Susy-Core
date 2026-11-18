package supersymmetry.client.renderer.gun;

import static supersymmetry.common.item.behavior.SuSyGunBehavior.IRON_SIGHT_ZOOM;

import java.util.Objects;

import javax.vecmath.Matrix4f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.EntityViewRenderEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
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
import supersymmetry.common.item.behavior.SuSyGunBehavior;
import supersymmetry.common.network.CPacketGunInput;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = Side.CLIENT)
public final class GunClientHandler {

    private static InputState lastState = null;

    private static float aimProgress = 0f;
    private static float lastAimProgress = 0f;

    private GunClientHandler() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        EntityPlayerSP player = mc.player;

        if (player == null || mc.world == null) {
            lastState = null;
            resetAim();
            return;
        }

        boolean unloadPressed = SuSyKeyBind.UNLOAD_GUN.isPressed();
        boolean reloadPressed = SuSyKeyBind.RELOAD_GUN.isPressed();
        boolean cyclePressed = SuSyKeyBind.CYCLE_FIRE_MODE.isPressed();

        EnumHand hand = findGunHand(player);

        if (hand == null) {
            releaseIfNeeded();
            resetAim();
            return;
        }

        boolean inputAllowed = mc.currentScreen == null;
        boolean trigger = inputAllowed && mc.gameSettings.keyBindAttack.isKeyDown();
        // right click aims, and the gun takes its aim time to get all the way to the sight view node
        boolean aim = inputAllowed &&
                (mc.gameSettings.keyBindUseItem.isKeyDown() || SuSyKeyBind.AIM.isKeyDown());
        boolean unload = inputAllowed && unloadPressed;
        boolean reload = inputAllowed && reloadPressed;
        boolean cycleMode = inputAllowed && cyclePressed;

        InputState current = new InputState(trigger, aim, unload, hand);
        boolean pulse = reload || cycleMode || unload;

        if (pulse || !current.equals(lastState)) {
            GregTechAPI.networkHandler.sendToServer(new CPacketGunInput(trigger, aim, reload, cycleMode, unload, hand));
            lastState = current;
        }

        updateAim(player.getHeldItem(hand), aim);
    }

    private static void updateAim(ItemStack stack, boolean aiming) {
        lastAimProgress = aimProgress;

        SuSyGunBehavior gun = SuSyMetaItems.getGunBehaviour(stack);
        double aimTicks = gun == null ? 0 : gun.getAimTime();
        float step = aimTicks > 0 ? (float) (1.0 / aimTicks) : 1f;

        if (aiming) {
            aimProgress = Math.min(1f, aimProgress + step);
        } else {
            aimProgress = Math.max(0f, aimProgress - step);
        }
    }

    private static void resetAim() {
        aimProgress = 0f;
        lastAimProgress = 0f;
    }

    private static float renderAimProgress(float partialTicks) {
        return MathHelper.clamp(lastAimProgress + (aimProgress - lastAimProgress) * partialTicks, 0f, 1f);
    }

    @SubscribeEvent
    public static void onRenderHand(RenderSpecificHandEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.player == null)
            return;

        ItemStack stack = event.getItemStack();

        if (event.getHand() == EnumHand.OFF_HAND) {
            // both arms are already drawn above when the offhand is empty, so vanilla's pass must not draw one too
            if (stack.isEmpty() && SuSyMetaItems.getGunBehaviour(mc.player.getHeldItemMainhand()) != null)
                event.setCanceled(true);
            return;
        }

        if (SuSyMetaItems.getGunBehaviour(stack) == null || mc.player.isInvisible())
            return;

        // without the view bones the gun cannot be placed from the model, so let vanilla draw it
        if (!GunItemStackRenderer.hasViewNodes(stack))
            return;

        // the gun places itself from its view node bones, which vanilla's hand transforms would fight with
        event.setCanceled(true);

        EnumHandSide primary = mc.player.getPrimaryHand();
        float partialTicks = event.getPartialTicks();
        float aim = renderAimProgress(partialTicks);

        GunItemStackRenderer.renderFirstPerson(stack, primary, event.getEquipProgress(), partialTicks, aim);

        // The vanilla arm chain is replaced by the ones the model holds, so both arms are drawn with the gun.
        renderArm(mc, event, primary, aim);
        if (mc.player.getHeldItemOffhand().isEmpty()) {
            renderArm(mc, event, primary, aim);
        }
    }

    // draw the arm exactly where it is in blockbench relative to the gun
    private static void renderArm(Minecraft mc, RenderSpecificHandEvent event,
                                  EnumHandSide side, float aim) {
        Matrix4f transform = GunItemStackRenderer.firstPersonArmTransform(event.getItemStack(), side,
                event.getEquipProgress(), aim);
        if (transform == null)
            return;

        // renderArmFirstPerson applies its own transforms without pushing, so the matrix has to be restored here or
        // the next arm would inherit this one's transforms
        GlStateManager.pushMatrix();
        GunItemStackRenderer.multMatrix(transform);
        mc.getItemRenderer().renderArmFirstPerson(0f, 0f, side);
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public static void onRenderOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.CROSSHAIRS)
            return;

        EntityPlayerSP player = Minecraft.getMinecraft().player;
        if (player != null && findGunHand(player) != null && aimProgress > 0) event.setCanceled(true);
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

    @SubscribeEvent
    public static void onFOVModifier(EntityViewRenderEvent.FOVModifier event) {
        float progress = renderAimProgress((float) event.getRenderPartialTicks());
        event.setFOV(event.getFOV() * (1 - (IRON_SIGHT_ZOOM - 1) * progress));
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
            if (!(o instanceof InputState other))
                return false;

            return trigger == other.trigger && aim == other.aim && hand == other.hand && unload == other.unload;
        }

        @Override
        public int hashCode() {
            return Objects.hash(trigger, aim, unload, hand);
        }
    }
}
