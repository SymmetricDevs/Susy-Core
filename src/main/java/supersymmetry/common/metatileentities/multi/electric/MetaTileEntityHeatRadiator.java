package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.MetaTileEntityHolder;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockDisplayText;
import gregtech.api.metatileentity.multiblock.ParallelLogicType;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.util.TextComponentUtil;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.impl.NoEnergyMultiblockRecipeLogic;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockSerpentine;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static gregtech.api.util.RelativeDirection.*;

public class MetaTileEntityHeatRadiator extends RecipeMapMultiblockController {
    public static final int MIN_RADIUS = 1;
    public static final int MIN_HEIGHT = 1;
    private int sDist = 0;
    private int bDist = 0;
    private int area;

    public MetaTileEntityHeatRadiator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.HEAT_RADIATOR_RECIPES);
        this.recipeMapWorkable = new ParallelableNoEnergyMultiblockRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityHeatRadiator(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        if (getWorld() != null) updateStructureDimensions();
        if (sDist < MIN_RADIUS) sDist = MIN_RADIUS;
        if (bDist < MIN_HEIGHT) bDist = MIN_HEIGHT;


        return FactoryBlockPattern.start(RIGHT, FRONT, UP)
                .aisle(rowPattern(rowType.BOTTOM, sDist).toString())
                .aisle(rowPattern(rowType.MIDDLE, sDist)).setRepeatable(1, bDist)
                .aisle(rowPattern(rowType.TOP, sDist))
                .where('S', selfPredicate())
                .where('A', states(getCasingState())
                        .or(autoAbilities(false, true, false, false, false, false, false)))
                .where('B', states(getRadiatorElementState()))
                .where('C', states(getCasingState())
                        .or(autoAbilities(false, false, false, false, true, false, false).setMinGlobalLimited(1))
                        .or(autoAbilities(false, false, false, false, false, true, false).setMinGlobalLimited(1)))
                .build();
    }

    private enum rowType {
        BOTTOM, MIDDLE, TOP, MIDDLE_PREVIEW, BOTTOM_PREVIEW
    }

    private String rowPattern(rowType rowType,int radius) {
        char center, left, right, other;

        // A: Metal Casing; S: Radiator; C: Metal Casing or Hatches; B: Tube Block
        switch (rowType) {
            case BOTTOM:
                center = 'S'; left = right = 'A'; other = 'A';
                break;
            case MIDDLE:
                center = 'B'; left = right = 'C'; other = 'B';
                break;
            case TOP:
                center = 'A'; left = right = 'A'; other = 'A';
                break;

        // These are only for JEI preview. I: Input Hatch; O: Output Hatch; M: Maintenance Hatch
            case BOTTOM_PREVIEW:
                center = 'S'; left = 'M'; right = 'A'; other = 'A';
                break;
            case MIDDLE_PREVIEW:
                center = 'B'; left = 'I'; right = 'O'; other = 'B';
                break;
            default:
                throw new IllegalArgumentException("Invalid rowType: " + rowType);
        }
        StringBuilder rowBuilder = new StringBuilder();
        for (int i = 0; i < radius; i++) {
            if (i == 0) {
                // Add Center
                rowBuilder.append(center);
            } else {
                rowBuilder.append(other);
                rowBuilder.insert(0, other);
            }
        }
        // Add Edges. I don't know whether left/right is correct or not, but it probably doesn't matter.
        rowBuilder.append(right);
        rowBuilder.insert(0, left);

        return rowBuilder.toString();
    }


    protected boolean updateStructureDimensions() {

        World world = getWorld();
        EnumFacing front = getFrontFacing();
        EnumFacing up = UP.getRelativeFacing(this.getFrontFacing(), this.getUpwardsFacing(), this.isFlipped()); // From the flare stack, I hate free rotation.
        EnumFacing left = front.rotateAround(up.getAxis());
        EnumFacing right = left.getOpposite();

        BlockPos.MutableBlockPos lPos = new BlockPos.MutableBlockPos(getPos().offset(up));
        BlockPos.MutableBlockPos rPos = new BlockPos.MutableBlockPos(getPos().offset(up));
        BlockPos.MutableBlockPos uPos = new BlockPos.MutableBlockPos(getPos());

        int sDist = 0;
        int bDist = 0;

        // find the left, right, and upper distances for the structure pattern
        // maximum size is 11x16 including walls
        for(int i = 0; i < 16; ++i) {
            if (isBlockEdge(world, uPos, up)) {
                bDist = i;
                break;
            }
        }

        for (int i = 1; i < 6; i++) { // start at 1 for an off-by-one error
            if (isBlockEdge(world, lPos, left) & isBlockEdge(world, rPos, right)) {
                sDist = i; // The & is absolutely *essential* here.
                break;
            }
        }


        if (sDist < MIN_RADIUS || bDist < MIN_HEIGHT) {
            invalidateStructure();
            return false;
        }

        this.sDist = sDist;
        this.bDist = bDist;
        this.area = bDist * (2 * sDist - 1);

        if (!this.getWorld().isRemote) {
            writeCustomData(GregtechDataCodes.UPDATE_STRUCTURE_SIZE, buf -> {
                buf.writeInt(this.sDist);
                buf.writeInt(this.bDist);
                buf.writeInt(this.area);
            });
        }
        return true;
    }

    @Override
    public void checkStructurePattern() {
        // From the evap branch, thanks to @EightXOR8
        // This is here so that it automatically updates the dimensions once a second if it isn't formed
        // hope this doesn't put too much of a toll on TPS - It really should not
        if (!isStructureFormed() || structurePattern == null) {
            reinitializeStructurePattern();
        }
        super.checkStructurePattern();
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        ArrayList<MultiblockShapeInfo.Builder> builders = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                    .aisle(rowPattern(rowType.BOTTOM_PREVIEW, i), rowPattern(rowType.MIDDLE_PREVIEW, i), rowPattern(rowType.TOP, i));
            builders.add(builder);
        }
        for (int j = 2; j < 15; j++) {

            // Probably not the best way to do this, but it works.
            String[] rows = new String[j + 2];
            Arrays.fill(rows, rowPattern(rowType.MIDDLE, 5));
            rows[0] = rowPattern(rowType.BOTTOM_PREVIEW, 5);
            rows[1] = rowPattern(rowType.MIDDLE_PREVIEW, 5);
            rows[j + 1] = rowPattern(rowType.TOP, 5);

            MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                    .aisle(rows);
            builders.add(builder);
        }
        builders.forEach(builder -> shapeInfo.add(builder
                        .where('S', SuSyMetaTileEntities.HEAT_RADIATOR, EnumFacing.SOUTH)
                        .where('I', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.LV], EnumFacing.SOUTH)
                        .where('O', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.LV], EnumFacing.SOUTH)
                        .where('M',
                                () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH :
                                        getCasingState(),EnumFacing.SOUTH)  // From Cleanroom
                        .where('A', getCasingState())
                        .where('C', getCasingState())
                        .where('B', getRadiatorElementState())
                        .build()
                ));
        return shapeInfo;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("sDist", this.sDist);
        data.setInteger("bDist", this.bDist);
        data.setInteger("area", this.area);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.sDist = data.getInteger("sDist");
        this.bDist = data.getInteger("bDist");
        this.area = data.getInteger("area");
        reinitializeStructurePattern();
    }

    @Override
    public void writeInitialSyncData(PacketBuffer buf) {
        super.writeInitialSyncData(buf);
        buf.writeInt(this.sDist);
        buf.writeInt(this.bDist);
        buf.writeInt(this.area);
    }

    @Override
    public void receiveInitialSyncData(PacketBuffer buf) {
        super.receiveInitialSyncData(buf);
        this.sDist = buf.readInt();
        this.bDist = buf.readInt();
        this.area = buf.readInt();
        reinitializeStructurePattern();
    }

    @Override
    public void receiveCustomData(int dataId, PacketBuffer buf) {
        super.receiveCustomData(dataId, buf);
        if (dataId == GregtechDataCodes.UPDATE_STRUCTURE_SIZE) {
            this.sDist = buf.readInt();
            this.bDist = buf.readInt();
            this.area = buf.readInt();
        } else if (dataId == GregtechDataCodes.WORKABLE_ACTIVE) {
            scheduleRenderUpdate();
        } else if (dataId == GregtechDataCodes.WORKING_ENABLED) {
            scheduleRenderUpdate();
        }
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        //Override to remove parallel display from superclass
        MultiblockDisplayText.builder(textList, isStructureFormed())
                .setWorkingStatus(recipeMapWorkable.isWorkingEnabled(), recipeMapWorkable.isActive())
                .addWorkingStatusLine()
                .addProgressLine(recipeMapWorkable.getProgressPercent());
        if(isStructureFormed()) {
            ITextComponent componentParallelAmount = TextComponentUtil.stringWithColor(TextFormatting.DARK_PURPLE,
                    String.valueOf(this.area));
            ITextComponent componentParallelAmountBase = TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                    "gregtech.machine.heat_radiator.parallel",
                    componentParallelAmount);
            ITextComponent componentParallelAmountHover = TextComponentUtil.translationWithColor(TextFormatting.GRAY,
                    "gregtech.machine.heat_radiator.parallel_hover");

            textList.add(TextComponentUtil.setHover(componentParallelAmountBase, componentParallelAmountHover));
        }
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(I18n.format("gregtech.multiblock.heat_radiator.tooltip.1"));
        tooltip.add(I18n.format("gregtech.multiblock.heat_radiator.tooltip.2"));
    }

    public boolean isBlockEdge(@Nonnull World world, @Nonnull BlockPos.MutableBlockPos pos, @Nonnull EnumFacing direction) {
        return world.getBlockState(pos.move(direction)) == getCasingState() || world.getTileEntity(pos) instanceof MetaTileEntityHolder;
    }

    public IBlockState getRadiatorElementState() {
        return SuSyBlocks.SERPENTINE.getState(BlockSerpentine.SerpentineType.BASIC);
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.RADIATOR_OVERLAY;
    }

    private class ParallelableNoEnergyMultiblockRecipeLogic extends NoEnergyMultiblockRecipeLogic {
        public ParallelableNoEnergyMultiblockRecipeLogic(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        public int getParallelLimit() {
            return ((MetaTileEntityHeatRadiator) this.getMetaTileEntity()).area;
        }

        @Override
        protected long getMaxParallelVoltage() {
            return 2147432767L;
        }

        @Override
        public boolean consumesEnergy() {
            return false;
        }

        @Override
        public @NotNull ParallelLogicType getParallelLogicType() {
            return ParallelLogicType.MULTIPLY;
        }
    }
}
