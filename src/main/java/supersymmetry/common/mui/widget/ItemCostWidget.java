package supersymmetry.common.mui.widget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
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
import net.minecraft.util.math.MathHelper;

import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.IRenderContext;
import gregtech.api.gui.Widget;
import gregtech.api.items.metaitem.MetaItem;
import gregtech.api.recipes.Recipe;
import gregtech.api.util.Position;
import gregtech.api.util.Size;
import gregtech.client.utils.RenderUtil;
import supersymmetry.api.SusyLog;

// some parts copied from ScrollableListWidget
public class ItemCostWidget extends Widget {

    @FunctionalInterface
    public static interface RecipeProvider {

        public Recipe get();
    }

    public static final int scrollPaneWidth = 10;
    public static final double HEIGHT_SCALE = 0.8;
    public static final int HEIGHT_OFFSET = (int) (16 * HEIGHT_SCALE);
    public static final int TEXT_COLOR = 0xf0f0f0;
    public int totalListHeight;
    public int mouseWheelMoveStep = 6;
    public int scrollOffset;

    public int lastMouseX;

    public int lastMouseY;
    public boolean draggedOnScrollBar;
    public RecipeProvider provider;
    public List<ItemStack> lastSyncedItems = new ArrayList<>();
    // shouldnt be rendered when the recipe is in progress
    public BooleanSupplier shouldRender;

    public ItemCostWidget(
                          Size size, Position pos, RecipeProvider provider, BooleanSupplier shouldRender) {
        super(pos, size);
        this.provider = provider;
        this.shouldRender = shouldRender;
    }

    // the limit for how much you can scroll
    public int getListHeight() {
        return HEIGHT_OFFSET * this.lastSyncedItems.size();
    }

    @Override
    public boolean mouseWheelMove(int mouseX, int mouseY, int wheelDelta) {
        if (isMouseOverElement(mouseX, mouseY)) {
            int direction = -MathHelper.clamp(wheelDelta, -1, 1);
            int moveDelta = direction * (mouseWheelMoveStep / 2);
            addScrollOffset(moveDelta);
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        if (isOnScrollPane(mouseX, mouseY)) {
            this.draggedOnScrollBar = true;
        }
        if (isPositionInsideScissor(mouseX, mouseY)) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        return false;
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        int mouseDelta = (mouseY - lastMouseY);
        this.lastMouseX = mouseX;
        this.lastMouseY = mouseY;
        if (draggedOnScrollBar) {
            addScrollOffset(mouseDelta);
            return true;
        }
        if (isPositionInsideScissor(mouseX, mouseY)) {
            return super.mouseDragged(mouseX, mouseY, button, timeDragged);
        }
        return false;
    }

    @Override
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        this.draggedOnScrollBar = false;
        if (isPositionInsideScissor(mouseX, mouseY)) {
            return super.mouseReleased(mouseX, mouseY, button);
        }
        return false;
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

        if (!items.stream()
                // this is one of the worst things ive wrote so far, but i forgot how to make a tuple in
                // this
                // horrible language, but the tostring of an itemstack returns pretty much what i need
                // to
                // check for so yeah good
                // luck reading this, im sorry :c

                // .allMatch(x -> lastSyncedItems.contains(x))) {
                //
                .allMatch(
                        x -> lastSyncedItems.stream()
                                .map(y -> y.toString())
                                .collect(Collectors.toList())
                                .contains(x.toString())) ||
                (items.size() == 0 && lastSyncedItems.size() == 0)) {
            SusyLog.logger.info("update sent because {}!={}", items, lastSyncedItems);

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
        if (this.shouldRender.getAsBoolean()) return;
        Position pos = new Position(getPosition().x, getPosition().y);
        // TODO: move this out of the widget and just use a text widget
        if (lastSyncedItems.size() > 0) {
            FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
            fontRenderer.drawString(
                    I18n.format("susy.machine.rocket_assembler.gui.required_items"),
                    pos.x,
                    pos.y - 8,
                    TEXT_COLOR);
        }
        if (!isPositionInsideScissor(mouseX, mouseY)) {
            mouseX = Integer.MAX_VALUE;
            mouseY = Integer.MAX_VALUE;
        }

        Position position = getPosition();
        Size size = getSize();
        int paneSize = scrollPaneWidth;
        int scrollX = position.x + size.width - paneSize;
        if (this.lastSyncedItems.size() > this.getSize().height / HEIGHT_OFFSET) {
            GuiTextures.SLIDER_BACKGROUND_VERTICAL.draw(
                    scrollX + 1, position.y + 1, paneSize - 2, size.height - 2);

            int maxScrollOffset = getSize().height - this.getListHeight() - 1;
            float scrollPercent = maxScrollOffset == 0 ? 0 : scrollOffset / (maxScrollOffset * 1.0f);
            int scrollSliderHeight = 14;
            int scrollSliderY = Math.round(position.y + (size.height - scrollSliderHeight) * scrollPercent);
            GuiTextures.SLIDER_ICON.draw(
                    scrollX + 1, scrollSliderY + 2, paneSize - 2, scrollSliderHeight);
        }
        RenderUtil.useScissor(
                position.x,
                position.y,
                size.width - paneSize,
                size.height,
                () -> {
                    // int min =
                    // MathHelper.clamp((scrollOffset / HEIGHT_OFFSET) - 1, 0,
                    // this.lastSyncedItems.size());
                    // int max =
                    // MathHelper.clamp(
                    // ((scrollOffset + this.getSize().height) / HEIGHT_OFFSET) + 1,
                    // 0,
                    // this.lastSyncedItems.size());
                    // for (int i = min; i < max; i++) {
                    // this.drawStack(
                    // new Position(
                    // this.getPosition().x,
                    // this.getPosition().y + HEIGHT_OFFSET * i + /* small offset from the text */
                    // 10),
                    // this.getSize(),
                    // this.lastSyncedItems.get(i));
                    // }
                    Position startpos = new Position(this.getPosition().x, this.getPosition().y + scrollOffset - 8);
                    for (ItemStack itemStack : lastSyncedItems) {
                        startpos = startpos.add(new Position(0, ItemCostWidget.HEIGHT_OFFSET));
                        this.drawStack(startpos, size, itemStack);
                    }
                });
    }

    public void drawStack(Position pos, Size size, ItemStack stack) {
        // pos = pos.add(new Position(0, HEIGHT_OFFSET));
        GlStateManager.enableBlend();
        GlStateManager.enableDepth();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableLighting();
        RenderHelper.disableStandardItemLighting();
        RenderHelper.enableStandardItemLighting();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.pushMatrix();
        RenderItem itemRender = Minecraft.getMinecraft().getRenderItem();

        GlStateManager.scale(HEIGHT_SCALE, HEIGHT_SCALE, HEIGHT_SCALE);
        itemRender.renderItemAndEffectIntoGUI(
                stack, (int) (pos.x / HEIGHT_SCALE), (int) (pos.y / HEIGHT_SCALE) - 4);
        GlStateManager.enableAlpha();

        GlStateManager.popMatrix();
        RenderHelper.disableStandardItemLighting();

        String item_name = I18n.format(stack.getTranslationKey() + ".name");
        if (stack.getItem() instanceof MetaItem metaitem) {
            item_name = I18n.format(metaitem.getTranslationKey(stack));
        }
        GlStateManager.pushMatrix();
        GlStateManager.scale(HEIGHT_SCALE, HEIGHT_SCALE, HEIGHT_SCALE);
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        fontRenderer.drawString(
                String.format("%s, x%d", item_name, stack.getCount()),
                (int) ((pos.x + HEIGHT_OFFSET + 6 / HEIGHT_SCALE) / HEIGHT_SCALE),
                (int) (pos.y / HEIGHT_SCALE),
                TEXT_COLOR);
        GlStateManager.popMatrix();
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
            // SusyLog.logger.info(
            // "this.lastSyncedItems.size(){} >{} this.getSize().height{} / HEIGHT_OFFSET{}",
            // this.lastSyncedItems.size(),
            // this.lastSyncedItems.size() > this.getSize().height / HEIGHT_OFFSET,
            // this.getSize().height,
            // ItemCostWidget.HEIGHT_OFFSET);
        }
    }

    private void addScrollOffset(int offset) {
        if (this.shouldRender.getAsBoolean()) {
            this.scrollOffset = MathHelper.clamp(
                    scrollOffset + offset, -(this.getListHeight() - this.getSize().height), 0);
        }
    }

    private boolean isPositionInsideScissor(int mouseX, int mouseY) {
        return isMouseOverElement(mouseX, mouseY) && !isOnScrollPane(mouseX, mouseY);
    }

    private boolean isOnScrollPane(int mouseX, int mouseY) {
        Position pos = getPosition();
        Size size = getSize();
        return isMouseOver(
                pos.x + size.width - scrollPaneWidth, pos.y, scrollPaneWidth, size.height, mouseX, mouseY);
    }
}
