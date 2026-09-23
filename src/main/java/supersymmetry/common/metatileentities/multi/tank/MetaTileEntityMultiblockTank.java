package supersymmetry.common.metatileentities.multi.tank;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_STRUCTURE_SIZE;

import net.minecraft.init.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import gregtech.api.util.GTUtility;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.PropertyFluidFilter;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.client.renderer.texture.Textures;

import supersymmetry.common.metatileentities.SuSyMetaTileEntities;
import supersymmetry.client.renderer.textures.SusyTextures;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Matrix4;
import codechicken.lib.render.pipeline.IVertexOperation;

import java.util.Collections;
import java.util.List;

public class MetaTileEntityMultiblockTank extends MultiblockWithDisplayBase {

    private static final int UPDATE_TANK_FILL_STATE = 9999;
    private static final int MAX_VALVES = 4;
    private static final int MIN_SIZE = 3;
    private static final int MAX_SIZE = 16;
    private static final boolean DEBUG_STRUCTURE = false;

    public final SuSyTankType type;

    private int lDist = 1, rDist = 1, uDist = 1, dDist = 1;
    private int airDepth = 1;

    private LongFilteredFluidHandler fluidTank;
    private int lastFillState = -1;

    public MetaTileEntityMultiblockTank(ResourceLocation metaTileEntityId, SuSyTankType type) {
        super(metaTileEntityId);
        this.type = type;
        initializeInventory();
    }

    private MetaTileEntity getValveForType() {
        switch (this.type) {
            case WOOD:
                return SuSyMetaTileEntities.WOOD_TANK_VALVES;
            case STEEL:
                return SuSyMetaTileEntities.STEEL_TANK_VALVES;
            case MONEL:
                return SuSyMetaTileEntities.MONEL_TANK_VALVES;
            case STAINLESS_STEEL:
                return SuSyMetaTileEntities.STAINLESS_STEEL_TANK_VALVES;
            case TITANIUM:
                return SuSyMetaTileEntities.TITANIUM_TANK_VALVES;
            case TUNGSTEN_STEEL:
                return SuSyMetaTileEntities.TUNGSTEN_STEEL_TANK_VALVES;
            default:
                return null;
        }
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMultiblockTank(metaTileEntityId, type);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();
        this.fluidTank = new LongFilteredFluidHandler(0);

        if (type == SuSyTankType.WOOD) {
            fluidTank.setFilter(new PropertyFluidFilter(340, false, false, false, false));
        } else if (type == SuSyTankType.STEEL) {
            fluidTank.setFilter(new PropertyFluidFilter(1855, true, false, false, false));
        } else if (type == SuSyTankType.MONEL) {
            fluidTank.setFilter(new PropertyFluidFilter(811, true, false, false, true));
        } else if (type == SuSyTankType.STAINLESS_STEEL) {
            fluidTank.setFilter(new PropertyFluidFilter(2428, true, true, true, true));
        } else if (type == SuSyTankType.TITANIUM) {
            fluidTank.setFilter(new PropertyFluidFilter(2426, true, false, true, false));
        } else if (type == SuSyTankType.TUNGSTEN_STEEL) {
            fluidTank.setFilter(new PropertyFluidFilter(3587, true, false, false, true));
        }

        this.importFluids = new FluidTankList(true, new FluidTank[]{ fluidTank });
        this.exportFluids = this.importFluids;
        this.fluidInventory = fluidTank;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == UPDATE_TANK_FILL_STATE) {
            this.lastFillState = buf.readInt();
            if (getWorld() != null && getWorld().isRemote) {
                scheduleRenderUpdate();
                getWorld().markBlockRangeForRenderUpdate(getPos(), getPos());
            }
        } else if (dataId == UPDATE_STRUCTURE_SIZE) {
            this.lDist = buf.readInt();
            this.rDist = buf.readInt();
            this.uDist = buf.readInt();
            this.dDist = buf.readInt();
            this.airDepth = buf.readInt();
            reinitializeStructurePattern();
        } else {
            super.receiveCustomData(dataId, buf);
        }
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(getFillState());
        buf.writeInt(lDist);
        buf.writeInt(rDist);
        buf.writeInt(uDist);
        buf.writeInt(dDist);
        buf.writeInt(airDepth);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.lastFillState = buf.readInt();
        this.lDist = buf.readInt();
        this.rDist = buf.readInt();
        this.uDist = buf.readInt();
        this.dDist = buf.readInt();
        this.airDepth = buf.readInt();
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        super.writeToNBT(data);
        data.setInteger("lDist", lDist);
        data.setInteger("rDist", rDist);
        data.setInteger("uDist", uDist);
        data.setInteger("dDist", dDist);
        data.setInteger("airDepth", airDepth);
        data.setInteger("lastFillState", lastFillState);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.lDist = data.getInteger("lDist");
        this.rDist = data.getInteger("rDist");
        this.uDist = data.getInteger("uDist");
        this.dDist = data.getInteger("dDist");
        this.airDepth = data.getInteger("airDepth");
        this.lastFillState = data.getInteger("lastFillState");
    }

    // Building

    @Override
    public void checkStructurePattern() {
        if (!isStructureFormed()) {
            reinitializeStructurePattern();
        }
        super.checkStructurePattern();
    }

    @Override
    protected void updateFormedValid() {
    }

    private boolean isWall(World world, BlockPos pos) {
        return world.getBlockState(pos) == type.casingState ||
                GTUtility.getMetaTileEntity(world, pos) instanceof IMultiblockPart;
    }

    private boolean isAir(World world, BlockPos pos) {
        return world.isAirBlock(pos);
    }

    protected boolean updateStructureDimensions() {
        World world = getWorld();
        if (world == null || world.isRemote) return false;

        EnumFacing front = getFrontFacing();

        if (front.getAxis().isVertical()) {
            return false;
        }
        
        EnumFacing back = front.getOpposite();
        EnumFacing right = front.rotateYCCW();
        EnumFacing left = right.getOpposite();

        BlockPos start = getPos();
        BlockPos centerAir = start.offset(back, 1);

        if (!isAir(world, centerAir)) {
            return false;
        }

        final int fixedDDist = 1;

        int l = 0, u = 0, air = 0;

        for (int i = 1; i < MAX_SIZE; i++) {
            if (isWall(world, centerAir.offset(left, i))) {
                l = i;
                break;
            }
        }

        for (int i = 1; i < MAX_SIZE; i++) {
            if (isWall(world, centerAir.offset(EnumFacing.UP, i))) {
                u = i;
                break;
            }
        }

        for (int i = 1; i <= MAX_SIZE - 2; i++) {
            if (isAir(world, start.offset(back, i))) {
                air = i;
            } else {
                break;
            }
        }

        int w = 1 + l + l;
        int h = 1 + u + fixedDDist;
        int depth = air + 2;

        this.lDist = l;
        this.rDist = l;
        this.uDist = u;
        this.dDist = fixedDDist;
        this.airDepth = air;

        boolean valid = w >= MIN_SIZE && w <= MAX_SIZE
                && h >= MIN_SIZE && h <= MAX_SIZE
                && depth >= MIN_SIZE && depth <= MAX_SIZE;

        if (!valid) {
            return false;
        }

        writeCustomData(UPDATE_STRUCTURE_SIZE, buf -> {
            buf.writeInt(lDist);
            buf.writeInt(rDist);
            buf.writeInt(uDist);
            buf.writeInt(dDist);
            buf.writeInt(airDepth);
        });
        return true;
    }

    @Override
    protected BlockPattern createStructurePattern() {
        if (getWorld() != null) {
            updateStructureDimensions();
        }

        int w = Math.max(lDist + rDist + 1, MIN_SIZE);
        int h = Math.max(uDist + dDist + 1, MIN_SIZE);
        int depth = Math.max(airDepth + 2, MIN_SIZE);

        String[][] aisles = new String[depth][h];
        for (int a = 0; a < depth; a++) {
            boolean frontWall = (a == depth - 1);
            boolean backWall = (a == 0);
            for (int r = 0; r < h; r++) {
                boolean topWall = (r == 0);
                boolean bottomWall = (r == h - 1);
                StringBuilder row = new StringBuilder(w);
                for (int c = 0; c < w; c++) {
                    boolean sideWall = (c == 0 || c == w - 1);
                    char cell;

                    if (frontWall && r == dDist && c == lDist) {
                        cell = 'S';
                    } else if (backWall || frontWall || bottomWall || topWall || sideWall) {
                        cell = 'X';
                    } else {
                        cell = ' ';
                    }
                    row.append(cell);
                }
                aisles[a][r] = row.toString();
            }
        }

        int interiorCells = (w - 2) * (h - 2) * (depth - 2);
        int skinCells = w * h * depth - interiorCells;

        FactoryBlockPattern pattern = FactoryBlockPattern.start();
        for (String[] rows : aisles) {
            pattern.aisle(rows);
        }
        return pattern
                .where('S', selfPredicate())
                .where('X', states(type.casingState)
                        .setMinGlobalLimited(skinCells - MAX_VALVES)
                        .or(metaTileEntities(getValveForType())
                                .setMaxGlobalLimited(MAX_VALVES)
                                .setMinGlobalLimited(1)))
                .where(' ', air())
                .build();
    }

    private int interiorVolume() {
        int w = lDist + rDist + 1;
        int h = uDist + dDist + 1;
        int depth = airDepth + 2;
        return Math.max(w - 2, 1) * Math.max(h - 2, 1) * Math.max(depth - 2, 1);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int volume = interiorVolume();
        if (volume < 1 || volume > type.maxAirBlocks) {
            invalidateStructure();
            return;
        }
        fluidTank.setLongCapacity(volume * (long) type.kLPerBlock * 1000L);

        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_TANK_FILL_STATE, buf -> buf.writeInt(getFillState()));
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        fluidTank.setLongCapacity(0);
        if (!getWorld().isRemote) {
            writeCustomData(UPDATE_TANK_FILL_STATE, buf -> buf.writeInt(0));
        }
    }

    @Override
    public void update() {
        super.update();
        if (!getWorld().isRemote && isStructureFormed()) {
            int currentState = getFillState();
            if (currentState != lastFillState) {
                this.lastFillState = currentState;
                writeCustomData(UPDATE_TANK_FILL_STATE, buf -> buf.writeInt(currentState));
                markDirty();
            }
        }
    }

    // Rendering

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return type != null && type.baseTexture != null ? type.baseTexture : SusyTextures.MONEL_400_CASING;
    }

    private int getFillState() {
        if (!isStructureFormed() || fluidTank == null || fluidTank.getLongCapacity() <= 0) {
            return 0;
        }
        
        long stored = fluidTank.getFluidAmount();
        long capacity = fluidTank.getLongCapacity();

        if (stored <= 0) return 0;
        if (stored >= capacity) return 8;

        double fillRatio = (double) stored / capacity;
        int state = (int) Math.ceil(fillRatio * 8.0);
        
        return Math.min(Math.max(state, 1), 8);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ICubeRenderer getFrontOverlay() {
        if (!isStructureFormed()) {
            return SusyTextures.TANK_OVERLAYS[0];
        }

        int state = (lastFillState >= 0) ? lastFillState : getFillState();

        if (SusyTextures.TANK_OVERLAYS != null && SusyTextures.TANK_OVERLAYS.length > 0) {
            int index = Math.min(Math.max(state, 0), SusyTextures.TANK_OVERLAYS.length - 1);
            if (SusyTextures.TANK_OVERLAYS[index] != null) {
                return SusyTextures.TANK_OVERLAYS[index];
            }
        }

        return SusyTextures.TANK_OVERLAYS[0];
    }
    @Override
    @SideOnly(Side.CLIENT)
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);

        if (getFrontFacing() != null) {
            ICubeRenderer overlay = getFrontOverlay();
            if (overlay != null) {
                overlay.renderSided(getFrontFacing(), renderState, translation, pipeline);
            }
        }
    }

    // GUI

    @Override
    protected boolean openGUIOnRightClick() {
        return true;
    }

    @Override
    protected ModularUI.Builder createUITemplate(EntityPlayer player) {
        return ModularUI.builder(GuiTextures.BACKGROUND, 176, 166)
                .widget(new LabelWidget(6, 6, getMetaFullName()))
                .widget(new TankWidget(importFluids.getTankAt(0), 52, 18, 72, 61)
                        .setBackgroundTexture(GuiTextures.SLOT)
                        .setContainerClicking(true, true))
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 0);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        if (isStructureFormed()) {
            int volume = interiorVolume();
            long volumeInLiters = volume * (long) type.kLPerBlock * 1000L;
            textList.add(new TextComponentTranslation("susy.multiblock.tank.hud",
                    lDist + rDist + 1, uDist + dDist + 1, airDepth + 2, volumeInLiters));
        }
    }

    @Override
    public void addInformation(ItemStack stack, World world, List tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);

        tooltip.add(I18n.format("gregtech.multiblock.tank.tooltip"));
        tooltip.add(I18n.format("susy.multiblock.tank.info", type.kLPerBlock * 1000L, type.maxAirBlocks, MIN_SIZE, MAX_SIZE));

        switch (this.type) {
            case WOOD:
                tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", 340));
                break;

            case STEEL:
                tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", 1855));
                tooltip.add(I18n.format("gregtech.fluid_pipe.gas_proof"));
                break;

            case MONEL:
                tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", 811));
                tooltip.add(I18n.format("gregtech.fluid_pipe.gas_proof"));
                tooltip.add(I18n.format("susy.fluid_pipe.base_proof"));
                tooltip.add(I18n.format("gregtech.fluid_pipe.acid_proof"));
                break;

            case STAINLESS_STEEL:
                tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", 2428));
                tooltip.add(I18n.format("gregtech.fluid_pipe.gas_proof"));
                tooltip.add(I18n.format("gregtech.fluid_pipe.cryo_proof"));
                tooltip.add(I18n.format("susy.fluid_pipe.base_proof"));
                tooltip.add(I18n.format("gregtech.fluid_pipe.acid_proof"));
                break;

            case TITANIUM:
                tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", 2426));
                tooltip.add(I18n.format("gregtech.fluid_pipe.gas_proof"));
                tooltip.add(I18n.format("susy.fluid_pipe.base_proof"));
                break;

            case TUNGSTEN_STEEL:
                tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", 3587));
                tooltip.add(I18n.format("gregtech.fluid_pipe.gas_proof"));
                tooltip.add(I18n.format("gregtech.fluid_pipe.acid_proof"));
                break;
        }
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (isStructureFormed()) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(fluidInventory);
            }
            return null;
        }
        return super.getCapability(capability, side);
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        MultiblockShapeInfo.Builder size3 = MultiblockShapeInfo.builder()
                .aisle("XXX", "XXX", "XXX")
                .aisle("XXX", "X X", "XXX")
                .aisle("XvX", "XSX", "XXX")
                .where('S', this, EnumFacing.SOUTH)
                .where('v', getValveForType(), EnumFacing.NORTH)
                .where('X', type.casingState)
                .where(' ', Blocks.AIR.getDefaultState());
        return Collections.singletonList(size3.build());
    }

    // Inner Class per gestire il Long Capacity
    public static class LongFilteredFluidHandler extends FilteredFluidHandler {

        private long longCapacity;

        public LongFilteredFluidHandler(long capacity) {
            super((int) Math.min(capacity, Integer.MAX_VALUE));
            this.longCapacity = capacity;
        }

        public void setLongCapacity(long capacity) {
            this.longCapacity = capacity;
            super.setCapacity((int) Math.min(capacity, Integer.MAX_VALUE));
        }

        public long getLongCapacity() {
            return this.longCapacity;
        }
    }
}