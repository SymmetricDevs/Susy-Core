package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.GTValues;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.MultiblockShapeInfo;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.metatileentities.MetaTileEntities;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockDrillBit;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityPreciseMillingMachine extends RecipeMapMultiblockController {
    public MetaTileEntityPreciseMillingMachine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.MILLING_RECIPES);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityPreciseMillingMachine(this.metaTileEntityId);
    }

    @NotNull
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("BBBBBB", "CCCCCC", "CGGGGC", "CCCCCC")
                .aisle("BBBBBB", "C    C", "CDDDDC", "CCCCCC")
                .aisle("BBBBBB", "C    C", "C    C", "CCCCCC")
                .aisle("BBBBBB", "CWWWWS", "CWWWWC", "CCCCCC")
                .where('S', selfPredicate())
                .where('B', states(getBaseCasingState()).setMinGlobalLimited(18)
                        .or(abilities(MultiblockAbility.INPUT_ENERGY)
                                .setMinGlobalLimited(1).setMaxGlobalLimited(3)
                                .addTooltip("gregtech.multiblock.pattern.error.milling.lower"))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH)
                                .setExactLimit(1)
                                .addTooltip("gregtech.multiblock.pattern.error.milling.lower"))
                )
                .where('C', states(getUpperCasingState()).setMinGlobalLimited(35)
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS)
                                .setMinGlobalLimited(1)
                                .addTooltip("gregtech.multiblock.pattern.error.milling.upper"))
                        .or(abilities(MultiblockAbility.EXPORT_ITEMS)
                                .setMinGlobalLimited(1)
                                .addTooltip("gregtech.multiblock.pattern.error.milling.upper"))
                )
                .where('D', states(getDrillBitState()))
                .where('G', states(getGearBoxState()))
                .where('W', states(getGlassState()))
                .build();
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder baseBuilder = MultiblockShapeInfo.builder()
                .where('S', SuSyMetaTileEntities.MILLING, EnumFacing.SOUTH)
                .where('B', getBaseCasingState())
                .where('C', getUpperCasingState())
                .where('D', getDrillBitState())
                .where('G', getGearBoxState())
                .where('W', getGlassState())
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.HV], EnumFacing.SOUTH)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.HV], EnumFacing.SOUTH)
                .where('E', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.HV], EnumFacing.SOUTH)
                .where('G', getGearBoxState())
                .where('M',
                        () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH :
                                getBaseCasingState(), EnumFacing.SOUTH);
        shapeInfo.add(baseBuilder.shallowCopy()
                .aisle("BBBBBB", "CCCCCC", "CGGGGC", "CCCCCC")
                .aisle("BBBBBB", "C    C", "CDDDDC", "CCCCCC")
                .aisle("BBBBBB", "C    C", "C    C", "CCCCCC")
                .aisle("BBBBEM", "OWWWWS", "IWWWWC", "CCCCCC")
                .build());
        return shapeInfo;
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (sourcePart instanceof IMultiblockAbilityPart<?>) {
            MultiblockAbility<?> ability = ((IMultiblockAbilityPart<?>) sourcePart).getAbility();
            if (ability.equals(MultiblockAbility.MAINTENANCE_HATCH) || ability.equals(MultiblockAbility.INPUT_ENERGY)) {
                return Textures.SOLID_STEEL_CASING;
            }
        }
        // for IO Buses and other unrecognized parts
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    protected static IBlockState getBaseCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID);
    }

    protected static IBlockState getUpperCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.STAINLESS_CLEAN);
    }

    protected static IBlockState getDrillBitState() {
        return SuSyBlocks.DRILL_BIT.getState(BlockDrillBit.DrillBitType.STEEL);
    }

    protected static IBlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS);
    }

    protected static IBlockState getGearBoxState() {
        return MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STAINLESS_STEEL_GEARBOX);
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.MILLING_OVERLAY;
    }
}
