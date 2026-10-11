package supersymmetry.api.input;

import gregtech.api.GTValues;
import gregtech.api.GregTechAPI;
import gregtech.api.util.GTLog;
import gregtech.core.network.packets.PacketKeysPressed;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.tuple.MutablePair;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import supersymmetry.api.SusyLog;
import supersymmetry.common.network.SuSyPacketKeysPressed;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;
import java.util.function.Supplier;

public enum SuSyKeyBind {

    RELOAD_GUN("susy.key.reload_gun", KeyConflictContext.IN_GAME, Keyboard.KEY_R);

    public static final SuSyKeyBind[] VALUES = values();

    public static void init() {
        SusyLog.logger.info("Registering KeyBinds");
        if (FMLCommonHandler.instance().getSide().isClient()) {
            MinecraftForge.EVENT_BUS.register(SuSyKeyBind.class);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public static void onInputEvent(InputEvent.KeyInputEvent event) {
        List<SuSyKeyBind> updating = new ArrayList<>();
        for (SuSyKeyBind keybind : VALUES) {
            boolean previousPressed = keybind.isPressed;
            boolean previousKeyDown = keybind.isKeyDown;
            keybind.isPressed = keybind.isPressed();
            keybind.isKeyDown = keybind.isKeyDown();
            if (previousPressed != keybind.isPressed || previousKeyDown != keybind.isKeyDown) {
                updating.add(keybind);
            }
        }
        if (!updating.isEmpty()) {
            GregTechAPI.networkHandler.sendToServer(new SuSyPacketKeysPressed(updating));
        }
    }
    @SideOnly(Side.CLIENT)
    private KeyBinding keybinding;
    @SideOnly(Side.CLIENT)
    private boolean isPressed, isKeyDown;

    private final WeakHashMap<EntityPlayerMP, MutablePair<Boolean, Boolean>> mapping = new WeakHashMap<>();

    // For Vanilla/Other Mod keybinds
    // Double Supplier to keep client classes from loading
    SuSyKeyBind(Supplier<Supplier<KeyBinding>> keybindingGetter) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.keybinding = keybindingGetter.get().get();
        }
    }

    SuSyKeyBind(String langKey, int button) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.keybinding = new KeyBinding(langKey, button, GTValues.MOD_NAME);
            ClientRegistry.registerKeyBinding(this.keybinding);
        }
    }

    SuSyKeyBind(String langKey, IKeyConflictContext ctx, int button) {
        if (FMLCommonHandler.instance().getSide().isClient()) {
            this.keybinding = new KeyBinding(langKey, ctx, button, GTValues.MOD_NAME);
            ClientRegistry.registerKeyBinding(this.keybinding);
        }
    }

    @SideOnly(Side.CLIENT)
    public KeyBinding toMinecraft() {
        return this.keybinding;
    }

    @SideOnly(Side.CLIENT)
    public boolean isPressed() {
        return this.keybinding.isPressed();
    }

    @SideOnly(Side.CLIENT)
    public boolean isKeyDown() {
        return this.keybinding.isKeyDown();
    }

    public void update(boolean pressed, boolean keyDown, EntityPlayerMP player) {
        MutablePair<Boolean, Boolean> pair = this.mapping.get(player);
        if (pair == null) {
            this.mapping.put(player, MutablePair.of(pressed, keyDown));
        } else {
            pair.left = pressed;
            pair.right = keyDown;
        }
    }

    public boolean isPressed(EntityPlayer player) {
        if (player.world.isRemote) {
            return isPressed();
        } else {
            MutablePair<Boolean, Boolean> pair = this.mapping.get((EntityPlayerMP) player);
            return pair != null && pair.left;
        }
    }

    public boolean isKeyDown(EntityPlayer player) {
        if (player.world.isRemote) {
            return isKeyDown();
        } else {
            MutablePair<Boolean, Boolean> pair = this.mapping.get((EntityPlayerMP) player);
            return pair != null && pair.right;
        }
    }
}
