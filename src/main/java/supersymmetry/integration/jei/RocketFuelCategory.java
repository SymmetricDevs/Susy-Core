package supersymmetry.integration.jei;

import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.screen.viewport.ModularGuiContext;
import com.cleanroommc.modularui.utils.MouseData;
import com.cleanroommc.modularui.value.sync.FluidSlotSyncHandler;
import com.cleanroommc.modularui.widget.Widget;
import com.cleanroommc.modularui.widget.WidgetTree;
import com.cleanroommc.modularui.widgets.slot.FluidSlot;
import gregtech.integration.jei.basic.BasicRecipeCategory;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.ingredients.VanillaTypes;
import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.input.Mouse;
import supersymmetry.Supersymmetry;
import supersymmetry.api.gui.SusyGuiTextures;

import java.lang.reflect.Field;
import java.util.List;

public class RocketFuelCategory extends BasicRecipeCategory<RocketFuelWrapper, RocketFuelWrapper> {
    public static final String UID = Supersymmetry.MODID + ":rocket_fuel";

    private ModularPanel modularUI;
    private ModularGuiContext context;
    private final FluidStack[] slots = new FluidStack[3];
    public RocketFuelCategory(IGuiHelper guiHelper) {
        super("rocket_fuel", "rocket_fuel.name", guiHelper.createBlankDrawable(176, 90), guiHelper);

        this.modularUI = ModularPanel.defaultPanel("rocket_fuel_ui").invisible().child(new FluidSlot().syncHandler(
                new JEIFluidSyncHandler(0).canFillSlot(false).canDrainSlot(false)
        ).overlayTexture(SusyGuiTextures.FLUID_SLOT));
    }
    @Override
    public void drawExtras(Minecraft minecraft) {
        if (context == null) {
            context = new ModularScreen(modularUI) {
                @Override
                public boolean isClientOnly() {
                    return true;
                }
            }.getContext();
            context.updateScreenArea(minecraft.displayWidth, minecraft.displayHeight);
            try {
                Field contextField = Widget.class.getDeclaredField("context");
                contextField.setAccessible(true);
                contextField.set(modularUI, this.context);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            context.setSettings(new UISettings());
            modularUI.onOpen(context.getScreen());
            WidgetTree.resizeInternal(modularUI, true);
        }
        final ScaledResolution scaledresolution = new ScaledResolution(minecraft);
        int i1 = scaledresolution.getScaledWidth();
        int j1 = scaledresolution.getScaledHeight();
        final int mouseX = Mouse.getX() * i1 / minecraft.displayWidth;
        final int mouseY = j1 - Mouse.getY() * j1 / minecraft.displayHeight - 1;
        context.updateState(mouseX, mouseY, minecraft.getTickLength());
        context.updateEventState();
        context.updateScreenArea(minecraft.displayWidth, minecraft.displayHeight);
        context.getScreen().onUpdate();
        context.getScreen().onFrameUpdate();
        WidgetTree.drawTree(modularUI, context);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, RocketFuelWrapper recipeWrapper, IIngredients ingredients) {
        List<List<FluidStack>> copyFrom = ingredients.getInputs(VanillaTypes.FLUID);
        for (int i = 0; i < slots.length && i < copyFrom.size(); i++) {
            slots[i] = copyFrom.get(i).get(0);
        }
    }

    @Override
    public IRecipeWrapper getRecipeWrapper(RocketFuelWrapper recipe) {
        return recipe;
    }

    @Override
    public @NotNull String getModName() {
        return Supersymmetry.MODID;
    }

    public class JEIFluidSyncHandler extends FluidSlotSyncHandler {
        private int i;
        public JEIFluidSyncHandler(int i) {
            super(null);
            this.i = i;
        }

        @Override
        public @Nullable FluidStack getValue() {
            return slots[i];
        }

        @Override
        public void tryClickPhantom(MouseData mouseData) {

        }

        @Override
        public void tryScrollPhantom(MouseData mouseData) {

        }

        @Override
        public void readOnClient(int id, PacketBuffer buf) {

        }

        @Override
        public void readOnServer(int id, PacketBuffer buf) {

        }
    }
}
