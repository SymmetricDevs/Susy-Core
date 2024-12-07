package supersymmetry.common.metatileentities.multi.storage;

import codechicken.lib.raytracer.CuboidRayTraceResult;
import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Matrix4;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.impl.FilteredFluidHandler;
import gregtech.api.capability.impl.FluidTankList;
import gregtech.api.gui.GuiTextures;
import gregtech.api.gui.ModularUI;
import gregtech.api.gui.widgets.LabelWidget;
import gregtech.api.gui.widgets.TankWidget;
import gregtech.api.metatileentity.IFastRenderMetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockWithDisplayBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.GTLog;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import supersymmetry.client.renderer.textures.custom.FluidBlockRenderer;

import java.io.IOException;
import java.util.Arrays;

import static gregtech.api.capability.GregtechDataCodes.UPDATE_FLUID;
import static gregtech.api.capability.GregtechDataCodes.UPDATE_FLUID_AMOUNT;
import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityMultiTank extends MultiblockWithDisplayBase implements IFastRenderMetaTileEntity {

    public static final int MIN_DIAMETER = 3;
    public static final int MIN_HEIGHT = 3;
    protected final int baseCapacity;
    protected int capacityMultiplier;
    protected AxisAlignedBB renderAABB;
    protected boolean renderFluids = false;
    protected FluidTank fluidTank;
    @Nullable
    protected FluidStack previousFluid;
    private int length = 0;
    private int width = 0;
    private int height = 0;

    public MetaTileEntityMultiTank(ResourceLocation metaTileEntityId, int baseCapacity) {
        super(metaTileEntityId);
        this.baseCapacity = baseCapacity;
    }

    public static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    public static IBlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.getDefaultState();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMultiTank(metaTileEntityId, baseCapacity);
    }

    @Override
    protected void initializeInventory() {
        super.initializeInventory();

        this.fluidTank = new FilteredFluidHandler(baseCapacity * width * height * length);
        this.fluidInventory = fluidTank;
        this.importFluids = new FluidTankList(false, fluidTank);
        this.exportFluids = new FluidTankList(false, fluidTank);
    }

    @Override
    protected void updateFormedValid() {
        if (!getWorld().isRemote) {
            FluidStack currentFluid = fluidTank.getFluid();
            if (previousFluid == null) {
                // tank was empty, but now is not
                if (currentFluid != null) {
                    updatePreviousFluid(currentFluid);
                }
            } else {
                if (currentFluid == null) {
                    // tank had fluid, but now is empty
                    updatePreviousFluid(null);
                } else if (previousFluid.getFluid().equals(currentFluid.getFluid()) &&
                        previousFluid.amount != currentFluid.amount) {
                    int currentFill = MathHelper
                            .floor(16 * ((float) currentFluid.amount) / fluidTank.getCapacity());
                    int previousFill = MathHelper
                            .floor(16 * ((float) previousFluid.amount) / fluidTank.getCapacity());
                    // tank has fluid with changed amount
                    previousFluid.amount = currentFluid.amount;
                    writeCustomData(UPDATE_FLUID_AMOUNT, buf -> {
                        buf.writeInt(currentFluid.amount);
                        buf.writeBoolean(currentFill != previousFill);
                    });

                } else if (!previousFluid.equals(currentFluid)) {
                    // tank has a different fluid from before
                    updatePreviousFluid(currentFluid);
                }
            }
        }
    }

    // should only be called on the server
    protected void updatePreviousFluid(FluidStack currentFluid) {
        previousFluid = currentFluid == null ? null : currentFluid.copy();
        writeCustomData(UPDATE_FLUID, buf -> buf
                .writeCompoundTag(currentFluid == null ? null : currentFluid.writeToNBT(new NBTTagCompound())));
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        initializeInventory();
//        Object type = context.get("FilterType");
    }

    @Override
    public void checkStructurePattern() {
        if (!this.isStructureFormed()) {
            reinitializeStructurePattern();
        }
        super.checkStructurePattern();
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    public boolean allowsFlip() {
        return false;
    }

    /**
     * Scans for blocks around the controller to update the dimensions
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean updateStructureDimensions() {
        World world = getWorld();
        EnumFacing front = getFrontFacing();
        EnumFacing back = front.getOpposite();
        EnumFacing left = front.rotateYCCW();
        EnumFacing right = left.getOpposite();

        BlockPos oPos = getPos().up().offset(back);

        BlockPos.MutableBlockPos lPos = new BlockPos.MutableBlockPos(oPos);
        BlockPos.MutableBlockPos rPos = new BlockPos.MutableBlockPos(oPos);
        BlockPos.MutableBlockPos bPos = new BlockPos.MutableBlockPos(oPos);
        BlockPos.MutableBlockPos uPos = new BlockPos.MutableBlockPos(oPos);

        // find the distances from the controller to the plascrete blocks on one horizontal axis and the Y axis
        // repeatable aisles take care of the second horizontal axis
        int lDist = 0;
        int rDist = 0;
        int bDist = 0;
        int uDist = 0;

        // find the left, right, back, and up distances for the structure pattern
        for (int i = 1; i < 14; i++) {
            if (i < 8 && lDist == 0 && isBlockEdge(world, lPos, left)) lDist = i;
            if (i < 8 && rDist == 0 && isBlockEdge(world, rPos, right)) rDist = i;
            if (bDist == 0 && isBlockEdge(world, bPos, back)) bDist = i;
            if (uDist == 0 && isBlockEdge(world, uPos, EnumFacing.UP)) uDist = i;
            if (lDist != 0 && rDist != 0 && bDist != 0 && uDist != 0) break;
        }

        int width = lDist + rDist + 1;
        int length = bDist + 2;
        int height = uDist + 2;


        if (width < MIN_DIAMETER || lDist != rDist || length < MIN_DIAMETER || height < MIN_HEIGHT) {
            invalidateStructure();
            return false;
        }

        this.width = width;
        this.length = length;
        this.height = height;

        writeCustomData(GregtechDataCodes.UPDATE_STRUCTURE_SIZE, buf -> {
            buf.writeInt(this.width);
            buf.writeInt(this.length);
            buf.writeInt(this.height);
        });
        return true;
    }

    /**
     * @param world     the world to check
     * @param pos       the pos to check and move
     * @param direction the direction to move
     * @return if a block is a valid wall block at pos moved in direction
     */
    public boolean isBlockEdge(@NotNull World world, @NotNull BlockPos.MutableBlockPos pos,
                               @NotNull EnumFacing direction) {
        IBlockState iblockstate = world.getBlockState(pos.move(direction));
        return iblockstate == getCasingState() || iblockstate == getGlassState();
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        // return the default structure, even if there is no valid size found
        // this means auto-build will still work, and prevents terminal crashes.
        if (getWorld() != null) updateStructureDimensions();

        // these can sometimes get set to 0 when loading the game, breaking JEI
        if (width < MIN_DIAMETER) width = MIN_DIAMETER;
        if (length < MIN_DIAMETER) length = MIN_DIAMETER;
        if (height < MIN_HEIGHT) height = MIN_HEIGHT;

        TraceabilityPredicate casingPredicate = states(getCasingState())
                .or(abilities(MultiblockAbility.EXPORT_FLUIDS, MultiblockAbility.IMPORT_FLUIDS).setPreviewCount(0));
        return FactoryBlockPattern.start(RIGHT, FRONT, UP)
                .aisle(Slice.layer(Slice.SELF, Slice.FULL, Slice.FULL, width, length))
                .aisle(Slice.layer(Slice.EDGE, Slice.MIDD, Slice.EDGE, width, length)).setRepeatable(1, height - 2)
                .aisle(Slice.layer(Slice.FULL, Slice.FULL, Slice.FULL, width, length))
                .where('S', selfPredicate())
                .where('X', casingPredicate)
                .where('Y', states(getGlassState())
                        .or(casingPredicate))
                .where('#', air())
                .build();
    }

    @SideOnly(Side.CLIENT)
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @SideOnly(Side.CLIENT)
    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.MULTIBLOCK_TANK_OVERLAY;
    }

    @Override
    public void renderMetaTileEntity(CCRenderState renderState, Matrix4 translation, IVertexOperation[] pipeline) {
        super.renderMetaTileEntity(renderState, translation, pipeline);
        getFrontOverlay().renderSided(getFrontFacing(), renderState, translation, pipeline);
    }

    @Override
    public boolean hasMaintenanceMechanics() {
        return false;
    }

    @Override
    public boolean onRightClick(EntityPlayer playerIn, EnumHand hand, EnumFacing facing,
                                CuboidRayTraceResult hitResult) {
        if (!isStructureFormed())
            return false;
        return super.onRightClick(playerIn, hand, facing, hitResult);
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return isStructureFormed();
    }

    @Override
    protected ModularUI.Builder createUITemplate(@NotNull EntityPlayer entityPlayer) {
        return ModularUI.defaultBuilder()
                .widget(new LabelWidget(6, 6, getMetaFullName()))
                .widget(new TankWidget(importFluids.getTankAt(0), 52, 18, 72, 61)
                        .setBackgroundTexture(GuiTextures.SLOT)
                        .setContainerClicking(true, true))
                .bindPlayerInventory(entityPlayer.inventory, GuiTextures.SLOT, 0);
    }

    @Override
    public void renderMetaTileEntityFast(CCRenderState renderState, Matrix4 translation, float partialTicks) {
        if (getWorld() != null && getPos() != null) {
//            renderState.lightMatrix.locate(getWorld(), getPos().up());
//            SusyTextures.PLASTIC_CAN.render(renderState, translation.copy().translate(0, 1, 0), new IVertexOperation[]{renderState.lightMatrix}, getFrontFacing());
//            Vec3d v = new Vec3d(translation.m03, translation.m13, translation.m23);
//            renderState.setFluidColour(new FluidStack(FluidRegistry.getFluid("water"), 1));fluidStack.getFluid().getColor(fluidStack) << 8 | alpha
//            DoubleLightMatrix mat = new DoubleLightMatrix();
//            mat.locate(getWorld(), v);
//            new Matrix4().translate(translation.m03 - getPos().getX(), translation.m13 - getPos().getY(), translation.m23 - getPos().getZ()).operate(renderState);
//            renderState.lightMatrix.locate(getWorld(), getPos().up());
//            for (EnumFacing facing : EnumFacing.values()) {
//                Textures.renderFace(renderState, translation.copy().translate(0, 1, 0), new IVertexOperation[] {renderState.lightMatrix}, facing, new Cuboid6(new Vector3(), new Vector3(1, 0.8, 1)),
//                        TextureUtils.getTexture(FluidRegistry.getFluid("water").getStill()), BlockRenderLayer.TRANSLUCENT);
//            }
            BufferBuilder buffer = renderState.getBuffer();
            FluidBlockRenderer renderer = FluidBlockRenderer.INSTANCE;
//            renderer.renderFluidCube(getWorld(), getPos().up(), 0.75, Materials.Water.getFluid(1000), buffer, new Vec3d(translation.m03, translation.m13 + 1, translation.m23));
//            renderer.renderFluidFace(getWorld(), getPos().up(), EnumFacing.UP, 0.875, Materials.Water.getFluid(1000), buffer, new Vec3d(translation.m03, translation.m13 + 1, translation.m23));
//            renderer.renderFluidFace(getWorld(), getPos().up(), EnumFacing.NORTH, 0.875, Materials.Water.getFluid(1000), buffer, new Vec3d(translation.m03, translation.m13 + 1, translation.m23));

            if (fluidTank == null) return;
            FluidStack stack = fluidTank.getFluid();
            if (stack == null || stack.getFluid() == null || stack.amount == 0) return;

            double fluidHeight = ((double) stack.amount) / (baseCapacity * width * height * length);
//            double fluidHeight = 0;
            AxisAlignedBB aabb = getRenderAABB();
            renderer.renderFluidTank(getWorld(), aabb, fluidHeight, stack, buffer, new Vec3d(translation.m03, translation.m13, translation.m23).add(aabb.minX - getPos().getX(), aabb.minY - getPos().getY(), aabb.minZ - getPos().getZ()));
        }
    }

    @Override
    @NotNull
    public AxisAlignedBB getRenderBoundingBox() {
        return getRenderAABB();
    }

    protected AxisAlignedBB getRenderAABB() {
        if (renderAABB == null) {

            EnumFacing front = getFrontFacing();
            EnumFacing back = front.getOpposite();
            EnumFacing left = front.rotateYCCW();
            EnumFacing right = left.getOpposite();

            this.renderAABB = new AxisAlignedBB(
                    getPos().up()
                            .offset(back, length - 2)
                            .offset(right, (width - 3) / 2),
                    getPos().up(height - 2)
                            .offset(back)
                            .offset(left, (width - 3) / 2)
            );
        }
        return renderAABB;
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_STRUCTURE_SIZE) {
            this.width = buf.readInt();
            this.length = buf.readInt();
            this.height = buf.readInt();
        } else if (dataId == UPDATE_FLUID) {
            try {
                this.fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(buf.readCompoundTag()));
            } catch (IOException ignored) {
                GTLog.logger.warn("Failed to load fluid from NBT in a quantum tank at {} on a routine fluid update",
                        this.getPos());
            }
            scheduleRenderUpdate();
        } else if (dataId == UPDATE_FLUID_AMOUNT) {
            // amount must always be read even if it cannot be used to ensure the reader index advances
            int amount = buf.readInt();
            boolean updateRendering = buf.readBoolean();
            FluidStack stack = fluidTank.getFluid();
            if (stack != null) {
                stack.amount = Math.min(amount, fluidTank.getCapacity());
                if (updateRendering)
                    scheduleRenderUpdate();
            }
        }
    }

    @Override
    public NBTTagCompound writeToNBT(@NotNull NBTTagCompound data) {
        super.writeToNBT(data);
        data.setTag("FluidInventory", fluidTank.writeToNBT(new NBTTagCompound()));
        data.setInteger("width", this.width);
        data.setInteger("length", this.length);
        data.setInteger("height", this.height);
        return data;
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.fluidTank.readFromNBT(data.getCompoundTag("FluidInventory"));
        this.width = data.getInteger("width");
        this.length = data.getInteger("length");
        this.height = data.getInteger("height");
        reinitializeStructurePattern();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.width);
        buf.writeInt(this.length);
        buf.writeInt(this.height);
        buf.writeCompoundTag(fluidTank.getFluid() == null ? null : fluidTank.getFluid().writeToNBT(new NBTTagCompound()));
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.width = buf.readInt();
        this.length = buf.readInt();
        this.height = buf.readInt();
        try {
            this.fluidTank.setFluid(FluidStack.loadFluidStackFromNBT(buf.readCompoundTag()));
        } catch (IOException e) {
            GTLog.logger.warn("Failed to load fluid from NBT in a quantum tank at " + this.getPos() +
                    " on initial server/client sync");
        }
    }

    private enum Slice {

        SELF('X', 'S', 'X'), // XXSXX
        FULL('X', 'X', 'X'), // XXXXX
        EDGE('X', 'Y', 'Y'), // XYYYX
        MIDD('Y', ' ', ' '); // Y   Y

        final char sides;
        final char middle;
        final char fill;

        Slice(char sides, char middle, char fill) {
            this.sides = sides;
            this.middle = middle;
            this.fill = fill;
        }

        private static String[] layer(Slice start, Slice fill, Slice end, int width, int length) {
            String[] result = new String[length];
            result[0] = start.line(width);
            result[length - 1] = end.line(width);
            Arrays.fill(result, 1, length - 1, fill.line(width));
            return result;
        }

        @SuppressWarnings("StringRepeatCanBeUsed") // I hate you jabel
        private String line(int width) {
            StringBuilder builder = new StringBuilder();
            builder.append(sides);
            for (int i = 0; i < (width - 3) / 2; i++) {
                builder.append(fill);
            }
            builder.append(middle);
            for (int i = 0; i < (width - 3) / 2; i++) {
                builder.append(fill);
            }
            return builder.append(sides).toString();
        }
    }
}
