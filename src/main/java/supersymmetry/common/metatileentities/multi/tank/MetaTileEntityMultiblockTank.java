package supersymmetry.common.metatileentities.multi.tank;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_STRUCTURE_SIZE;

import net.minecraft.init.Blocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
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
import net.minecraftforge.fluids.capability.IFluidHandler;

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
import gregtech.client.renderer.texture.Textures;
import gregtech.api.capability.impl.FluidTankList;

import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

import java.util.Collections;
import java.util.List;

public class MetaTileEntityMultiblockTank extends MultiblockWithDisplayBase {

    private static final int MAX_VALVES = 4;
    private static final int MIN_SIZE = 3;
    private static final int MAX_SIZE = 16;

    public final SuSyTankType type;

    private int lDist = 1, rDist = 1, uDist = 1, dDist = 1;
    private int airDepth = 1;

    private FilteredFluidHandler fluidTank;

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
        this.fluidTank = new FilteredFluidHandler(0);
        if (type == SuSyTankType.WOOD) {
            fluidTank.setFilter(new PropertyFluidFilter(340, false, false, false, false));
        }
        this.importFluids = new FluidTankList(true, new FluidTank[]{ fluidTank });
        this.exportFluids = this.importFluids;
        this.fluidInventory = fluidTank;
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
        if (world.isRemote) {
            return true;
        }

        EnumFacing front = getFrontFacing();
        EnumFacing back = front.getOpposite();
        EnumFacing right = front.rotateYCCW();
        EnumFacing left = right.getOpposite();

        BlockPos.MutableBlockPos lPos = new BlockPos.MutableBlockPos(getPos());
        BlockPos.MutableBlockPos rPos = new BlockPos.MutableBlockPos(getPos());
        BlockPos.MutableBlockPos uPos = new BlockPos.MutableBlockPos(getPos());
        BlockPos.MutableBlockPos dPos = new BlockPos.MutableBlockPos(getPos());

        int lDist = 0;
        int rDist = 0;
        int uDist = 0;
        int dDist = 0;
        int airDepth = 0;

        // Scansiona i muri laterali, superiore e inferiore fino a 16 blocchi
        for (int i = 1; i < 16; i++) {
            if (lDist == 0 && isWall(world, lPos.move(left))) lDist = i;
            if (rDist == 0 && isWall(world, rPos.move(right))) rDist = i;
            if (uDist == 0 && isWall(world, uPos.move(EnumFacing.UP))) uDist = i;
            if (dDist == 0 && isWall(world, dPos.move(EnumFacing.DOWN))) dDist = i;

            if (lDist != 0 && rDist != 0 && uDist != 0 && dDist != 0) break;
        }

        // Scansiona la profondità dell'aria interna verso il retro
        BlockPos.MutableBlockPos bPos = new BlockPos.MutableBlockPos(getPos());
        for (int i = 1; i < 15; i++) {
            if (isAir(world, bPos.move(back))) airDepth = i; else break;
        }

        int w = 1 + lDist + rDist;
        int h = 1 + uDist + dDist;
        int depth = airDepth + 2;

        if (w < MIN_SIZE || w > MAX_SIZE || h < MIN_SIZE || h > MAX_SIZE || depth < MIN_SIZE || depth > MAX_SIZE) {
            invalidateStructure();
            return false;
        }

        this.lDist = lDist;
        this.rDist = rDist;
        this.uDist = uDist;
        this.dDist = dDist;
        this.airDepth = airDepth;

        writeCustomData(UPDATE_STRUCTURE_SIZE, buf -> {
            buf.writeInt(this.lDist);
            buf.writeInt(this.rDist);
            buf.writeInt(this.uDist);
            buf.writeInt(this.dDist);
            buf.writeInt(this.airDepth);
        });
        return true;
    }

    protected String[] genRow(int w, int h, int depth, int aisle) { return null; }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == UPDATE_STRUCTURE_SIZE) {
            this.lDist = buf.readInt();
            this.rDist = buf.readInt();
            this.uDist = buf.readInt();
            this.dDist = buf.readInt();
            this.airDepth = buf.readInt();
        }
    }

    @Override
    protected BlockPattern createStructurePattern() {
        if (getWorld() != null) updateStructureDimensions();

        int w = Math.max(lDist + rDist + 1, MIN_SIZE);
        int h = Math.max(uDist + dDist + 1, MIN_SIZE);
        int depth = Math.max(airDepth + 2, MIN_SIZE);

        String[][] aisles = new String[depth][h];
        for (int a = 0; a < depth; a++) {
            boolean frontWall = a == depth - 1;
            boolean backWall = a == 0;
            for (int r = 0; r < h; r++) {
                boolean topWall = r == h - 1;
                boolean bottomWall = r == 0;
                StringBuilder row = new StringBuilder(w);
                for (int c = 0; c < w; c++) {
                    boolean sideWall = c == 0 || c == w - 1;
                    char cell;
                    if (frontWall && r == uDist && c == lDist) {
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
        return Math.max(lDist + rDist - 1, 0) * Math.max(uDist + dDist - 1, 0) * Math.max(airDepth, 0);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        int volume = interiorVolume();
        if (volume < 1 || volume > type.maxAirBlocks) {
            invalidateStructure();
            return;
        }
        fluidTank.setCapacity(volume * type.kLPerBlock);
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        fluidTank.setCapacity(0);
    }

    // Rendering

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return type.baseTexture;
    }

    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.MULTIBLOCK_TANK_OVERLAY;
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
            textList.add(new TextComponentTranslation("susy.multiblock.tank.hud",
                    lDist + rDist + 1, uDist + dDist + 1, airDepth + 2, volume * type.kLPerBlock));
        }
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.multiblock.tank.tooltip"));
        tooltip.add(I18n.format("susy.multiblock.tank.info", type.kLPerBlock * 1000L, type.maxAirBlocks, MIN_SIZE, MAX_SIZE));
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
}