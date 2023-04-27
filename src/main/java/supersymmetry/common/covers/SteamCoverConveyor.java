package supersymmetry.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.IControllable;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.CoverWithUI;
import gregtech.api.cover.ICoverable;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.*;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import gregtech.common.covers.*;
import gregtech.common.covers.filter.ItemFilterContainer;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.MathHelper;

public class SteamCoverConveyor extends CoverConveyor {

    protected int transferRate;

    public SteamCoverConveyor(ICoverable coverable, EnumFacing attachedSide, int itemsPerSecond) {
        super(coverable, attachedSide, 0, itemsPerSecond);
        this.transferRate = itemsPerSecond;
    }

    @Override
    public void setTransferRate(int transferRate) {
        this.transferRate = transferRate;
        super.setTransferRate(transferRate);
    }

    @Override
    public ModularUI createUI(EntityPlayer player) {
        WidgetGroup primaryGroup = new WidgetGroup();
        primaryGroup.addWidget(new LabelWidget(10, 5, this.getUITitle(), "Steam"));
        primaryGroup.addWidget((new IncrementButtonWidget(136, 20, 30, 20, 1, 8, 64, 512, this::adjustTransferRate)).setDefaultTooltip().setShouldClientCallback(false));
        primaryGroup.addWidget((new IncrementButtonWidget(10, 20, 30, 20, -1, -8, -64, -512, this::adjustTransferRate)).setDefaultTooltip().setShouldClientCallback(false));
        primaryGroup.addWidget(new ImageWidget(40, 20, 96, 20, GuiTextures.DISPLAY));
        primaryGroup.addWidget((new TextFieldWidget2(42, 26, 92, 20, () -> {
            return String.valueOf(this.transferRate);
        }, (val) -> {
            if (val != null && !val.isEmpty()) {
                this.setTransferRate(MathHelper.clamp(Integer.parseInt(val), 1, this.maxItemTransferRate));
            }

        })).setNumbersOnly(1, this.maxItemTransferRate).setMaxLength(4).setPostFix("cover.conveyor.transfer_rate"));
        primaryGroup.addWidget(new CycleButtonWidget(10, 45, 75, 20, ConveyorMode.class, this::getConveyorMode, this::setConveyorMode));
        primaryGroup.addWidget((new CycleButtonWidget(7, 166, 116, 20, ManualImportExportMode.class, this::getManualImportExportMode, this::setManualImportExportMode)).setTooltipHoverString("cover.universal.manual_import_export.mode.description"));
        if (this.coverHolder.getWorld().getTileEntity(this.coverHolder.getPos()) instanceof TileEntityItemPipe || this.coverHolder.getWorld().getTileEntity(this.coverHolder.getPos().offset(this.attachedSide)) instanceof TileEntityItemPipe) {
            ImageCycleButtonWidget distributionModeButton = (new ImageCycleButtonWidget(149, 166, 20, 20, GuiTextures.DISTRIBUTION_MODE, 3, () -> {
                return this.distributionMode.ordinal();
            }, (val) -> {
                this.setDistributionMode(DistributionMode.values()[val]);
            })).setTooltipHoverString((val) -> {
                return DistributionMode.values()[val].getName();
            });
            primaryGroup.addWidget(distributionModeButton);
        }

        this.itemFilterContainer.initUI(70, primaryGroup::addWidget);
        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, 176, 272).widget(primaryGroup).bindPlayerInventory(player.inventory, GuiTextures.SLOT, 7, 190);
        return this.buildUI(builder, player);
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("TransferRate", this.transferRate);
        return tagCompound;
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        this.transferRate = tagCompound.getInteger("TransferRate");
    }

    @Override
    protected TextureAtlasSprite getPlateSprite() {
        return Textures.STEAM_CASING_BRONZE.getSpriteOnSide(SimpleSidedCubeRenderer.RenderSide.SIDE);
    }
}
