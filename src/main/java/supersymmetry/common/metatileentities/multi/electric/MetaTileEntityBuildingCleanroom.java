package supersymmetry.common.metatileentities.multi.electric;

import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.GregtechTileCapabilities;
import gregtech.api.capability.IEnergyContainer;
import gregtech.api.capability.IWorkable;
import gregtech.api.capability.impl.CleanroomLogic;
import gregtech.api.capability.impl.EnergyContainerList;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.*;
import gregtech.api.pattern.*;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.GTUtility;
import gregtech.api.util.Mods;
import gregtech.api.util.TextComponentUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockCleanroomCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import gregtech.core.sound.GTSoundEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;
import supersymmetry.common.metatileentities.single.rocket.MetaTileEntityComponentScanner;

import java.util.*;

public class MetaTileEntityBuildingCleanroom extends MultiblockWithDisplayBase implements ICleanroomProvider, IWorkable {

    public static final int CLEAN_AMOUNT_THRESHOLD = 90;
    public static final int MIN_CLEAN_AMOUNT = 0;
    public static final int MIN_RADIUS = 2;
    public static final int MIN_DEPTH = 4;
    private int lDist = 0;
    private int rDist = 0;
    private int bDist = 0;
    private int fDist = 0;
    private int hDist = 0;
    private CleanroomType cleanroomType = null;
    private int cleanAmount;
    private IEnergyContainer energyContainer;
    private final CleanroomLogic cleanroomLogic = new CleanroomLogic(this, 1);
    private final Collection<ICleanroomReceiver> cleanroomReceivers = new HashSet();

    public MetaTileEntityBuildingCleanroom(ResourceLocation mteId) {
        super(mteId);
    }

    public boolean updateStructureDimensions() {
        World world = this.getWorld();
        EnumFacing front = this.getFrontFacing();
        EnumFacing back = front.getOpposite();
        EnumFacing left = front.rotateYCCW();
        EnumFacing right = left.getOpposite();
        BlockPos.MutableBlockPos lPos = new BlockPos.MutableBlockPos(this.getPos());
        BlockPos.MutableBlockPos rPos = new BlockPos.MutableBlockPos(this.getPos());
        BlockPos.MutableBlockPos fPos = new BlockPos.MutableBlockPos(this.getPos());
        BlockPos.MutableBlockPos bPos = new BlockPos.MutableBlockPos(this.getPos());
        BlockPos.MutableBlockPos hPos = new BlockPos.MutableBlockPos(this.getPos());
        int lDist = 0;
        int rDist = 0;
        int bDist = 0;
        int fDist = 0;
        int hDist = 0;

        int i;
        for(i = 1; i < 8; ++i) {
            if (lDist == 0 && this.isBlockEdge(world, lPos, left)) {
                lDist = i;
            }

            if (rDist == 0 && this.isBlockEdge(world, rPos, right)) {
                rDist = i;
            }

            if (bDist == 0 && this.isBlockEdge(world, bPos, back)) {
                bDist = i;
            }

            if (fDist == 0 && this.isBlockEdge(world, fPos, front)) {
                fDist = i;
            }

            if (lDist != 0 && rDist != 0 && bDist != 0 && fDist != 0) {
                break;
            }
        }

        for(i = 1; i < 15; ++i) {
            if (this.isBlockFloor(world, hPos, EnumFacing.DOWN)) {
                hDist = i;
            }

            if (hDist != 0) {
                break;
            }
        }

        if (lDist >= 2 && rDist >= 2 && bDist >= 2 && fDist >= 2 && hDist >= 4) {
            this.lDist = lDist;
            this.rDist = rDist;
            this.bDist = bDist;
            this.fDist = fDist;
            this.hDist = hDist;
            this.writeCustomData(GregtechDataCodes.UPDATE_STRUCTURE_SIZE, (buf) -> {
                buf.writeInt(this.lDist);
                buf.writeInt(this.rDist);
                buf.writeInt(this.bDist);
                buf.writeInt(this.fDist);
                buf.writeInt(this.hDist);
            });
            return true;
        } else {
            this.invalidateStructure();
            return false;
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBuildingCleanroom(this.metaTileEntityId);
    }
    protected void initializeAbilities() {
        this.energyContainer = new EnergyContainerList(this.getAbilities(MultiblockAbility.INPUT_ENERGY));
    }

    private void resetTileAbilities() {
        this.energyContainer = new EnergyContainerList(new ArrayList());
    }

    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        this.initializeAbilities();
        Object type = context.get("FilterType");
        if (type instanceof BlockCleanroomCasing.CasingType) {
            BlockCleanroomCasing.CasingType casingType = (BlockCleanroomCasing.CasingType)type;
            if (casingType.equals(BlockCleanroomCasing.CasingType.FILTER_CASING)) {
                this.cleanroomType = CleanroomType.CLEANROOM;
            } else if (casingType.equals(BlockCleanroomCasing.CasingType.FILTER_CASING_STERILE)) {
                this.cleanroomType = CleanroomType.STERILE_CLEANROOM;
            }
        }

        this.cleanroomLogic.setMaxProgress(Math.max(100, (this.lDist + this.rDist + 1) * (this.bDist + this.fDist + 1) * this.hDist - (this.lDist + this.rDist + 1) * (this.bDist + this.fDist + 1)));
    }

    public void invalidateStructure() {
        super.invalidateStructure();
        this.resetTileAbilities();
        this.cleanroomLogic.invalidate();
        this.cleanAmount = 0;
        this.cleanroomReceivers.forEach((receiver) -> {
            receiver.setCleanroom((ICleanroomProvider)null);
        });
        this.cleanroomReceivers.clear();
    }

    protected void updateFormedValid() {
        if (!this.getWorld().isRemote) {
            this.cleanroomLogic.updateLogic();
            if (this.cleanroomLogic.wasActiveAndNeedsUpdate()) {
                this.cleanroomLogic.setWasActiveAndNeedsUpdate(false);
                this.cleanroomLogic.setActive(false);
            }
        }

    }

    public void checkStructurePattern() {
        if (!this.isStructureFormed()) {
            this.reinitializeStructurePattern();
        }

        super.checkStructurePattern();
    }

    public boolean allowsExtendedFacing() {
        return false;
    }

    public boolean allowsFlip() {
        return false;
    }


    public boolean isBlockEdge(@NotNull World world, @NotNull BlockPos.@NotNull MutableBlockPos pos, @NotNull EnumFacing direction) {
        return world.getBlockState(pos.move(direction)) == MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.PLASCRETE);
    }

    public boolean isBlockFloor(@NotNull World world, @NotNull BlockPos.@NotNull MutableBlockPos pos, @NotNull EnumFacing direction) {
        return this.isBlockEdge(world, pos, direction) || world.getBlockState(pos) == MetaBlocks.TRANSPARENT_CASING.getState(gregtech.common.blocks.BlockGlassCasing.CasingType.CLEANROOM_GLASS);
    }

    protected @NotNull BlockPattern createStructurePattern() {
        if (this.getWorld() != null) {
            this.updateStructureDimensions();
        }

        if (this.lDist < 2) {
            this.lDist = 2;
        }

        if (this.rDist < 2) {
            this.rDist = 2;
        }

        if (this.bDist < 2) {
            this.bDist = 2;
        }

        if (this.fDist < 2) {
            this.fDist = 2;
        }

        if (this.hDist < 4) {
            this.hDist = 4;
        }

        if (this.frontFacing == EnumFacing.EAST || this.frontFacing == EnumFacing.WEST) {
            int tmp = this.lDist;
            this.lDist = this.rDist;
            this.rDist = tmp;
        }

        StringBuilder borderBuilder = new StringBuilder();
        StringBuilder wallBuilder = new StringBuilder();
        StringBuilder insideBuilder = new StringBuilder();
        StringBuilder roofBuilder = new StringBuilder();
        StringBuilder controllerBuilder = new StringBuilder();
        StringBuilder centerBuilder = new StringBuilder();

        int i;
        for(i = 0; i < this.lDist; ++i) {
            borderBuilder.append("B");
            if (i == 0) {
                wallBuilder.append("B");
                insideBuilder.append("X");
                roofBuilder.append("B");
                controllerBuilder.append("B");
                centerBuilder.append("B");
            } else {
                insideBuilder.append(" ");
                wallBuilder.append("X");
                roofBuilder.append("F");
                controllerBuilder.append("F");
                centerBuilder.append("X");
            }
        }

        borderBuilder.append("B");
        wallBuilder.append("X");
        insideBuilder.append(" ");
        roofBuilder.append("F");
        controllerBuilder.append("S");
        centerBuilder.append("K");

        for(i = 0; i < this.rDist; ++i) {
            borderBuilder.append("B");
            if (i == this.rDist - 1) {
                wallBuilder.append("B");
                insideBuilder.append("X");
                roofBuilder.append("B");
                controllerBuilder.append("B");
                centerBuilder.append("B");
            } else {
                insideBuilder.append(" ");
                wallBuilder.append("X");
                roofBuilder.append("F");
                controllerBuilder.append("F");
                centerBuilder.append("X");
            }
        }

        String[] wall = new String[this.hDist + 1];
        Arrays.fill(wall, wallBuilder.toString());
        wall[0] = borderBuilder.toString();
        wall[wall.length - 1] = borderBuilder.toString();
        String[] slice = new String[this.hDist + 1];
        Arrays.fill(slice, insideBuilder.toString());
        slice[0] = wallBuilder.toString();
        slice[slice.length - 1] = roofBuilder.toString();
        String[] center = (String[])Arrays.copyOf(slice, slice.length);
        if (this.frontFacing != EnumFacing.NORTH && this.frontFacing != EnumFacing.SOUTH) {
            center[0] = centerBuilder.toString();
            center[center.length - 1] = controllerBuilder.toString();
        } else {
            center[0] = centerBuilder.reverse().toString();
            center[center.length - 1] = controllerBuilder.reverse().toString();
        }

        TraceabilityPredicate wallPredicate = states(new IBlockState[]{this.getCasingState(), this.getGlassState()});
        TraceabilityPredicate basePredicate = this.autoAbilities().or(abilities(new MultiblockAbility[]{MultiblockAbility.INPUT_ENERGY}).setMinGlobalLimited(1).setMaxGlobalLimited(3));
        return FactoryBlockPattern.start().aisle(wall).aisle(slice).setRepeatable(this.bDist - 1).aisle(center).aisle(slice).setRepeatable(this.fDist - 1).aisle(wall).
                where('S', this.selfPredicate()).where('B', states(new IBlockState[]{this.getCasingState()}).or(basePredicate)).where('X', wallPredicate.or(basePredicate).or(doorPredicate().setMaxGlobalLimited(8)).or(abilities(new MultiblockAbility[]{MultiblockAbility.PASSTHROUGH_HATCH}).setMaxGlobalLimited(30))
                        .or(scannerPredicate().setExactLimit(1))).
                where('K', wallPredicate).where('F', this.filterPredicate()).where('C',this.scannerPredicate()).where(' ', this.innerPredicate()).build();
    }

    protected TraceabilityPredicate scannerPredicate() {
        return (new TraceabilityPredicate((blockWorldState -> {
            IBlockState bs = blockWorldState.getBlockState();
            TileEntity tile = blockWorldState.getTileEntity();
            if (tile instanceof MetaTileEntityHolder) {
                MetaTileEntity metaTileEntity = ((MetaTileEntityHolder) tile).getMetaTileEntity();
                if (metaTileEntity instanceof MetaTileEntityComponentScanner) {
                    ICleanroomReceiver cleanroomReceiver = (ICleanroomReceiver)metaTileEntity;
                    if (cleanroomReceiver.getCleanroom() != this) {
                        cleanroomReceiver.setCleanroom(this);
                        this.cleanroomReceivers.add(cleanroomReceiver);
                    }
                    return true;
                }
            }
            return false;
        })));
    }

    protected @NotNull TraceabilityPredicate filterPredicate() {
        return (new TraceabilityPredicate((blockWorldState) -> {
            IBlockState blockState = blockWorldState.getBlockState();
            Block block = blockState.getBlock();
            if (block instanceof BlockCleanroomCasing) {
                BlockCleanroomCasing.CasingType casingType = (BlockCleanroomCasing.CasingType)((BlockCleanroomCasing)blockState.getBlock()).getState(blockState);
                if (casingType.equals(BlockCleanroomCasing.CasingType.PLASCRETE)) {
                    return false;
                } else {
                    Object currentFilter = blockWorldState.getMatchContext().getOrPut("FilterType", casingType);
                    if (!currentFilter.toString().equals(casingType.getName())) {
                        blockWorldState.setError(new PatternStringError("gregtech.multiblock.pattern.error.filters"));
                        return false;
                    } else {
                        ((LinkedList)blockWorldState.getMatchContext().getOrPut("VABlock", new LinkedList())).add(blockWorldState.getPos());
                        return true;
                    }
                }
            } else {
                return false;
            }
        }, () -> {
            return (BlockInfo[]) ArrayUtils.addAll((BlockInfo[])Arrays.stream(BlockCleanroomCasing.CasingType.values()).filter((type) -> {
                return !type.equals(BlockCleanroomCasing.CasingType.PLASCRETE);
            }).map((type) -> {
                return new BlockInfo(MetaBlocks.CLEANROOM_CASING.getState(type), (TileEntity)null);
            }).toArray((x$0) -> {
                return new BlockInfo[x$0];
            }), new BlockInfo[0]);
        })).addTooltips(new String[]{"gregtech.multiblock.pattern.error.filters"});
    }

    @SideOnly(Side.CLIENT)
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.PLASCRETE;
    }

    protected @NotNull IBlockState getCasingState() {
        return MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.PLASCRETE);
    }

    protected @NotNull IBlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(gregtech.common.blocks.BlockGlassCasing.CasingType.CLEANROOM_GLASS);
    }

    protected static @NotNull TraceabilityPredicate doorPredicate() {
        return new TraceabilityPredicate((blockWorldState) -> {
            return blockWorldState.getBlockState().getBlock() instanceof BlockDoor;
        });
    }

    protected @NotNull TraceabilityPredicate innerPredicate() {
        return new TraceabilityPredicate((blockWorldState) -> {
            TileEntity tileEntity = blockWorldState.getTileEntity();
            if (!(tileEntity instanceof IGregTechTileEntity)) {
                return true;
            } else {
                MetaTileEntity metaTileEntity = ((IGregTechTileEntity)tileEntity).getMetaTileEntity();
                if (metaTileEntity instanceof ICleanroomProvider) {
                    return false;
                } else if (!(metaTileEntity instanceof ICleanroomReceiver)) {
                    return true;
                } else {
                    ICleanroomReceiver cleanroomReceiver = (ICleanroomReceiver)metaTileEntity;
                    if (cleanroomReceiver.getCleanroom() != this) {
                        cleanroomReceiver.setCleanroom(this);
                        this.cleanroomReceivers.add(cleanroomReceiver);
                    }

                    return true;
                }
            }
        });
    }

    public SoundEvent getBreakdownSound() {
        return GTSoundEvents.BREAKDOWN_MECHANICAL;
    }

    protected void addDisplayText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, this.isStructureFormed()).setWorkingStatus(this.cleanroomLogic.isWorkingEnabled(), this.cleanroomLogic.isActive()).addEnergyUsageLine(this.energyContainer).addCustom((tl) -> {
            if (this.isStructureFormed()) {
                TextComponentTranslation cleanState;
                if (this.isClean()) {
                    cleanState = TextComponentUtil.translationWithColor(TextFormatting.GREEN, "gregtech.multiblock.cleanroom.clean_state", new Object[]{this.cleanAmount});
                } else {
                    cleanState = TextComponentUtil.translationWithColor(TextFormatting.DARK_RED, "gregtech.multiblock.cleanroom.dirty_state", new Object[]{this.cleanAmount});
                }

                tl.add(TextComponentUtil.translationWithColor(TextFormatting.GRAY, "gregtech.multiblock.cleanroom.clean_status", new Object[]{cleanState}));
            }

        }).addWorkingStatusLine().addProgressLine((double)this.getProgressPercent() / 100.0);
    }

    protected void addWarningText(List<ITextComponent> textList) {
        MultiblockDisplayText.builder(textList, this.isStructureFormed(), false).addLowPowerLine(!this.drainEnergy(true)).addCustom((tl) -> {
            if (this.isStructureFormed() && !this.isClean()) {
                tl.add(TextComponentUtil.translationWithColor(TextFormatting.YELLOW, "gregtech.multiblock.cleanroom.warning_contaminated", new Object[0]));
            }

        }).addMaintenanceProblemLines(this.getMaintenanceProblems());
    }

    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.1", new Object[0]));
        tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.2", new Object[0]));
        tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.3", new Object[0]));
        tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.4", new Object[0]));
        if (TooltipHelper.isCtrlDown()) {
            tooltip.add("");
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.5", new Object[0]));
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.6", new Object[0]));
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.7", new Object[0]));
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.8", new Object[0]));
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.9", new Object[0]));
            if (Mods.AppliedEnergistics2.isModLoaded()) {
                tooltip.add(I18n.format(AEConfig.instance().isFeatureEnabled(AEFeature.CHANNELS) ? "gregtech.machine.cleanroom.tooltip.ae2.channels" : "gregtech.machine.cleanroom.tooltip.ae2.no_channels", new Object[0]));
            }

            tooltip.add("");
        } else {
            tooltip.add(I18n.format("gregtech.machine.cleanroom.tooltip.hold_ctrl", new Object[0]));
        }

    }

    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        this.getFrontOverlay().renderOrientedState(renderState, translation, pipeline, this.getFrontFacing(), this.isActive(), this.isWorkingEnabled());
    }

    @SideOnly(Side.CLIENT)
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return Textures.CLEANROOM_OVERLAY;
    }

    public boolean checkCleanroomType(@NotNull CleanroomType type) {
        return type == this.cleanroomType;
    }

    public void setCleanAmount(int amount) {
        this.cleanAmount = amount;
    }

    public void adjustCleanAmount(int amount) {
        this.cleanAmount = MathHelper.clamp(this.cleanAmount + amount, 0, 100);
    }

    public boolean isClean() {
        return this.cleanAmount >= 90;
    }

    public @NotNull List<ITextComponent> getDataInfo() {
        return Collections.singletonList(new TextComponentTranslation(this.isClean() ? "gregtech.multiblock.cleanroom.clean_state" : "gregtech.multiblock.cleanroom.dirty_state", new Object[0]));
    }

    public boolean isActive() {
        return super.isActive() && this.cleanroomLogic.isActive();
    }

    public boolean isWorkingEnabled() {
        return this.cleanroomLogic.isWorkingEnabled();
    }

    public void setWorkingEnabled(boolean isActivationAllowed) {
        if (!isActivationAllowed) {
            this.setCleanAmount(0);
        }

        this.cleanroomLogic.setWorkingEnabled(isActivationAllowed);
    }

    public int getProgress() {
        return this.cleanroomLogic.getProgressTime();
    }

    public int getMaxProgress() {
        return this.cleanroomLogic.getMaxProgress();
    }

    public int getProgressPercent() {
        return this.cleanroomLogic.getProgressPercent();
    }

    public int getEnergyTier() {
        return this.energyContainer == null ? 1 : Math.max(1, GTUtility.getFloorTierByVoltage(this.energyContainer.getInputVoltage()));
    }

    public long getEnergyInputPerSecond() {
        return this.energyContainer.getInputPerSec();
    }

    public boolean drainEnergy(boolean simulate) {
        long energyToDrain = this.isClean() ? (long)Math.min(4.0, Math.pow(4.0, (double)this.getEnergyTier())) : (long) GTValues.VA[this.getEnergyTier()];
        long resultEnergy = this.energyContainer.getEnergyStored() - energyToDrain;
        if (resultEnergy >= 0L && resultEnergy <= this.energyContainer.getEnergyCapacity()) {
            if (!simulate) {
                this.energyContainer.changeEnergy(-energyToDrain);
            }

            return true;
        } else {
            return false;
        }
    }

    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == GregtechTileCapabilities.CAPABILITY_WORKABLE) {
            return GregtechTileCapabilities.CAPABILITY_WORKABLE.cast(this);
        } else {
            return capability == GregtechTileCapabilities.CAPABILITY_CONTROLLABLE ? GregtechTileCapabilities.CAPABILITY_CONTROLLABLE.cast(this) : super.getCapability(capability, side);
        }
    }

    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_STRUCTURE_SIZE) {
            this.lDist = buf.readInt();
            this.rDist = buf.readInt();
            this.bDist = buf.readInt();
            this.fDist = buf.readInt();
            this.hDist = buf.readInt();
        } else if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            this.cleanroomLogic.setActive(buf.readBoolean());
            this.scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            this.cleanroomLogic.setWorkingEnabled(buf.readBoolean());
            this.scheduleRenderUpdate();
        }

    }

    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("lDist", this.lDist);
        data.setInteger("rDist", this.rDist);
        data.setInteger("bDist", this.fDist);
        data.setInteger("fDist", this.bDist);
        data.setInteger("hDist", this.hDist);
        data.setInteger("cleanAmount", this.cleanAmount);
        return this.cleanroomLogic.writeToNBT(data);
    }

    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.lDist = data.hasKey("lDist") ? data.getInteger("lDist") : this.lDist;
        this.rDist = data.hasKey("rDist") ? data.getInteger("rDist") : this.rDist;
        this.hDist = data.hasKey("hDist") ? data.getInteger("hDist") : this.hDist;
        this.bDist = data.hasKey("bDist") ? data.getInteger("bDist") : this.bDist;
        this.fDist = data.hasKey("fDist") ? data.getInteger("fDist") : this.fDist;
        this.reinitializeStructurePattern();
        this.cleanAmount = data.getInteger("cleanAmount");
        this.cleanroomLogic.readFromNBT(data);
    }

    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.lDist);
        buf.writeInt(this.rDist);
        buf.writeInt(this.bDist);
        buf.writeInt(this.fDist);
        buf.writeInt(this.hDist);
        buf.writeInt(this.cleanAmount);
        this.cleanroomLogic.writeInitialSyncData(buf);
    }

    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.lDist = buf.readInt();
        this.rDist = buf.readInt();
        this.bDist = buf.readInt();
        this.fDist = buf.readInt();
        this.hDist = buf.readInt();
        this.cleanAmount = buf.readInt();
        this.cleanroomLogic.receiveInitialSyncData(buf);
    }

    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle(new String[]{"XXXXX", "XIHLX", "XXDXX", "XXXXX", "XXXXX"})
                .aisle(new String[]{"XXXXX", "X   X", "G   G", "X   X", "XFFFX"})
                .aisle(new String[]{"XXXXX", "X   X", "G   G", "X   X", "XFSFX"})
                .aisle(new String[]{"XXXXX", "X   X", "G   G", "X   X", "XFFFX"})
                .aisle(new String[]{"XMXEX", "XXOCX", "XXRXX", "XXXXX", "XXXXX"})
                
                .where('X', MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.PLASCRETE))
                .where('G', MetaBlocks.TRANSPARENT_CASING.getState(gregtech.common.blocks.BlockGlassCasing.CasingType.CLEANROOM_GLASS))
                .where('S', SuSyMetaTileEntities.BUILDING_CLEANROOM, EnumFacing.SOUTH).where(' ', Blocks.AIR.getDefaultState())
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[1], EnumFacing.SOUTH)
                .where('I', MetaTileEntities.PASSTHROUGH_HATCH_ITEM, EnumFacing.NORTH)
                .where('L', MetaTileEntities.PASSTHROUGH_HATCH_FLUID, EnumFacing.NORTH)
                .where('C', SuSyMetaTileEntities.COMPONENT_SCANNER,EnumFacing.NORTH)
                .where('H', MetaTileEntities.HULL[3], EnumFacing.NORTH)
                .where('D', MetaTileEntities.DIODES[3], EnumFacing.NORTH).where('M', () -> {
            return ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH : MetaBlocks.CLEANROOM_CASING.getState(BlockCleanroomCasing.CasingType.PLASCRETE);
        }, EnumFacing.SOUTH)
                .where('O', Blocks.IRON_DOOR.getDefaultState().withProperty(BlockDoor.FACING, EnumFacing.NORTH).withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER))
                .where('R', Blocks.IRON_DOOR.getDefaultState().withProperty(BlockDoor.FACING, EnumFacing.NORTH).withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER));
        Arrays.stream(BlockCleanroomCasing.CasingType.values()).filter((casingType) -> {
            return !casingType.equals(BlockCleanroomCasing.CasingType.PLASCRETE);
        }).forEach((casingType) -> {
            shapeInfo.add(builder.where('F', MetaBlocks.CLEANROOM_CASING.getState(casingType)).build());
        });
        return shapeInfo;
    }

    protected boolean shouldShowVoidingModeButton() {
        return false;
    }

    public AxisAlignedBB getInteriorBB() {
        EnumFacing front = this.getFrontFacing();
        EnumFacing back = front.getOpposite();
        EnumFacing left = front.rotateYCCW();
        EnumFacing right = left.getOpposite();
        Vec3i down = new Vec3i(0,0,-1);
        BlockPos frontleftdown = getPos().add(multiply(front.getDirectionVec(),fDist-1))
                .add(multiply(left.getDirectionVec(),lDist-1))
                .add(multiply(down, hDist-1));
        BlockPos backrightup = getPos().add(multiply(back.getDirectionVec(), bDist-1))
                .add(multiply(right.getDirectionVec(),rDist-1));
        return new AxisAlignedBB(frontleftdown, backrightup);
    }

    private Vec3i multiply(Vec3i bp, int val) {
        return new Vec3i(bp.getX()*val, bp.getY()*val, bp.getZ()*val);
    }
}
