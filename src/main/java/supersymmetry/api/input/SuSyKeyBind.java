package supersymmetry.api.input;

import java.util.function.Supplier;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.lwjgl.input.Keyboard;

import gregtech.api.GTValues;
import supersymmetry.api.SusyLog;

public enum SuSyKeyBind {

    RELOAD_GUN("susy.key.reload_gun", KeyConflictContext.IN_GAME, Keyboard.KEY_R),
    CYCLE_FIRE_MODE("susy.key.cycle_fire_mode", KeyConflictContext.IN_GAME, Keyboard.KEY_B),
    AIM("susy.key.aim", KeyConflictContext.IN_GAME, -99);

    public static final SuSyKeyBind[] VALUES = values();

    public static void init() {
        if (FMLCommonHandler.instance().getSide().isClient()) return;
        SusyLog.logger.info("Registering KeyBinds");
    }

    @SideOnly(Side.CLIENT)
    private KeyBinding keybinding;

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
}
