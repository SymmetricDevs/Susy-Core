package supersymmetry.common.metatileentities.multi.electric;

import static gregtech.api.util.RelativeDirection.*;

import javax.annotation.Nonnull;

import gregtech.api.GTValues;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.common.ConfigHolder;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import gregtech.api.capability.GregtechDataCodes;
import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtechfoodoption.block.GTFOGlassCasing;
import gregtechfoodoption.block.GTFOMetaBlocks;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockCoolingCoil;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

import java.util.*;

public class MetaTileEntityGreenhouse extends RecipeMapMultiblockController {

    public static final int MAX_LENGTH = 25;
    private int cellCount;
    private int length;

    public MetaTileEntityGreenhouse(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.GREENHOUSE_PLANT);
        this.recipeMapWorkable = new GreenhouseRecipeLogic(this);
    }

    protected boolean updateStructureDimensions() {
        World world = getWorld();
        EnumFacing back = getFrontFacing().getOpposite();
        BlockPos.MutableBlockPos bPos = new BlockPos.MutableBlockPos(getPos());

        int length = 0;

        for (int i = 1; i <= MAX_LENGTH; i++) {
            if (isBlockEdge(world, bPos, back)) {
                length = i;
            } else {
                break;
            }
        }

        if (length < 5 || ((length - 1) % 4) != 0) {
            invalidateStructure();
        }

        if (!this.getWorld().isRemote) {
            writeCustomData(GregtechDataCodes.UPDATE_STRUCTURE_SIZE, buf -> {
                buf.writeInt(this.length);
            });
        }

        this.length = length;
        this.cellCount = (length / 4) - 1;
        return true;
    }

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound data) {
        data.setInteger("cellCount", this.cellCount);
        return super.writeToNBT(data);
    }

    @Override
    public void readFromNBT(NBTTagCompound data) {
        super.readFromNBT(data);
        this.cellCount = data.getInteger("cellCount");
    }

    @Override
    public void checkStructurePattern() {
        if (updateStructureDimensions()) {
            reinitializeStructurePattern();
        }
        super.checkStructurePattern();
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityGreenhouse(metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        if (getWorld() != null) {
            updateStructureDimensions();
        }
        if (cellCount < 1) {
            cellCount = 1;
        }
        return createStructurePattern(this.cellCount);
    };

    protected @NotNull BlockPattern createStructurePattern(int cells) {
        TraceabilityPredicate casingPredicate = states(getCasingState());

        var builder = FactoryBlockPattern.start(RIGHT, UP, FRONT);

        builder.aisle("CCCCS", "FGGGF", "FGGGF", " FFF ");
        builder.aisle("CDDDC", "G###G", "G###G", " GGG ");
        builder.aisle("CDDDC", "G###G", "G###G", " GGG ");
        builder.aisle("CDDDC", "G###G", "G###G", " GGG ");

        for (int i = 1; i <= cells; i++) {
            builder.aisle("CDDDC", "F###F", "F###F", " FFF ");
            builder.aisle("CDDDC", "G###G", "G###G", " GGG ");
            builder.aisle("CDDDC", "G###G", "G###G", " GGG ");
            builder.aisle("CDDDC", "G###G", "G###G", " GGG ");
        }
        return builder
                .aisle("CCCCC", "FGGGF", "FGGGF", " FFF ")
                .where('S', selfPredicate())
                .where('C', states(this.getCasingState()).or(this.autoAbilities()))
                .where('D', states(Blocks.DIRT.getDefaultState(), Blocks.GRASS.getDefaultState()))
                .where('G', states(getGlassState()))
                .where('F', frames(Materials.Steel))
                .where(' ', any())
                .where('#', air())
                .build();
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    protected static IBlockState getGlassState() {
        return GTFOMetaBlocks.GTFO_GLASS_CASING.getState(GTFOGlassCasing.CasingType.GREENHOUSE_GLASS);
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    public boolean isBlockEdge(@Nonnull World world, @Nonnull BlockPos.MutableBlockPos pos,
                               @Nonnull EnumFacing direction) {
        return world.getBlockState(pos.move(direction)) == getSecondaryCasingState();
    }

    public IBlockState getSecondaryCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        List<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        for (int i = 1; i <= 5; i++) {
            shapeInfo.add(
                    new MultiblockShapeInfo(createStructurePattern(i).getPreview(new int[] { 1, 1, 1, 1, 1 })));
        }
        return shapeInfo;
    }

    @Override
    public void addInformation(ItemStack stack, World world, List<String> tooltip, boolean advanced) {
        super.addInformation(stack, world, tooltip, advanced);
        tooltip.add(TextFormatting.GRAY + I18n.format("susy.machine.greenhouse.tooltip.1"));
        tooltip.add(TextFormatting.GRAY + I18n.format("susy.machine.greenhouse.tooltip.2"));
    }

    private class GreenhouseRecipeLogic extends MultiblockRecipeLogic {

        public GreenhouseRecipeLogic(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        public int getParallelLimit() {return cellCount + 1;}
    }
}
