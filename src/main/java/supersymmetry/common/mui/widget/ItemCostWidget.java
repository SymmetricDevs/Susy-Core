package supersymmetry.common.mui.widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;

import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.recipes.Recipe;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import supersymmetry.api.SusyLog;

public class ItemCostWidget extends Widget {

    @FunctionalInterface
    public static interface RecipeProvider {

        public Recipe get();
    }

    public static final int HEIGHT_OFFSET = 8;
    public static final int TEXT_COLOR = 0xf0f0f0;
    public RecipeProvider provider;
    public List<ItemStack> lastSyncedItems = new ArrayList<>();

    public ItemCostWidget(Size size, Position pos, RecipeProvider provider) {
        super(pos, size);
        this.provider = provider;
    }

    @Override
    public void detectAndSendChanges() {
        Recipe recipe = this.provider.get();
        if (recipe == null) {
            this.lastSyncedItems.clear();
            writeUpdateInfo(
                    100,
                    buf -> {
                        buf.writeInt(0);
                    });
            return;
        }
        List<ItemStack> items = new ArrayList<>();
        recipe.getInputs().stream()
                .forEach(
                        x -> {
                            for (ItemStack i : x.getInputStacks()) {
                                items.add(i);
                            }
                        });

        if (!items
                .stream() // this is one of the worst things ive wrote so far, but i forgot how to make a
                // tuple in this
                // horrible language, but the tostring of an itemstack returns pretty much what i need to
                // check for so yeah good
                // luck reading this, im sorry :c

                // .allMatch(x -> lastSyncedItems.contains(x))) {
                //
                .allMatch(
                        x -> lastSyncedItems.stream()
                                .map(y -> y.toString())
                                .collect(Collectors.toList())
                                .contains(x.toString()))) {

            this.writeUpdateInfo(
                    100,
                    buf -> {
                        buf.writeInt(items.size());
                        items.forEach(x -> buf.writeCompoundTag(x.writeToNBT(new NBTTagCompound())));
                    });
            this.lastSyncedItems = items;
        }
    }

    @Override
    public void drawInBackground(int mouseX, int mouseY, float partialTicks, IRenderContext context) {
        var pos = new Position(getPosition().x, getPosition().y);
        var size = getSize();
        if (lastSyncedItems.size() > 0) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            fontRenderer.drawString(
                    I18n.format("susy.machine.rocket_assembler.gui.required_items"),
                    pos.x,
                    pos.y - 4,
                    TEXT_COLOR);
        }
        for (ItemStack stack : lastSyncedItems) {
            // this part should render the item textures
            pos = pos.add(new Position(0, HEIGHT_OFFSET));
            GlStateManager.enableBlend();
            GlStateManager.enableDepth();
            GlStateManager.disableRescaleNormal();
            GlStateManager.disableLighting();
            RenderHelper.disableStandardItemLighting();
            RenderHelper.enableStandardItemLighting();
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.pushMatrix();
            RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();

            GlStateManager.scale(0.5, 0.5, 0.5); // hopefully will make the texture smaller?
            itemRender.renderItemAndEffectIntoGUI(stack, pos.x * 2, pos.y * 2 + 1);
            // itemRender.renderItemOverlayIntoGUI(
            // Minecraft.getMinecraft().fontRenderer, stack, itempos.x + 1, itempos.y + 1, null);
            GlStateManager.enableAlpha();

            GlStateManager.popMatrix();
            RenderHelper.disableStandardItemLighting();

            String item_name = I18n.format(stack.getTranslationKey() + ".name");
            if (stack.getItem() instanceof MetaItem metaitem) {
                item_name = I18n.format(metaitem.getTranslationKey(stack) + ".name");
            }
            GlStateManager.pushMatrix();
            GlStateManager.scale(.5, .5, .5);
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            fontRenderer.drawString(
                    String.format("%s, x%d", item_name, stack.getCount()),
                    (size.width + pos.x) * 2 - fontRenderer.getStringWidth(item_name),
                    pos.y * 2,
                    TEXT_COLOR);
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void readUpdateInfo(int id, PacketBuffer buffer) {
        super.readUpdateInfo(id, buffer);
        if (id == 100) {

            this.lastSyncedItems.clear();
            int c = buffer.readInt();
            for (int i = 0; i < c; i++) {
                try {
                    this.lastSyncedItems.add(new ItemStack(buffer.readCompoundTag()));
                } catch (IOException e) {
                    SusyLog.logger.error("failed to read an itemstack nbt, this shouldnt happen!");
                }
            }
        }
    }
}
