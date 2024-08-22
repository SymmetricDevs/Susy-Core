package supersymmetry.common.metatileentities.single.electric;

import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IControllable;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.SlotWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.TieredMetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtechfoodoption.utils.GTFOUtils;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.items.IItemHandlerModifiable;
import org.jetbrains.annotations.NotNull;

import static gregtech.api.GTValues.VA;
import static gregtech.api.capability.GregtechDataCodes.WORKING_ENABLED;

public class MetaTileEntityLandfill extends TieredMetaTileEntity implements IControllable {

    private boolean isWorkingEnabled = true;
    private boolean hasItems = false;
    private int numBlocksFilled;
    private boolean isDoneFilling;
    private ObjectArrayList<BlockPos> cachedPos = new ObjectArrayList<>(); // Using both the stack and the list features

    public MetaTileEntityLandfill(ResourceLocation metaTileEntityId, int tier) {
        super(metaTileEntityId, tier);
        initializeInventory();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityLandfill(metaTileEntityId, this.getTier());
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
        if (isWorkingEnabled && !this.getWorld().isRemote && (hasItems || this.notifiedItemInputList != null)) {
            this.hasItems = true;
            int startSlot = GTFOUtils.getFirstUnemptyItemSlot(this.importItems, 0);
            if (startSlot == -1) {
                this.hasItems = false;
            }
            if (startSlot != -1 && this.energyContainer.removeEnergy(VA[getTier()]) == -VA[getTier()]) {
                if (!isDoneFilling) {
                    if (cachedPos == null || cachedPos.isEmpty()) {
                        cachedPos.push(this.getPos().down());
                    }
                    BlockPos.MutableBlockPos operationPos = new BlockPos.MutableBlockPos(cachedPos.top());
                    if (this.isBlocked(getWorld().getBlockState(operationPos)) && cachedPos.size() == 1) {
                        this.isDoneFilling = true;
                    } else {
                        while (getNextBlock(getWorld(), operationPos) && operationPos.getY() > 0) {
                            cachedPos.push(operationPos.toImmutable());
                        }
                        getWorld().setBlockState(operationPos, Blocks.DIRT.getDefaultState());
                        cachedPos.pop();
                        numBlocksFilled++;
                    }
                }
                if (!isDoneFilling || numBlocksFilled > 1000) {
                    this.importItems.extractItem(startSlot, getTier(), false);
                }
            }

        }
    }



    private boolean getNextBlock(World world, BlockPos.MutableBlockPos pos) {
        pos.move(EnumFacing.DOWN);
        if (!isBlocked(world.getBlockState(pos))) {
            return true;
        } else for (EnumFacing facing : EnumFacing.HORIZONTALS) {
            if (!isBlocked(world.getBlockState(pos.move(facing)))) {
                return true;
            }
            pos.move(facing.getOpposite());
        }
        pos.move(EnumFacing.UP);
        return false;
    }


    private boolean isBlocked(IBlockState state)
    {
        Block block = state.getBlock(); //Forge: state must be valid for position
        Material mat = state.getMaterial();

        if (!(block instanceof BlockDoor) && block != Blocks.STANDING_SIGN && block != Blocks.LADDER && block != Blocks.REEDS)
        {
            return mat != Material.PORTAL && mat != Material.STRUCTURE_VOID ? mat.blocksMovement() : true;
        }
        else
        {
            return true;
        }
    }

    private ModularUI.Builder createUITemplate(EntityPlayer player, int gridSize) {
        int backgroundWidth = gridSize > 6 ? 176 + (gridSize - 6) * 18 : 176;
        int center = backgroundWidth / 2;

        int gridStartX = center - (gridSize * 9);

        int inventoryStartX = center - 9 - 4 * 18;
        int inventoryStartY = 18 + 18 * gridSize + 12;

        ModularUI.Builder builder = ModularUI.builder(GuiTextures.BACKGROUND, backgroundWidth, 18 + 18 * gridSize + 94)
                .label(10, 5, getMetaFullName());

        for (int y = 0; y < gridSize; y++) {
            for (int x = 0; x < gridSize; x++) {
                int index = y * gridSize + x;

                builder.widget(new SlotWidget(importItems, index,
                        gridStartX + x * 18, 18 + y * 18, true, true)
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

    @Override
    public void receiveCustomData(int dataId, @NotNull PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == WORKING_ENABLED) {
            this.isWorkingEnabled = buf.readBoolean();
        }
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setBoolean("workingEnabled", this.isWorkingEnabled);
        data.setBoolean("isDoneFilling", this.isDoneFilling);
        data.setInteger("numBlocksFilled", this.numBlocksFilled);
        return super.writeToNBT(data);
    }


    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.isWorkingEnabled = data.getBoolean("workingEnabled");
        this.isDoneFilling = data.getBoolean("isDoneFilling");
        this.numBlocksFilled = data.getInteger("numBlocksFilled");
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE)
            return GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this);
        return super.getCapability(capability, side);
    }

    @Override
    public void writeInitialSyncData(@NotNull PacketBuffer buf) {
        super.writeInitialSyncData(buf);
    }
}
