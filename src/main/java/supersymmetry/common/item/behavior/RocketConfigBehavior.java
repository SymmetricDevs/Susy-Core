package supersymmetry.common.item.behavior;

import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.IIntValue;
import com.cleanroommc.modularui.api.value.sync.IIntSyncValue;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.screen.ModularScreen;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.TextWidget;
import com.cleanroommc.modularui.widgets.layout.Row;
import dev.tianmi.sussypatches.api.mui2.factory.MetaItemGuiFactory;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.recipes.ingredients.IntCircuitIngredient;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
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
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;
import supersymmetry.api.gui.SusyGuiTextures;
import supersymmetry.api.metatileentity.Mui2Utils;
import supersymmetry.api.space.CelestialObjects;
import supersymmetry.api.space.Planetoid;

import static supersymmetry.api.gui.SusyGuiTextures.ICON_LEFT;
import static supersymmetry.api.gui.SusyGuiTextures.ICON_RIGHT;

public class RocketConfigBehavior implements IItemBehaviour, IMui2Factory, ItemUIFactory {

    private int pageNum = 0;
    public static final int MAX_PAGES = 10;

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

        EnumSyncValue missionType = new EnumSyncValue<>(
                MissionType.class,
                () -> getMissionType(pageNum, stack),
                v -> setMissionType(pageNum, stack, v));
        syncManager.syncValue("mission_type", missionType);


        IntSyncValue dimension = new IntSyncValue(
                () -> getDimension(pageNum, stack),
                v -> setDimension(pageNum, stack, v));
        syncManager.syncValue("dimension", dimension);

        panel.child(new Row().top(10).horizontalCenter().coverChildren()
                .child(new ButtonWidget<>().size(12).onMousePressed((w) -> {
                    pageNum--;
                    return true;
                }).setEnabledIf((w) -> pageNum > 0).overlay(ICON_LEFT))
                .child(IKey.str("susy.gui.page", pageNum + 1).asWidget())
                .child(new ButtonWidget<>().size(12).onMousePressed((w) -> {
                    pageNum++;
                    return true;
                })).setEnabledIf((w) -> pageNum < MAX_PAGES - 1).overlay(ICON_RIGHT));

        panel.child(new Row().top(30).left(10).coverChildren()
                .child(new ToggleButton().value(select(missionType, MissionType.Manned))
                        .tooltip((tooltip) -> tooltip.addLine(I18n.format("susy.gui.rocket_programmer.manned"))))
                .child(new ToggleButton().value(select(missionType, MissionType.UnmannedCargo))
                        .tooltip((tooltip -> tooltip.addLine(I18n.format("susy.gui.rocket_programmer.unmanned_cargo")))))
                .child(new ToggleButton().value(select(missionType, MissionType.UnmannedCollection))
                        .tooltip((tooltip -> tooltip.addLine(I18n.format("susy.gui.rocket_programmer.unmanned_collection"))))));

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

        Flow planetoidsFlow = new Row().top(60).left(10).coverChildren();
        // TODO: research item
        Planetoid[] planetoids = {CelestialObjects.EARTH, CelestialObjects.MOON};
        for (Planetoid planetoid : planetoids) {
            planetoidsFlow.child(new ToggleButton()
                    .size(18)
                    .overlay(new ItemDrawable(planetoid.getDisplayItem()).asIcon().size(16))
                    .overlay(true,
                            new ItemDrawable(planetoid.getDisplayItem()).asIcon().size(16))
                    .value(select(dimension, planetoid.getDimension()))
                    .tooltip((tooltip) -> tooltip.addLine(I18n.format(planetoid.getTranslationKey()))));
        }
        // Select between either orbit or specific landing coords


        panel.child(planetoidsFlow);
        return panel;
    }

    private BoolValue.Dynamic select(IIntSyncValue v, int selected) {
        return new BoolValue.Dynamic(() -> v.getIntValue() == selected, (b) -> v.setIntValue(selected));
    }

    private BoolValue.Dynamic select(IIntSyncValue v, Enum selected) {
        return select(v, selected.ordinal());
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
        return MissionType.values()[pageTag.getInteger("mission_type")];
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

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand) {
        if (!world.isRemote) {
            MetaItemGuiFactory.openFromHand(player, hand);
        }
        return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
    }

    @Override
    public ModularUI createUI(PlayerInventoryHolder holder, EntityPlayer entityPlayer) {
        return null;
    }

    private enum MissionType {
        Manned,
        UnmannedCargo,
        UnmannedCollection
    }

    private enum TravelType {
        Orbit,
        Landing
    }
}
