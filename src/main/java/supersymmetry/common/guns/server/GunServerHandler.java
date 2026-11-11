package supersymmetry.common.guns.server;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import gregtech.api.GregTechAPI;
import supersymmetry.Supersymmetry;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.item.behavior.GunFireMode;
import supersymmetry.common.item.behavior.GunMagazineBehavior;
import supersymmetry.common.item.behavior.SuSyGunBehavior;
import supersymmetry.common.network.SPacketGunRecoil;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public final class GunServerHandler {

    private static final Map<UUID, GunState> playerStates = new HashMap<>();

    private GunServerHandler() {}

    public static void onInput(EntityPlayerMP player, boolean trigger, boolean aim, boolean reload,
                               boolean cycleMode, boolean unload, EnumHand hand) {
        if (player == null || !player.isEntityAlive() || player.isSpectator()) {
            return;
        }

        UUID id = player.getUniqueID();
        ItemStack stack = player.getHeldItem(hand);
        SuSyGunBehavior gun = SuSyMetaItems.getGunBehaviour(stack);

        if (gun == null) {
            playerStates.remove(id);
            return;
        }

        GunState state = playerStates.get(id);
        if (state == null) {
            state = new GunState();
            playerStates.put(id, state);
        }
        int slot = slotOf(player, hand);
        if (state.slot != slot || state.hand != hand) {
            state.reset();
        }
        state.hand = hand;
        state.slot = slot;
        state.aim = aim;
        state.unload = unload;

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
        if (state == null) {
            return;
        }

        if (!player.isEntityAlive() || player.isSpectator()) {
            playerStates.remove(player.getUniqueID());
            return;
        }

        ItemStack stack = player.getHeldItem(state.hand);
        SuSyGunBehavior gun = SuSyMetaItems.getGunBehaviour(stack);
        if (gun == null) {
            playerStates.remove(player.getUniqueID());
            return;
        }

        int slot = slotOf(player, state.hand);
        if (slot != state.slot) {
            state.reset();
            state.slot = slot;
            return;
        }

        long now = player.world.getTotalWorldTime();

        // recoil handling: first jump to (original pitch + 10x recoil), then smoothly bring it down to (original
        // pitch + recoil) according to the equation: pitch(tick) = originalPitch + 10 * recoil - 0.1 * (tick ^ 2)
        if (state.recoilTick > 1) {
            int lastTick = (int) Math.floor(10 * Math.sqrt(state.recoil));
            if (state.recoilTick >= lastTick) {
                state.recoilTick = 0;
            } else {
                float recoil = -0.1f * state.recoilTick * state.recoilTick;
                GregTechAPI.networkHandler.sendTo(new SPacketGunRecoil(recoil, false), player);
                state.recoilTick++;
            }
        }

        if (state.recoilTick == 1) {
            Random random = player.getRNG();
            float recoil = gun.getRecoilStrength() * (random.nextFloat() / 2f + 0.75f);
            state.recoil = recoil;
            GregTechAPI.networkHandler.sendTo(new SPacketGunRecoil(recoil * 10, true), player);
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
                    return;
                }

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

        return true;
    }

    private static void startReload(EntityPlayerMP player, GunState state, SuSyGunBehavior gun, ItemStack stack) {
        if (gun.getReloadTicks() <= 0) {
            ItemStack mag = GunMagazineBehavior.findValidMagazine(player, stack);
            if (mag != null) {
                gun.reload(player, stack, mag);
            } else {
                return;
            }

            return;
        }
        ItemStack mag = GunMagazineBehavior.findValidMagazine(player, stack);
        if (mag != null) {
            state.reloading = true;
            state.burstLeft = 0;
            state.fireRequested = false;
            state.reloadEnd = player.world.getTotalWorldTime() + gun.getReloadTicks();
        }
    }

    private static void startUnload(EntityPlayerMP player, GunState state, SuSyGunBehavior gun, ItemStack stack) {
        if (gun.getReloadTicks() <= 0) {
            gun.unload(player, stack);
            return;
        }
        state.unloading = true;
        state.burstLeft = 0;
        state.fireRequested = false;
        state.unloadEnd = player.world.getTotalWorldTime() + gun.getReloadTicks() / 2;
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

        private boolean trigger;
        private boolean aim;
        private boolean fireRequested;
        private boolean unload;

        private long nextShot;
        private long burstNext;
        private int burstLeft;
        private int recoilTick = 0;
        private float recoil;

        private boolean reloading;
        private long reloadEnd;
        private boolean unloading;
        private long unloadEnd;

        private void reset() {
            fireRequested = false;
            burstLeft = 0;
            reloading = false;
            unloading = false;
            recoilTick = 0;
        }
    }
}
