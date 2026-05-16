package supersymmetry.common.mui.widget;

import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.SlotItemHandler;

import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Mouse;

import com.google.common.collect.Lists;

import gregtech.api.gui.ingredient.IGhostIngredientTarget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.client.utils.TooltipHelper;
import mezz.jei.api.gui.IGhostIngredientHandler.Target;

public class ComponentCardSlotWidget extends SlotWidget implements IGhostIngredientTarget {

    private static final Field ITEM_HANDLER_FIELD;

    static {
        Field field = null;
        try {
            field = SlotItemHandler.class.getDeclaredField("itemHandler");
            field.setAccessible(true);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
        ITEM_HANDLER_FIELD = field;
    }

    private boolean clearSlotOnRightClick;

    public ComponentCardSlotWidget(
                                   IItemHandlerModifiable itemHandler, int slotIndex, int xPosition, int yPosition) {
        super(itemHandler, slotIndex, xPosition, yPosition, false, false);
    }

    public ComponentCardSlotWidget setClearSlotOnRightClick(boolean clearSlotOnRightClick) {
        this.clearSlotOnRightClick = clearSlotOnRightClick;
        return this;
    }

    private IItemHandlerModifiable getHandler() {
        try {
            return (IItemHandlerModifiable) ITEM_HANDLER_FIELD.get(slotReference);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
            // ¯\_(ツ)_/¯
        }
    }

    private void syncSlot() {
        IItemHandlerModifiable handler = getHandler();
        slotReference.putStack(handler.getStackInSlot(0));
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            if (button == 1 && clearSlotOnRightClick && !slotReference.getStack().isEmpty()) {
                IItemHandlerModifiable handler = getHandler();
                handler.setStackInSlot(0, ItemStack.EMPTY);
                syncSlot();
                writeClientAction(2, buf -> {});
            } else {
                gui.getModularUIGui().superMouseClicked(mouseX, mouseY, button);
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(int mouseX, int mouseY, int button, long timeDragged) {
        if (isMouseOverElement(mouseX, mouseY) && gui != null) {
            ItemStack is = gui.entityPlayer.inventory.getItemStack().copy();
            is.setCount(1);
            IItemHandlerModifiable handler = getHandler();
            handler.insertItem(0, is, false);
            syncSlot();
            writeClientAction(
                    1,
                    buffer -> {
                        buffer.writeItemStack(slotReference.getStack());
                        int mouseButton = Mouse.getEventButton();
                        boolean shiftDown = TooltipHelper.isShiftDown();
                        buffer.writeVarInt(mouseButton);
                        buffer.writeBoolean(shiftDown);
                    });
            return true;
        }
        return false;
    }

    @Override
    public ItemStack slotClick(int dragType, ClickType clickTypeIn, EntityPlayer player) {
        ItemStack stackHeld = player.inventory.getItemStack();
        return this.slotClickPhantom(dragType, clickTypeIn, stackHeld);
    }

    @Override
    public boolean canMergeSlot(ItemStack stack) {
        return false;
    }

    private ItemStack slotClickPhantom(int mouseButton, ClickType clickTypeIn, ItemStack stackHeld) {
        IItemHandlerModifiable handler = getHandler();
        // the positions are desynced but i stopped caring about them
        // SusyLog.logger.info("slotClickPhantom ({}, {}, {}) xy {}:{}", mouseButton, clickTypeIn,
        // stackHeld,this.getPosition().x,this.getPosition().y);

        if (mouseButton == 0 || mouseButton == 1) {
            if (stackHeld.isEmpty()) {
                handler.setStackInSlot(0, ItemStack.EMPTY);
            } else {
                if (!(handler.insertItem(0, stackHeld, true) == stackHeld)) {
                    handler.insertItem(0, stackHeld, false);
                }
            }
        } else if (mouseButton == 2) {
            handler.setStackInSlot(0, ItemStack.EMPTY);
        }

        syncSlot();
        return stackHeld;
    }

    @Override
    public List<Target<?>> getPhantomTargets(Object ingredient) {
        if (!(ingredient instanceof ItemStack)) {
            return Collections.emptyList();
        }
        Rectangle rectangle = toRectangleBox();
        return Lists.newArrayList(
                new Target<Object>() {

                    @NotNull
                    @Override
                    public Rectangle getArea() {
                        return rectangle;
                    }

                    @Override
                    public void accept(@NotNull Object ingredient) {
                        if (ingredient instanceof ItemStack) {
                            int mouseButton = Mouse.getEventButton();
                            boolean shiftDown = TooltipHelper.isShiftDown();
                            ClickType clickType = shiftDown ? ClickType.QUICK_MOVE : ClickType.PICKUP;
                            slotClickPhantom(mouseButton, clickType, (ItemStack) ingredient);
                            writeClientAction(
                                    1,
                                    buffer -> {
                                        buffer.writeItemStack((ItemStack) ingredient);
                                        buffer.writeVarInt(mouseButton);
                                        buffer.writeBoolean(shiftDown);
                                    });
                        }
                    }
                });
    }

    @Override
    public void handleClientAction(int id, PacketBuffer buffer) {
        if (id == 1) {
            ItemStack stackHeld;
            try {
                stackHeld = buffer.readItemStack();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            int mouseButton = buffer.readVarInt();
            boolean shiftKeyDown = buffer.readBoolean();
            ClickType clickType = shiftKeyDown ? ClickType.QUICK_MOVE : ClickType.PICKUP;
            slotClickPhantom(mouseButton, clickType, stackHeld);
        } else if (id == 2) {
            IItemHandlerModifiable handler = getHandler();
            handler.setStackInSlot(0, ItemStack.EMPTY);
            syncSlot();
        }
    }
}
