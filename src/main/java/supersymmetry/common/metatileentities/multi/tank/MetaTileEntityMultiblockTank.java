package supersymmetry.common.metatileentities.multi.tank;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_STRUCTURE_SIZE;

import net.minecraft.init.Blocks;
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
import net.minecraft.block.state.IBlockState;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

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
    private static final boolean DEBUG_STRUCTURE = false;

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
        } else if (type == SuSyTankType.STEEL) {
            fluidTank.setFilter(new PropertyFluidFilter(1855, true, false, false, false));
        //} else if (type == SuSyTankType.MONEL) {
        //    fluidTank.setFilter(new PropertyFluidFilter(1855, true, false, false, true));
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
            if (DEBUG_STRUCTURE) {
                System.out.println("[TankScan] centerAir not air");
            }
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

        if (DEBUG_STRUCTURE) {
            System.out.println("[TankScan] l=r=" + l + " u=" + u + " d=" + fixedDDist +
                    " air=" + air + " -> w=" + w + " h=" + h + " depth=" + depth);
        }

        boolean valid = w >= MIN_SIZE && w <= MAX_SIZE
                && h >= MIN_SIZE && h <= MAX_SIZE
                && depth >= MIN_SIZE && depth <= MAX_SIZE;

        if (!valid) {
            if (DEBUG_STRUCTURE) {
                System.out.println("[TankScan] not right size" +
                        MIN_SIZE + "-" + MAX_SIZE + ").");
            }
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
        fluidTank.setCapacity((int) (volume * (long) type.kLPerBlock * 1000L));
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

            //case MONEL:
            //    tooltip.add(I18n.format("gregtech.fluid_pipe.max_temperature", 811));
            //    tooltip.add(I18n.format("gregtech.fluid_pipe.gas_proof"));
            //    tooltip.add(I18n.format("susy.fluid_pipe.base_proof"));
            //    tooltip.add(I18n.format("gregtech.fluid_pipe.acid_proof"));
            //    break;

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
}