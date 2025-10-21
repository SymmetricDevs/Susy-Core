package supersymmetry.common.item.behavior;

import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import gregtech.api.gui.GuiTextures;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.*;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Flow;

import dev.tianmi.sussypatches.api.item.IMui2Factory;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.space.CelestialObjects;
import supersymmetry.api.space.Planetoid;

public class RocketConfigBehavior implements IItemBehaviour, IMui2Factory {

    private int pageNum = 0;

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        ItemStack stack = guiData.getUsedItemStack();
        NBTTagCompound tag = guiData.getUsedItemStack().getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            guiData.getUsedItemStack().setTagCompound(tag);
        }

        ModularPanel panel = ModularPanel.defaultPanel("rocket_config");
        // This panel needs to set up an unbounded list of missions.
        // Each mission can be one of three types.

        // Set up the sync values
        syncManager.syncValue("page_num", new IntSyncValue(() -> pageNum, v -> pageNum = v));
        syncManager.syncValue("mission_type", new EnumSyncValue<>(
                MissionType.class,
                () -> getMissionType(pageNum, stack),
                v -> setMissionType(pageNum, stack, v)));
        syncManager.syncValue("manned", new InteractionSyncHandler()
                .setOnMousePressed(b -> setMissionType(pageNum, stack, MissionType.Manned)));
        syncManager.syncValue("unmanned_cargo", new InteractionSyncHandler()
                .setOnMousePressed(b -> setMissionType(pageNum, stack, MissionType.UnmannedCargo)));
        syncManager.syncValue("unmanned_collection", new InteractionSyncHandler()
                .setOnMousePressed(b -> setMissionType(pageNum, stack, MissionType.UnmannedCollection)));

        syncManager.syncValue("dimension", new IntSyncValue(
                () -> getDimension(pageNum, stack),
                v -> setDimension(pageNum, stack, v)));


        Flow overallFlow = new Flow(GuiAxis.Y);
        panel.child(overallFlow);

        overallFlow.child(new Flow(GuiAxis.X)
                .child(new ToggleButton().syncHandler("mission_type").value(
                        new BoolValue(getMissionType(pageNum, stack) == MissionType.Manned)).syncHandler("manned"))
                .child(new ToggleButton().syncHandler("mission_type").value(
                                new BoolValue(getMissionType(pageNum, stack) == MissionType.UnmannedCargo))
                        .syncHandler("unmanned_cargo"))
                .child(new ToggleButton().syncHandler("mission_type").value(
                                new BoolValue(getMissionType(pageNum, stack) == MissionType.UnmannedCollection))
                        .syncHandler("unmanned_collection")));


        /*
         * syncManager.syncValue("config", i, new InteractionSyncHandler()
         * .setOnMousePressed(b -> {
         * ItemStack item = IntCircuitIngredient.getIntegratedCircuit(finalI);
         * item.setCount(guiData.getUsedItemStack().getCount());
         * circuitPreview.setItem(item);
         * if (Interactable.hasShiftDown()) panel.animateClose();
         * guiData.getPlayer().setHeldItem(guiData.getHand(), item);
         * }));
         */

        Flow planetoidsFlow = new Flow(GuiAxis.X);
        // TODO: research item
        Planetoid[] planetoids = {CelestialObjects.EARTH, CelestialObjects.MOON};
        for (Planetoid planetoid : planetoids) {
            planetoidsFlow.child(new ButtonWidget<>()
                    .size(18)
                    .background(SusyGuiTextures.SLOT,
                            new ItemDrawable(planetoid.getDisplayItem()).asIcon().size(16))
                    .disableHoverBackground()
                    .onMousePressed(b -> {
                        if (getDimension(pageNum, stack) == planetoid.getDimension()) {
                            return false;
                        }
                        setDimension(pageNum, stack, planetoid.getDimension());
                        return true;
                    }));
        }



        overallFlow.child(planetoidsFlow);

        return panel;
    }

    private MissionType getMissionType(int page, ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagCompound pageTag = tag.getCompoundTag("page_" + page);
        if (pageTag.isEmpty()) {
            tag.setTag("page_" + page, pageTag);
        }
        if (!pageTag.hasKey("mission_type")) {
            pageTag.setInteger("mission_type", 0);
        }
        return MissionType.values()[tag.getInteger("mission_type")];
    }

    private void setMissionType(int page, ItemStack stack, MissionType type) {
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagCompound pageTag = tag.getCompoundTag("page_" + page);
        pageTag.setInteger("mission_type", type.ordinal());
    }

    private int getDimension(int page, ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagCompound pageTag = tag.getCompoundTag("page_" + page);
        // We now assume that the page tag has been set.
        if (!pageTag.hasKey("dimension")) {
            pageTag.setInteger("dimension", 0);
        }
        return pageTag.getInteger("dimension");
    }

    private void setDimension(int page, ItemStack stack, int dimension) {
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagCompound pageTag = tag.getCompoundTag("page_" + page);
        pageTag.setInteger("dimension", dimension);
    }

    private enum MissionType {
        Manned,
        UnmannedCargo,
        UnmannedCollection
    }
}
