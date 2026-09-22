package supersymmetry.common.metatileentities.multi.tank;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_STRUCTURE_SIZE;

import java.util.Collections;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.capability.impl.PropertyFluidFilter;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;

import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

public class MetaTileEntityMultiblockTank extends MultiblockWithDisplayBase {

    private static final int MIN_SIZE = 3;
    private static final int MAX_SIZE = 16;
    private static final int TANK_SIZE_PACKET_ID = 1000;

    public final SuSyTankType type;

    private int lDist = 1;
    private int rDist = 1;
    private int uDist = 1;
    private int dDist = 1;
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
            fluidTank.setFilter(new PropertyFluidFilter(1000, true, true, false, false));
        } else if (type == SuSyTankType.STAINLESS_STEEL) {
            fluidTank.setFilter(new PropertyFluidFilter(2000, true, true, true, false));
        } else if (type == SuSyTankType.TITANIUM) {
            fluidTank.setFilter(new PropertyFluidFilter(3000, true, true, true, false));
        } else if (type == SuSyTankType.TUNGSTEN_STEEL) {
            fluidTank.setFilter(new PropertyFluidFilter(5000, true, true, true, true));
        }

        this.importFluids = new FluidTankList(true, new FluidTank[]{fluidTank});
        this.exportFluids = this.importFluids;
        this.fluidInventory = fluidTank;
    }

    // Building

    @Override
    public void checkStructurePattern() {
        if (getWorld() != null && !getWorld().isRemote) {
            boolean sizeChanged = updateStructureDimensions();

            if (sizeChanged || !isStructureFormed()) {
                reinitializeStructurePattern();
            }
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

        if (world == null || world.isRemote) {
            return false;
        }

        EnumFacing front = getFrontFacing();
        if (front == null || front.getAxis().isVertical()) {
            front = EnumFacing.NORTH;
        }

        EnumFacing back = front.getOpposite();
        EnumFacing left = front.rotateY();
        EnumFacing right = front.rotateYCCW();

        BlockPos start = getPos();
        BlockPos inside = start.offset(back);

        int newL = 0;
        int newR = 0;
        int newU = 0;
        int newD = 0;
        int newAirDepth = 0;

        for (int i = 1; i < MAX_SIZE; i++) {
            if (newL == 0 && isWall(world, inside.offset(left, i))) {
                newL = i;
            }
            if (newR == 0 && isWall(world, inside.offset(right, i))) {
                newR = i;
            }
            if (newU == 0 && isWall(world, inside.offset(EnumFacing.UP, i))) {
                newU = i;
            }
            if (newD == 0 && isWall(world, inside.offset(EnumFacing.DOWN, i))) {
                newD = i;
            }

            if (newL != 0 && newR != 0 && newU != 0 && newD != 0) {
                break;
            }
        }

        for (int i = 1; i <= MAX_SIZE - 2; i++) {
            BlockPos depthPos = start.offset(back, i);
            if (!isAir(world, depthPos)) {
                break;
            }
            newAirDepth = i;
        }

        int oldL = this.lDist;
        int oldR = this.rDist;
        int oldU = this.uDist;
        int oldD = this.dDist;
        int oldAirDepth = this.airDepth;

        this.lDist = Math.max(newL, 1);
        this.rDist = Math.max(newR, 1);
        this.uDist = Math.max(newU, 1);
        this.dDist = Math.max(newD, 1);
        this.airDepth = Math.max(newAirDepth, 1);

        boolean changed =
                oldL != this.lDist ||
                oldR != this.rDist ||
                oldU != this.uDist ||
                oldD != this.dDist ||
                oldAirDepth != this.airDepth;

        int width = this.lDist + this.rDist + 1;
        int height = this.uDist + this.dDist + 1;
        int depth = this.airDepth + 2;

        if (width < MIN_SIZE || width > MAX_SIZE ||
                height < MIN_SIZE || height > MAX_SIZE ||
                depth < MIN_SIZE || depth > MAX_SIZE) {

            invalidateStructure();
            return changed;
        }

        if (this.lDist != this.rDist) {
            invalidateStructure();
            return changed;
        }

        if (this.dDist != 1) {
            invalidateStructure();
            return changed;
        }

        writeCustomData(TANK_SIZE_PACKET_ID, buf -> {
            buf.writeInt(this.lDist);
            buf.writeInt(this.rDist);
            buf.writeInt(this.uDist);
            buf.writeInt(this.dDist);
            buf.writeInt(this.airDepth);
        });

        return changed;
    }

    protected String[] genRow(int w, int h, int depth, int aisle) {
        return null;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        if (dataId == TANK_SIZE_PACKET_ID) {
            this.lDist = buf.readInt();
            this.rDist = buf.readInt();
            this.uDist = buf.readInt();
            this.dDist = buf.readInt();
            this.airDepth = buf.readInt();
        } else {
            super.receiveCustomData(dataId, buf);
        }
    }

    @Override
    protected BlockPattern createStructurePattern() {
        int width = Math.max(lDist + rDist + 1, MIN_SIZE);
        int height = Math.max(uDist + dDist + 1, MIN_SIZE);
        int depth = Math.max(airDepth + 2, MIN_SIZE);

        String[][] aisles = new String[depth][height];

        for (int aisle = 0; aisle < depth; aisle++) {
            boolean frontWall = aisle == depth - 1;
            boolean backWall = aisle == 0;

            for (int row = 0; row < height; row++) {
                boolean topWall = row == 0;
                boolean bottomWall = row == height - 1;

                StringBuilder line = new StringBuilder(width);

                for (int column = 0; column < width; column++) {
                    boolean sideWall =
                            column == 0 ||
                            column == width - 1;

                    char symbol;

                    if (frontWall &&
                            row == height - 2 &&
                            column == lDist) {

                        symbol = 'S';

                    } else if (frontWall ||
                            backWall ||
                            topWall ||
                            bottomWall ||
                            sideWall) {

                        symbol = 'X';

                    } else {
                        symbol = ' ';
                    }

                    line.append(symbol);
                }

                aisles[aisle][row] = line.toString();
            }
        }

        int interiorCells =
                (width - 2) *
                (height - 2) *
                (depth - 2);

        int shellCells =
                width * height * depth -
                interiorCells;

        int valveLimit = (shellCells - 1) / 4;
        int casingMinimum = shellCells - 1 - valveLimit;

        FactoryBlockPattern pattern = FactoryBlockPattern.start();

        for (String[] rows : aisles) {
            pattern.aisle(rows);
        }

        return pattern
                .where('S', selfPredicate())
                .where('X', states(type.casingState)
                        .setMinGlobalLimited(casingMinimum)
                        .or(metaTileEntities(getValveForType())
                                .setMaxGlobalLimited(valveLimit)
                                .setMinGlobalLimited(1)))
                .where(' ', air())
                .build();
    }

    private int interiorVolume() {
        int width = lDist + rDist + 1;
        int height = uDist + dDist + 1;
        int depth = airDepth + 2;

        return Math.max(width - 2, 1) *
                Math.max(height - 2, 1) *
                Math.max(depth - 2, 1);
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
                .widget(new TankWidget(
                        importFluids.getTankAt(0),
                        52,
                        18,
                        72,
                        61
                )
                        .setBackgroundTexture(GuiTextures.SLOT)
                        .setContainerClicking(true, true))
                .bindPlayerInventory(player.inventory, GuiTextures.SLOT, 0);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);

        if (isStructureFormed()) {
            int volume = interiorVolume();

            textList.add(new TextComponentTranslation(
                    "susy.multiblock.tank.hud",
                    lDist + rDist + 1,
                    uDist + dDist + 1,
                    airDepth + 2,
                    volume * type.kLPerBlock
            ));
        }
    }

    @Override
    public void addInformation(
            ItemStack stack,
            World world,
            List<String> tooltip,
            boolean advanced) {

        super.addInformation(stack, world, tooltip, advanced);

        tooltip.add(I18n.format(
                "gregtech.multiblock.tank.tooltip"
        ));

        tooltip.add(I18n.format(
                "susy.multiblock.tank.info",
                type.kLPerBlock * 1000L,
                type.maxAirBlocks,
                MIN_SIZE,
                MAX_SIZE
        ));
    }

    @Override
    public <T> T getCapability(
            Capability<T> capability,
            EnumFacing side) {

        if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
            if (isStructureFormed()) {
                return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(
                        fluidInventory
                );
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
                .aisle(
                        "XXX",
                        "XXX",
                        "XXX"
                )
                .aisle(
                        "XXX",
                        "X X",
                        "XXX"
                )
                .aisle(
                        "XvX",
                        "XSX",
                        "XXX"
                )
                .where('S', this, EnumFacing.SOUTH)
                .where('v', getValveForType(), EnumFacing.NORTH)
                .where('X', type.casingState)
                .where(' ', Blocks.AIR.getDefaultState());

        return Collections.singletonList(size3.build());
    }
}