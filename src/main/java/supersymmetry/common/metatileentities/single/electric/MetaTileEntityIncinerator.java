package supersymmetry.common.metatileentities.single.electric;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.AdvancedTextWidget;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtechfoodoption.utils.GTFOUtils;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.client.renderer.textures.SusyTextures;

import java.util.List;

import static gregtech.api.GTValues.V;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_ACTIVE;
import static gregtech.api.capability.GregtechDataCodes.WORKING_ENABLED;

public class MetaTileEntityIncinerator extends TieredMetaTileEntity implements IControllable {

    private boolean isWorkingEnabled = true;
    private boolean canProgress;
    private boolean hasItems = false;
    private int progress = 0;
    private boolean isClogged = false;
    public final int maxProgress;
    public final int itemsPerRun;
    public static final int UPDATE_CLOGGED = 3488;

    public MetaTileEntityIncinerator(ResourceLocation metaTileEntityId, int tier, int maxProgress, int itemsPerRun) {
        super(metaTileEntityId, tier);
        initializeInventory();
        this.maxProgress = maxProgress;
        this.itemsPerRun = itemsPerRun;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        SusyTextures.INCINERATOR_OVERLAY.renderOrientedState(renderState, translation, pipeline, getFrontFacing(), this.isActive(), true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityIncinerator(metaTileEntityId, this.getTier(), maxProgress, itemsPerRun);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        int rowSize = (int) Math.sqrt(getInventorySize());
        return createUITemplate(entityPlayer, rowSize)
                .build(getHolder(), entityPlayer);
    }

    @Override
    public void update() {
        super.update();
        if (isActive() && this.getOffsetTimer() % 5 == 0) {
            if (this.getWorld().isRemote) {
                this.incineratingParticles();
            } else {
                getWorld().playSound(null, getPos(), SoundEvents.ENTITY_GENERIC_BURN, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        }
        if (!this.getWorld().isRemote && this.getOffsetTimer() % 40 == 0) {
            checkClogged();
        }
        if (!isClogged && isWorkingEnabled && !this.getWorld().isRemote && (hasItems || this.notifiedItemInputList != null)) {
            this.hasItems = true;
            int startSlot = GTFOUtils.getFirstUnemptyItemSlot(this.importItems, 0);
            if (startSlot == -1) {
                this.hasItems = false;
                progress = 0;
                if (this.canProgress)
                    setCanProgress(false);
            } else if (this.energyContainer.removeEnergy(V[getTier()] / 2) == -V[getTier()] / 2) {
                if (!this.canProgress)
                    setCanProgress(true);
                progress++;
                if (progress >= maxProgress) {
                    this.importItems.extractItem(startSlot, itemsPerRun, false);
                    progress = 0;
                }
            } else {
                if (this.canProgress)
                    setCanProgress(false);
            }
        }
    }

    public void checkClogged() {
        for (BlockPos pos : BlockPos.getAllInBox(getPos().up(1), getPos().up(8))) {
            IBlockState state = getWorld().getBlockState(pos);
            if (!state.getBlock().isAir(state, getWorld(), pos)) {
                setClogged(true);
                progress = 0;
                return;
            }
        }
        setClogged(false);
    }

    @Override
    public boolean isActive() {
        return canProgress && isWorkingEnabled && !isClogged;
    }

    public void setCanProgress(boolean canProgress) {
        this.canProgress = canProgress;
        this.writeCustomData(UPDATE_ACTIVE, buf -> buf.writeBoolean(canProgress));
    }

    @SideOnly(Side.CLIENT)
    private void incineratingParticles() {
        BlockPos pos = this.getPos();
        double xPos = pos.getX() + Math.random() / 2;
        double yPos = pos.getY() + 0.5D;
        double zPos = pos.getZ() + Math.random() / 2;

        double ySpd = 0.1D + Math.random() / 3;
        double xSpd = (Math.random() - 0.5D) / 3;
        double zSpd = (Math.random() - 0.5D) / 3;

        getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, xPos, yPos, zPos, xSpd, ySpd, zSpd);
        getWorld().spawnParticle(EnumParticleTypes.SMOKE_LARGE, xPos, yPos, zPos, xSpd, ySpd, zSpd);
    }

    private ModularUI.Builder createUITemplate(EntityPlayer player, int gridSize) {
        int backgroundWidth = gridSize > 6 ? 176 + (gridSize - 6) * 18 : 176;
        int center = backgroundWidth / 2;

        int gridStartX = center - (gridSize * 9);

        int inventoryStartX = center - 9 - 4 * 18;
        int inventoryStartY = 30 + 18 * gridSize + 12;

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, backgroundWidth, 30 + 18 * gridSize + 94)
                .label(10, 5, getMetaFullName());

        builder.widget(new AdvancedTextWidget(10, 18, this::getDisplayText, 0));

        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                int index = y * gridSize + x;

                builder.widget(new SlotWidget(importItems, index,
                        gridStartX + x * 18, 30 + y * 18, true, true)
                        .setBackgroundTexture(GuiTextures.SLOT));
            }
        }

        return builder.bindPlayerInventory(player.inventory, GuiTextures.SLOT, inventoryStartX, inventoryStartY);
    }

    private int getInventorySize() {
        int sizeRoot = 1 + Math.min(9, getTier());
        return sizeRoot * sizeRoot;
    }

    @Override
    protected IItemHandlerModifiable createImportItemHandler() {
        return new NotifiableItemStackHandler(this, getInventorySize(), this, false);
    }

    @Override
    public boolean isWorkingEnabled() {
        return isWorkingEnabled;
    }

    @Override
    public void setWorkingEnabled(boolean b) {
        this.isWorkingEnabled = b;
        this.writeCustomData(WORKING_ENABLED, buf -> buf.writeBoolean(b));
    }

    public void setClogged(boolean b) {
        this.isClogged = b;
        this.writeCustomData(UPDATE_CLOGGED, buf -> buf.writeBoolean(b));
    }

    public void getDisplayText(List<ITextComponent> list) {
        list.add(new TextComponentTranslation(isClogged ? "gregtech.multiblock.incinerator.clogged" : "gregtech.multiblock.incinerator.working"));
    }

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == WORKING_ENABLED) {
            this.isWorkingEnabled = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_CLOGGED) {
            this.isClogged = buf.readBoolean();
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_ACTIVE) {
            this.canProgress = buf.readBoolean();
            scheduleRenderUpdate();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setBoolean("workingEnabled", this.isWorkingEnabled);
        data.setInteger("progress", this.progress);
        data.setBoolean("isClogged", this.isClogged);
        data.setBoolean("canProgress", this.canProgress);
        return super.writeToNBT(data);
    }


    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isWorkingEnabled = data.getBoolean("workingEnabled");
        this.progress = data.getInteger("progress");
        this.isClogged = data.getBoolean("isClogged");
        this.canProgress = data.getBoolean("canProgress");
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeBoolean(this.isWorkingEnabled);
        buf.writeInt(this.progress);
        buf.writeBoolean(this.isClogged);
        buf.writeBoolean(this.canProgress);
    }

    @Override
    public void receiveInitialSyncData(@NotNull PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.isWorkingEnabled = buf.readBoolean();
        this.progress = buf.readInt();
        this.isClogged = buf.readBoolean();
        this.canProgress = buf.readBoolean();
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.incinerator.tooltip.1", itemsPerRun, maxProgress));
        tooltip.add(I18n.format("gregtech.machine.incinerator.tooltip.2"));
        tooltip.add(I18n.format("gregtech.machine.incinerator.tooltip.3"));
    }
}
