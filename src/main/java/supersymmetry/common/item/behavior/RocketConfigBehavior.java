package supersymmetry.common.item.behavior;

import static net.minecraft.util.EnumFacing.Axis.*;
import static supersymmetry.api.gui.SusyGuiTextures.ICON_LEFT;
import static supersymmetry.api.gui.SusyGuiTextures.ICON_RIGHT;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import com.cleanroommc.modularui.api.GuiAxis;
import com.cleanroommc.modularui.api.drawable.IKey;
import com.cleanroommc.modularui.api.value.sync.IIntSyncValue;
import com.cleanroommc.modularui.drawable.ItemDrawable;
import com.cleanroommc.modularui.factory.PlayerInventoryGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.screen.UISettings;
import com.cleanroommc.modularui.utils.Alignment;
import com.cleanroommc.modularui.value.BoolValue;
import com.cleanroommc.modularui.value.sync.*;
import com.cleanroommc.modularui.widgets.ButtonWidget;
import com.cleanroommc.modularui.widgets.ToggleButton;
import com.cleanroommc.modularui.widgets.layout.Column;
import com.cleanroommc.modularui.widgets.layout.Flow;
import com.cleanroommc.modularui.widgets.layout.Row;
import com.cleanroommc.modularui.widgets.textfield.TextFieldWidget;

import dev.tianmi.sussypatches.api.item.IMui2Factory;
import dev.tianmi.sussypatches.api.mui2.factory.MetaItemGuiFactory;
import gregtech.api.gui.ModularUI;
import gregtech.api.items.gui.ItemUIFactory;
import gregtech.api.items.gui.PlayerInventoryHolder;
import gregtech.api.items.metaitem.stats.IItemBehaviour;
import supersymmetry.api.space.CelestialObjects;
import supersymmetry.api.space.Planetoid;
import supersymmetry.common.rocketry.RocketConfiguration;
import supersymmetry.common.rocketry.RocketConfiguration.MissionType;

public class RocketConfigBehavior implements IItemBehaviour, IMui2Factory, ItemUIFactory {

    private int pageNum = 0;
    public static final int MAX_PAGES = 10;

    @Override
    public ModularPanel buildUI(PlayerInventoryGuiData guiData, PanelSyncManager syncManager, UISettings settings) {
        pageNum = 0;
        ItemStack stack = guiData.getUsedItemStack();
        NBTTagCompound tag = guiData.getUsedItemStack().getTagCompound();
        if (tag == null) {
            tag = new NBTTagCompound();
            guiData.getUsedItemStack().setTagCompound(tag);
        }

        ModularPanel panel = ModularPanel.defaultPanel("rocket_config", 221, 181);
        // This panel needs to set up an unbounded list of missions.
        // Each mission can be one of three types.

        // Set up the sync values
        syncManager.syncValue("page_num", new IntSyncValue(() -> pageNum, v -> pageNum = v));

        EnumSyncValue missionType = new EnumSyncValue<>(
                RocketConfiguration.MissionType.class,
                () -> getMissionType(pageNum, stack),
                v -> setMissionType(pageNum, stack, v));
        syncManager.syncValue("mission_type", missionType);

        IntSyncValue dimension = new IntSyncValue(
                () -> getDimension(pageNum, stack),
                v -> setDimension(pageNum, stack, v));
        syncManager.syncValue("dimension", dimension);

        EnumSyncValue destinationType = new EnumSyncValue<>(
                RocketConfiguration.DestinationType.class,
                () -> getDestinationType(pageNum, stack),
                v -> setDestinationType(pageNum, stack, v));
        syncManager.syncValue("destination_type", destinationType);

        panel.child(new Row().top(10).horizontalCenter().coverChildren()
                .child(new ButtonWidget<>().size(12).onMousePressed((w) -> {
                    pageNum--;
                    return true;
                }).setEnabledIf((w) -> pageNum > 0).overlay(ICON_LEFT))
                .child(IKey.lang("susy.gui.page", () -> new Object[] { pageNum + 1, MAX_PAGES }).asWidget())
                .child(new ButtonWidget<>().size(12).onMousePressed((w) -> {
                    pageNum++;
                    return true;
                })).setEnabledIf((w) -> pageNum <= MAX_PAGES - 1).overlay(ICON_RIGHT));
        Flow overallFlow = new Flow(GuiAxis.Y);

        Flow rowFlow = new Column().coverChildren().padding(10, 10, 30, 10)
                .crossAxisAlignment(Alignment.CrossAxis.START)
                .childPadding(5);
        panel.child(rowFlow);

        rowFlow.child(IKey.lang("susy.gui.rocket_programmer.mission_type").asWidget());
        rowFlow.child(new Row().coverChildren()
                .child(new ToggleButton().value(select(missionType, RocketConfiguration.MissionType.Manned))
                        .tooltip((tooltip) -> tooltip.addLine(I18n.format("susy.gui.rocket_programmer.manned"))))

                .child(new ToggleButton().value(select(missionType, MissionType.UnmannedCargo))
                        .tooltip(
                                (tooltip -> tooltip.addLine(I18n.format("susy.gui.rocket_programmer.unmanned_cargo")))))
                .child(new ToggleButton().value(select(missionType, MissionType.UnmannedCollection))
                        .tooltip((tooltip -> tooltip
                                .addLine(I18n.format("susy.gui.rocket_programmer.unmanned_collection"))))));

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

        Flow planetoidsFlow = new Row().coverChildren();
        rowFlow.child(IKey.lang("susy.gui.rocket_programmer.planetoid").asWidget());
        // TODO: research item
        Planetoid[] planetoids = { CelestialObjects.EARTH, CelestialObjects.MOON };
        for (Planetoid planetoid : planetoids) {
            planetoidsFlow.child(new ToggleButton()
                    .size(18)
                    .overlay(new ItemDrawable(planetoid.getDisplayItem()).asIcon().size(16))
                    .overlay(true,
                            new ItemDrawable(planetoid.getDisplayItem()).asIcon().size(16))
                    .value(select(dimension, planetoid.getDimension()))
                    .tooltip((tooltip) -> tooltip.addLine(I18n.format(planetoid.getTranslationKey()))));
        }
        rowFlow.child(planetoidsFlow);
        // Select destination type
        rowFlow.child(IKey.lang("susy.gui.rocket_programmer.destination_type").asWidget());
        Flow destinationTypeFlow = new Row().coverChildren()
                .child(new ToggleButton()
                        .size(18)
                        .value(select(destinationType, RocketConfiguration.DestinationType.Landing))
                        .tooltip((tooltip) -> tooltip.addLine(I18n.format("susy.gui.rocket_programmer.landing"))))
                .child(new ToggleButton()
                        .size(18)
                        .value(select(destinationType, RocketConfiguration.DestinationType.Orbit))
                        .tooltip((tooltip) -> tooltip.addLine(I18n.format("susy.gui.rocket_programmer.orbit"))));
        rowFlow.child(destinationTypeFlow);

        // Register landing coordinates with text fields
        rowFlow.child(IKey.lang("susy.gui.rocket_programmer.landing_coordinates").asWidget()
                .setEnabledIf(
                        (w) -> destinationType.getIntValue() == RocketConfiguration.DestinationType.Landing.ordinal()));
        Flow landingFlow = new Row().coverChildren()
                .setEnabledIf(
                        (w) -> destinationType.getIntValue() == RocketConfiguration.DestinationType.Landing.ordinal());
        for (EnumFacing.Axis axis : EnumFacing.Axis.values()) {
            IntSyncValue coord = new IntSyncValue(
                    () -> getLandingCoord(pageNum, stack, axis),
                    v -> setLandingCoord(pageNum, stack, axis, v));
            landingFlow.child(new TextFieldWidget().height(16).setNumbers().value(coord));
        }
        rowFlow.child(landingFlow);
        overallFlow.child(planetoidsFlow);

        return panel;
    }

    private BoolValue.Dynamic select(IIntSyncValue v, int selected) {
        return new BoolValue.Dynamic(() -> v.getIntValue() == selected, (b) -> v.setIntValue(selected));
    }

    private BoolValue.Dynamic select(IIntSyncValue v, Enum selected) {
        return select(v, selected.ordinal());
    }

    private RocketConfiguration.MissionType getMissionType(int page, ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagCompound pageTag = tag.getCompoundTag("page_" + page);
        if (pageTag.isEmpty()) {
            tag.setTag("page_" + page, pageTag);
        }
        if (!pageTag.hasKey("mission_type")) {
            pageTag.setInteger("mission_type", 0);
        }
        return RocketConfiguration.MissionType.values()[pageTag.getInteger("mission_type")];
    }

    private void setMissionType(int page, ItemStack stack, RocketConfiguration.MissionType type) {
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

    private RocketConfiguration.DestinationType getDestinationType(int page, ItemStack stack) {
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagCompound pageTag = tag.getCompoundTag("page_" + page);
        // We now assume that the page tag has been set.
        if (!pageTag.hasKey("destination_type")) {
            pageTag.setInteger("destination_type", 0);
        }
        return RocketConfiguration.DestinationType.values()[pageTag.getInteger("destination_type")];
    }

    private void setDestinationType(int page, ItemStack stack, RocketConfiguration.DestinationType type) {
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagCompound pageTag = tag.getCompoundTag("page_" + page);
        pageTag.setInteger("destination_type", type.ordinal());
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

    private int getLandingCoord(int page, ItemStack stack, EnumFacing.Axis axis) {
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagCompound pageTag = tag.getCompoundTag("page_" + page);
        if (!pageTag.hasKey("landing_" + axis)) {
            pageTag.setInteger("landing_" + axis, 0);
        }
        return pageTag.getInteger("landing_" + axis);
    }

    private void setLandingCoord(int page, ItemStack stack, EnumFacing.Axis axis, int x) {
        NBTTagCompound tag = stack.getTagCompound();
        NBTTagCompound pageTag = tag.getCompoundTag("landing_" + axis + page);
        pageTag.setInteger("landing_" + axis, x);
    }
}
