package supersymmetry.common.metatileentities.multi.electric;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockGlassCasing;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockDrillBit;
import supersymmetry.common.blocks.SuSyBlocks;

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
                        .or(abilities(MultiblockAbility.INPUT_ENERGY).setPreviewCount(1)
                                .setMinGlobalLimited(1).setMaxGlobalLimited(2)
                                .addTooltip("susy.multiblock.pattern.error.milling.lower"))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH)
                                .setExactLimit(1)
                                .addTooltip("susy.multiblock.pattern.error.milling.lower")))
                .where('C', states(getUpperCasingState()).setMinGlobalLimited(35)
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setPreviewCount(1)
                                .setMinGlobalLimited(1)
                                .addTooltip("susy.multiblock.pattern.error.milling.upper"))
                        .or(abilities(MultiblockAbility.EXPORT_ITEMS).setPreviewCount(1)
                                .setMinGlobalLimited(1)
                                .addTooltip("susy.multiblock.pattern.error.milling.upper")))
                .where('D', states(getDrillBitState()))
                .where('G', states(getGearBoxState()))
                .where('W', states(getGlassState()))
                .build();
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (sourcePart instanceof IMultiblockAbilityPart<?>) {
            MultiblockAbility<?> ability = ((IMultiblockAbilityPart<?>) sourcePart).getAbility();
            if (ability.equals(MultiblockAbility.MAINTENANCE_HATCH) || ability.equals(MultiblockAbility.INPUT_ENERGY)) {
                return Textures.SOLID_STEEL_CASING;
            }
        }

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
