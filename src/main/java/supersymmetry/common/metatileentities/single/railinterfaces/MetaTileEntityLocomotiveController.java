package supersymmetry.common.metatileentities.single.railinterfaces;

import cam72cam.immersiverailroading.entity.EntityRollingStock;
import cam72cam.immersiverailroading.entity.Locomotive;
import com.cleanroommc.modularui.factory.PosGuiData;
import com.cleanroommc.modularui.screen.ModularPanel;
import com.cleanroommc.modularui.value.sync.PanelSyncManager;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.CycleButtonWidget;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.gui.widgets.VerticalSliderWidget;
import supersymmetry.api.stockinteraction.StockHelperFunctions;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityLocomotiveController extends MetaTileEntityStockInteractor
{
    AxisAlignedBB interactionBoundingBox;
    public int ticksAlive;

    public boolean active;
    //#fix# may need to filter types of locomotives later on
    public static final int filterIndex = 1;

    //control settings
    public float activeBreak;
    public float activeThrottle;
    public float inactiveBreak;
    public float inactiveThrottle;

    public MetaTileEntityLocomotiveController(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SusyTextures.STOCK_CONTROLLER);
    }

    @Override
    public ModularPanel buildUI(PosGuiData guiData, PanelSyncManager syncManager) {
        return null;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLocomotiveController(this.metaTileEntityId);
    }

    public boolean isOpaqueCube() {
        return true;
    }

    //#fix# pickaxe not it maybe
    public String getHarvestTool() {
        return "wrench";
    }

    public boolean hasFrontFacing() {
        return true;
    }

    public void writeStatsToBuffer(PacketBuffer buf) {

        buf.writeFloat(this.activeBreak);
        buf.writeFloat(this.activeThrottle);
        buf.writeFloat(this.inactiveBreak);
        buf.writeFloat(this.inactiveThrottle);
    }

    public void readStatsFromBuffer(PacketBuffer buf) {
        this.activeBreak = buf.readFloat();
        this.activeThrottle = buf.readFloat();
        this.inactiveBreak = buf.readFloat();
        this.inactiveThrottle = buf.readFloat();
    }


    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.active);
        this.writeStatsToBuffer(buf);
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.active = buf.readBoolean();
        this.readStatsFromBuffer(buf);

        this.scheduleRenderUpdate();
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if((dataId & 0b1) > 0) {
            this.active = buf.readBoolean();
        }
        if((dataId & 0b10) > 0) {
            this.readStatsFromBuffer(buf);
        }
    }

    public void update() {
        super.update();

        if(this.getWorld().isRemote)
            return;

        if(this.ticksAlive % 20 == 0)
        {
            this.onNeighborChanged();

            List<EntityRollingStock> stocks = StockHelperFunctions.getStocksInArea(this.getWorld(), this.getInteractionBoundingBox());

            if(!(stocks.size() > 0))
                return;

            if(!(stocks.get(0) instanceof Locomotive loco))
                return;

            this.active = this.isBlockRedstonePowered();
            this.writeCustomData(0b1, buf -> buf.writeBoolean(this.active));

            if(this.active && this.activeBreak >= 0) {
                loco.setAirBrake(this.activeBreak);
                loco.setThrottle(this.activeThrottle);
            } else if(!this.active && this.inactiveBreak >= 0) {
                loco.setAirBrake(this.inactiveBreak);
                loco.setThrottle(this.inactiveThrottle);
            }
        }

        this.ticksAlive++;
    }

    public void onNeighborChanged() {
        if(this.getWorld().isRemote)
            return;
        this.updateInputRedstoneSignals();
        this.active = this.isBlockRedstonePowered();
        this.writeCustomData(0b1, (buf) -> buf.writeBoolean(this.active));
    }

    public boolean needsSneakToRotate() {
        return true;
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("susy.stock_interfaces.locomotive_controller.description"));
        tooltip.add(I18n.format("susy.stock_interfaces.right_click_for_gui"));
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {

        int w = 192;
        int h = 256;
        FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
        int fontHeight = fontRenderer.FONT_HEIGHT;
        int buffer = 8;

        List<VerticalSliderWidget> leftWidgets = new ArrayList<>();
        List<VerticalSliderWidget> rightWidgets = new ArrayList<>();

        int r1y = buffer;
        int r2y = r1y; // = r1y + fontHeight + buffer * 2;
        int r3y = r2y + fontHeight + buffer;
        int r3h = 25;

        int rsw = 16;

        //row 1
        LabelWidget header = new LabelWidget(w / 2, r1y, I18n.format("gregtech.machine.stock_controller.name")); //getMetaFullName()) #fix# add translations
        header.setXCentered(true);

        int bufBar = buffer + rsw;
        int midB = bufBar + buffer / 2;
        int trw = fontRenderer.getStringWidth("unpowered (off)") + 8;
        int btnMid = midB - (trw / 2);

        int r4y = r3y + buffer + 32;

        LabelWidget infoLabel = new LabelWidget(w / 2, r4y - 12, "  break | throttle");
        infoLabel.setXCentered(true);

        CycleButtonWidget leftToggleWidget = new CycleButtonWidget(btnMid + 22, r3y, trw, r3h, new String[]{ "powered (off)", "powered (on)"}, () -> (this.activeBreak < 0f) ? 0 : 1,
                (x) -> {
                    if(x == 0) {
                        this.activeBreak = -1.0f;
                        this.activeThrottle = 0;
                        leftWidgets.forEach((l) -> l.setVisible(false));
                    }
                    else {
                        this.activeBreak = 0.0f;
                        this.activeThrottle = 0.0f;
                        leftWidgets.forEach((l) -> l.setVisible(true));
                        leftWidgets.get(0).setSliderValue(1.0f - this.activeBreak);
                        leftWidgets.get(1).setSliderValue((1.0f - this.activeThrottle) / 2.0f);
                    }
                });

        CycleButtonWidget rightToggleWidget = new CycleButtonWidget(w - (btnMid + trw + 22), r3y, trw, r3h, new String[]{ "unpowered (off)", "unpowered (on)"}, () -> (this.inactiveBreak < 0f) ? 0 : 1,
                (x) -> {
                    if(x == 0) {
                        this.inactiveBreak = -1.0f;
                        this.inactiveThrottle = 0.0f;
                        rightWidgets.forEach((l) -> l.setVisible(false));
                    }
                    else {
                        this.inactiveBreak = 0.0f;
                        this.inactiveThrottle = 0.0f;
                        rightWidgets.forEach((l) -> l.setVisible(true));
                        rightWidgets.get(0).setSliderValue(1.0f - this.inactiveBreak);
                        rightWidgets.get(1).setSliderValue((1.0f - this.inactiveThrottle) / 2.0f);
                    }
                });

        int ySpaceLeft = h - buffer - r4y - buffer;
        VerticalSliderWidget leftBreakSlide = new VerticalSliderWidget("leftBreak", buffer, r4y, rsw, ySpaceLeft, 0.0f, 1.0f, 1.0f - this.activeBreak, (x) -> this.activeBreak = 1.0f - x);
        leftWidgets.add(leftBreakSlide);
        VerticalSliderWidget leftThrottleSide = new VerticalSliderWidget("leftThrottle", buffer + bufBar + buffer, r4y, rsw, ySpaceLeft, 0.0f, 1.0f, (1.0f - this.activeThrottle) / 2.0f, (x) -> this.activeThrottle = 1.0f - 2 * x);
        leftWidgets.add(leftThrottleSide);

        VerticalSliderWidget rightBreakSlide = new VerticalSliderWidget("rightBreak", w - (buffer + bufBar + buffer + rsw), r4y, rsw, ySpaceLeft, 0.0f, 1.0f, 1.0f - this.inactiveBreak, (x) -> this.inactiveBreak = 1.0f - x);
        rightWidgets.add(rightBreakSlide);
        VerticalSliderWidget rightThrottleSide = new VerticalSliderWidget("rightThrottle", w - (buffer + rsw), r4y, rsw, ySpaceLeft, 0.0f, 1.0f, (1.0f - this.inactiveThrottle) / 2.0f, (x) -> this.inactiveThrottle = 1.0f  - 2 * x);
        rightWidgets.add(rightThrottleSide);

        int sy = r4y;
        int ey = h - buffer - fontHeight - buffer - buffer;
        int hy = (ey + sy) / 2;
        LabelWidget oneLabel = new LabelWidget(w / 2, sy, "1.0");
        oneLabel.setXCentered(true);
        LabelWidget halfLabel = new LabelWidget(w / 2, hy, "0.5");
        halfLabel.setXCentered(true);
        LabelWidget zeroLabel = new LabelWidget(w / 2, ey, "0.0");
        zeroLabel.setXCentered(true);


        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, w, h)
                .widget(header)

                .widget(infoLabel)

                .widget(leftToggleWidget)
                .widget(leftBreakSlide)
                .widget(leftThrottleSide)

                .widget(rightToggleWidget)
                .widget(rightBreakSlide)
                .widget(rightThrottleSide)

                .widget(oneLabel)
                .widget(halfLabel)
                .widget(zeroLabel)
                ;

        ModularUI modularUI =  builder.build(getHolder(), entityPlayer);
        modularUI.holder.markAsDirty();

        return modularUI;
    }

    //#fix# does detected need to be saved or just refreshed on load? does ticks-alive need to be saved to prevent every one ticking at once?
    //update system based on chunk and global time instead of ticks alive?
    //should detection area be changeable and saved?
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setBoolean("active", this.active);
        return data;
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.active = data.getBoolean("active");
    }


    protected boolean canMachineConnectRedstone(EnumFacing side) {
        return true;
    }

    public AxisAlignedBB getInteractionBoundingBox() {
        return interactionBoundingBox == null ? interactionBoundingBox = StockHelperFunctions.getBox(this.getPos(), this.getFrontFacing(), 8, 8) : interactionBoundingBox;
    }
}
