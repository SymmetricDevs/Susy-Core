package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.block.BlockCoolingCoil;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.recipe.SuSyRecipeMaps;
import supersymmetry.api.recipe.properties.CoilingCoilTemperatureProperty;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public class MetaTileEntityMagneticRefrigerator extends RecipeMapMultiblockController {

    private int temperature;

    public MetaTileEntityMagneticRefrigerator(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.COOLING_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityMagneticRefrigerator(metaTileEntityId);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (isStructureFormed()) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.magnetic_refrigerator.min_temperature",
                    new TextComponentTranslation(GTUtility.formatNumbers(temperature) + "K")
                            .setStyle(new Style().setColor(TextFormatting.AQUA))));
        }
        super.addDisplayText(textList);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        Object type = context.get("CoolingCoilType");
        if (type instanceof BlockCoolingCoil.CoolingCoilType) {
            this.temperature = ((BlockCoolingCoil.CoolingCoilType) type).coilTemperature;
        } else {
            this.temperature = BlockCoolingCoil.CoolingCoilType.MANGANESE_IRON_ARSENIC_PHOSPHIDE.coilTemperature;
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.temperature = Integer.MAX_VALUE;
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe, boolean consumeIfSuccess) {
        return this.temperature <= recipe.getProperty(CoilingCoilTemperatureProperty.getInstance(), 0);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("XXX", "CCC", "CCC", "XXX")
                .aisle("XXX", "C#C", "C#C", "XXX")
                .aisle("XSX", "CCC", "CCC", "XXX")
                .where('S', selfPredicate())
                .where('X', states(getCasingState()).setMinGlobalLimited(10)
                        .or(autoAbilities(true, true, true, true, true, true, false)))
                .where('C', SuSyPredicates.coolingCoils())
                .where('#', air())
                .build();
    }

    protected @NotNull IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.ALUMINIUM_FROSTPROOF);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.FROST_PROOF_CASING;
    }

    @Override
    public boolean canBeDistinct() {
        return true;
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder builder = MultiblockShapeInfo.builder()
                .aisle("XEM", "CCC", "CCC", "XXX")
                .aisle("XXD", "C#C", "C#C", "XXX")
                .aisle("ISO", "CCC", "CCC", "XXX")
                .where('X', MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.ALUMINIUM_FROSTPROOF))
                .where('S', SuSyMetaTileEntities.MAGNETIC_REFRIGERATOR, EnumFacing.SOUTH)
                .where('#', Blocks.AIR.getDefaultState())
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.MV], EnumFacing.NORTH)
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.LV], EnumFacing.SOUTH)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.LV], EnumFacing.SOUTH)
                .where('D', MetaTileEntities.FLUID_EXPORT_HATCH[GTValues.LV], EnumFacing.EAST)
                .where('M', () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH : MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF), EnumFacing.NORTH);
        Arrays.stream(BlockCoolingCoil.CoolingCoilType.values())
                .sorted(Comparator.comparingInt(entry -> -entry.coilTemperature))
                .forEach(entry -> shapeInfo.add(builder.where('C', SuSyBlocks.COOLING_COIL.getState(entry)).build()));
        return shapeInfo;
    }

    @NotNull
    @Override
    public List<ITextComponent> getDataInfo() {
        List<ITextComponent> list = super.getDataInfo();
        list.add(new TextComponentTranslation("gregtech.multiblock.magnetic_refrigerator.min_temperature",
                new TextComponentTranslation(GTUtility.formatNumbers(temperature) + "K")
                        .setStyle(new Style().setColor(TextFormatting.BLUE))));
        return list;
    }
}
