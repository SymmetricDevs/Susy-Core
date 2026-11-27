package supersymmetry.common.guns.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import dev.tianmi.sussypatches.api.mui2.factory.MetaItemGuiFactory;
import gregtech.api.GregTechAPI;
import supersymmetry.Supersymmetry;
import supersymmetry.client.renderer.gun.GunAnimation;
import supersymmetry.common.guns.GunClipLengths;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.item.behavior.GunFireMode;
import supersymmetry.common.item.behavior.GunMagazineBehavior;
import supersymmetry.common.item.behavior.SuSyGunBehavior;
import supersymmetry.common.network.SPacketGunAnimation;
import supersymmetry.common.network.SPacketGunRecoil;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public final class GunServerHandler {

    private static final Map<UUID, GunState> playerStates = new HashMap<>();

    private GunServerHandler() {}

    public static void onInput(EntityPlayerMP player, boolean trigger, boolean aim, boolean reload,
                               boolean cycleMode, boolean unload, boolean openAttachments, EnumHand hand) {
        if (player == null || !player.isEntityAlive() || player.isSpectator()) {
            return;
        }

        UUID id = player.getUniqueID();
        ItemStack stack = player.getHeldItem(hand);
        SuSyGunBehavior gun = SuSyMetaItems.getGunBehaviour(stack);

        if (gun == null) {
            stopReloadAnimation(player);
            playerStates.remove(id);
            return;
        }

        // opening the attachment screen is a one-off action
        if (openAttachments) {
            MetaItemGuiFactory.openFromHand(player, hand);
            return;
        }

        GunState state = syncHeld(player, playerStates.get(id), stack, hand);
        state.aim = aim;
        state.unload = unload;

        // the gun cannot be used until it has been drawn
        if (state.equipping) {
            state.fireRequested = false;
            state.trigger = false;
            return;
        }

        if (cycleMode && !state.reloading && state.burstLeft == 0) {
            gun.cycleMode(player, stack);
        }

        if (reload && !state.reloading && !state.unloading && state.burstLeft == 0 && !gun.isFull(stack)) {
            startReload(player, state, gun, stack);
        }

        if (unload && !state.unloading && !state.reloading && state.burstLeft == 0) {
            startUnload(player, state, gun, stack);
        }

        if (trigger && !state.trigger) {
            state.fireRequested = true;
        }
        state.trigger = trigger;
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END ||
                !(event.player instanceof EntityPlayerMP player) || event.side.isClient()) {
            return;
        }

        GunState state = playerStates.get(player.getUniqueID());

        if (!player.isEntityAlive() || player.isSpectator()) {
            if (state != null) {
                state.reset(player);
                playerStates.remove(player.getUniqueID());
            }
            return;
        }

        EnumHand hand = activeGunHand(player);
        if (hand == null) {
            if (state != null) {
                state.reset(player);
                playerStates.remove(player.getUniqueID());
            }
            return;
        }

        ItemStack stack = player.getHeldItem(hand);
        SuSyGunBehavior gun = SuSyMetaItems.getGunBehaviour(stack);
        if (gun == null) {
            if (state != null) {
                state.reset(player);
                playerStates.remove(player.getUniqueID());
            }
            return;
        }

        long now = player.world.getTotalWorldTime();
        state = syncHeld(player, state, stack, hand);

        if (state.equipping) {
            state.fireRequested = false;
            state.trigger = false;
            if (now < state.equipEnd) {
                return;
            }
            state.equipping = false;
        }

        // recoil handling: first jump to (original pitch + 10x recoil), then smoothly bring it down to (original
        // pitch + recoil) according to the equation: pitch(tick) = originalPitch + 10 * recoil - 0.1 * (tick ^ 2)

        if (state.recoilTick <= 0) {
            state.handFatigue -= 0.5f;
        }

        if (state.recoilTick > 1) {
            int lastTick = (int) Math.floor(10 * Math.sqrt(gun.getRecoilStrength() * state.recoilRNG));
            if (state.recoilTick >= lastTick) {
                state.recoilTick = 0;
            } else {
                float recoil = -0.1f * state.recoilTick * state.recoilTick * state.recoilRNG;
                GregTechAPI.networkHandler.sendTo(new SPacketGunRecoil(recoil, 0), player);
                state.recoilTick++;
            }
        }

        if (state.recoilTick == 1) {
            Random random = player.getRNG();
            state.recoilRNG = random.nextFloat() / 2f + 0.75f;
            GregTechAPI.networkHandler
                    .sendTo(new SPacketGunRecoil(gun.getStats(stack, SuSyGunBehavior.isBraced(player)).getRecoil() * 10,
                            gun.getStats(stack, SuSyGunBehavior.isBraced(player)).getRecoil() * state.recoilRNG *
                                    Math.min(1 + state.handFatigue, 5)),
                            player);
            state.handFatigue += gun.getRecoilStrength();
            state.recoilTick = 2;
        }

        if (state.reloading) {
            state.fireRequested = false;
            if (now >= state.reloadEnd) {
                ItemStack mag = GunMagazineBehavior.findValidMagazine(player, stack);
                if (mag != null) {
                    gun.reload(player, stack, mag);
                    state.reloading = false;
                } else {
                    state.reloading = false;
                    stopReloadAnimation(player);
                    return;
                }
                stopReloadAnimation(player);
            }
            return;
        }

        if (state.unloading) {
            state.fireRequested = false;
            if (now >= state.unloadEnd) {
                gun.unload(player, stack);
                state.reloading = false;
            }
            return;
        }

        if (state.burstLeft > 0) {
            state.fireRequested = false;
            if (now >= state.burstNext) {
                if (shoot(player, state, gun, stack)) {
                    state.recoilTick = 1;
                    state.burstLeft--;
                    state.burstNext = now + gun.getBurstIntervalTicks();
                } else {
                    state.burstLeft = 0;
                }
            }
            return;
        }

        GunFireMode mode = gun.getSelectedMode(stack);
        boolean wantsFire = mode == GunFireMode.FULL_AUTO ? state.trigger : state.fireRequested;
        state.fireRequested = false;

        if (!wantsFire || now < state.nextShot) {
            return;
        }

        state.nextShot = now + gun.getCooldownTicks(mode);

        if (!shoot(player, state, gun, stack)) {
            return;
        }
        state.recoilTick = 1;
        if (mode == GunFireMode.BURST) {
            state.burstLeft = gun.getBurstCount() - 1;
            state.burstNext = now + gun.getBurstIntervalTicks();
        }
    }

    private static boolean shoot(EntityPlayerMP player, GunState state, SuSyGunBehavior gun, ItemStack stack) {
        if (!gun.consumeRound(stack)) {
            return false;
        }
        gun.shootOnce(player, state.hand, stack, state.aim);
        GregTechAPI.networkHandler.sendTo(new SPacketGunRecoil(0f, 0f, true), player);

        return true;
    }

    private static EnumHand activeGunHand(EntityPlayerMP player) {
        if (SuSyMetaItems.getGunBehaviour(player.getHeldItem(EnumHand.MAIN_HAND)) != null) {
            return EnumHand.MAIN_HAND;
        }
        if (SuSyMetaItems.getGunBehaviour(player.getHeldItem(EnumHand.OFF_HAND)) != null) {
            return EnumHand.OFF_HAND;
        }
        return null;
    }

    /**
     * Keeps the state pointing at the gun that is actually held. Drawing a gun (switching to it, or to another slot
     * or hand) starts its equip animation, so exactly the switch does that, without having to wait for any input.
     */
    private static GunState syncHeld(EntityPlayerMP player, GunState state, ItemStack stack, EnumHand hand) {
        int slot = slotOf(player, hand);
        boolean sameGun = state != null && state.hand == hand && state.slot == slot &&
                state.item == stack.getItem() && state.meta == stack.getMetadata();
        if (sameGun) {
            return state;
        }

        if (state == null) {
            state = new GunState();
            playerStates.put(player.getUniqueID(), state);
        } else {
            state.reset(player);
        }

        state.hand = hand;
        state.slot = slot;
        state.item = stack.getItem();
        state.meta = stack.getMetadata();
        startEquip(player, state, stack);
        return state;
    }

    private static void startEquip(EntityPlayerMP player, GunState state, ItemStack stack) {
        float length = GunClipLengths.of(stack, GunAnimation.GunAnimationNames.EQUIP);
        if (length <= 0f) {
            return;
        }
        state.equipping = true;
        state.burstLeft = 0;
        state.fireRequested = false;
        state.equipEnd = player.world.getTotalWorldTime() + (long) Math.ceil(length);
        sendAnimation(player, GunAnimation.GunAnimationNames.EQUIP, true);
    }

    private static void startReload(EntityPlayerMP player, GunState state, SuSyGunBehavior gun, ItemStack stack) {
        ItemStack mag = GunMagazineBehavior.findValidMagazine(player, stack);
        if (mag == null) {
            return;
        }

        float length = GunClipLengths.of(stack, GunAnimation.GunAnimationNames.RELOAD);
        // guns without a reload animation fall back to the set reloadTicks
        long duration = length > 0f ? (long) Math.ceil(length) : gun.getReloadTicks();
        if (duration <= 0) {
            gun.reload(player, stack, mag);
            return;
        }

        state.reloading = true;
        state.burstLeft = 0;
        state.fireRequested = false;
        state.reloadEnd = player.world.getTotalWorldTime() + duration;
        if (length > 0f) {
            sendAnimation(player, GunAnimation.GunAnimationNames.RELOAD, true, incomingMagazineSize(gun, mag));
        }
    }

    private static int incomingMagazineSize(SuSyGunBehavior gun, ItemStack mag) {
        GunMagazineBehavior magBehavior = GunMagazineBehavior.getMagBehavior(mag);
        int size = magBehavior == null ? gun.getMaxMagazineSize() : magBehavior.getMaxCapacity();
        return size > gun.getMaxMagazineSize() ? gun.getMaxMagazineSize() : size;
    }

    private static void startUnload(EntityPlayerMP player, GunState state, SuSyGunBehavior gun, ItemStack stack) {
        // FIXME: real unload animations
        float length = GunClipLengths.of(stack, GunAnimation.GunAnimationNames.RELOAD);
        long duration = length > 0f ? (long) Math.ceil(length) / 2 : gun.getReloadTicks() / 2;
        if (duration <= 0) {
            gun.unload(player, stack);
            return;
        }
        state.unloading = true;
        state.burstLeft = 0;
        state.fireRequested = false;
        state.unloadEnd = player.world.getTotalWorldTime() + duration;
    }

    private static void sendAnimation(EntityPlayerMP player, String animation, boolean start) {
        sendAnimation(player, animation, start, 0);
    }

    private static void sendAnimation(EntityPlayerMP player, String animation, boolean start, int magazineSize) {
        GregTechAPI.networkHandler.sendTo(new SPacketGunAnimation(animation, start, magazineSize), player);
    }

    private static void stopReloadAnimation(EntityPlayerMP player) {
        sendAnimation(player, GunAnimation.GunAnimationNames.RELOAD, false);
    }

    private static int slotOf(EntityPlayerMP player, EnumHand hand) {
        return hand == EnumHand.MAIN_HAND ? player.inventory.currentItem : -1;
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        playerStates.remove(event.player.getUniqueID());
    }

    private static final class GunState {

        private EnumHand hand = EnumHand.MAIN_HAND;
        private int slot = -2;
        private Item item;
        private int meta;

        private boolean trigger;
        private boolean aim;
        private boolean fireRequested;
        private boolean unload;

        private long nextShot;
        private long burstNext;
        private int burstLeft;
        private int recoilTick = 0;
        private float recoilRNG;
        private float handFatigue;

        private boolean reloading;
        private long reloadEnd;
        private boolean unloading;
        private long unloadEnd;
        private boolean equipping;
        private long equipEnd;

        private void reset(EntityPlayerMP player) {
            boolean wasReloading = reloading;
            fireRequested = false;
            burstLeft = 0;
            reloading = false;
            unloading = false;
            equipping = false;
            recoilTick = 0;
            if (wasReloading) {
                stopReloadAnimation(player);
            }
        }
    }
}
