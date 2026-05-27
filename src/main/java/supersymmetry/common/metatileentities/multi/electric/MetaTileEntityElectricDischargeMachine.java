package supersymmetry.common.metatileentities.multi.electric;

import gregicality.multiblocks.api.render.GCYMTextures;
import gregicality.multiblocks.common.block.GCYMMetaBlocks;
import gregicality.multiblocks.common.block.blocks.BlockLargeMultiblockCasing;
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
import supersymmetry.common.blocks.BlockEDMElectrode;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.metatileentities.SuSyMetaTileEntities;

import java.util.ArrayList;
import java.util.List;

public class MetaTileEntityElectricDischargeMachine extends RecipeMapMultiblockController {

    public MetaTileEntityElectricDischargeMachine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.EDM_RECIPES);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityElectricDischargeMachine(this.metaTileEntityId);
    }

    @NotNull
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCCC", "CCCCC", "CCCCC", "CCCCC", " CCC ")
                .aisle("CCCCC", "C C C", "C E C", "C C C", " CCC ")
                .aisle("CCCCC", "C   C", "C   C", "C   C", " CCC ")
                .aisle(" CSC ", " GGG ", " GGG ", " CCC ", "     ")
                .where('S', selfPredicate())
                .where('C', states(GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING.getState(BlockLargeMultiblockCasing.CasingType.NONCONDUCTING_CASING)).setMinGlobalLimited(45)
                        .or(abilities(MultiblockAbility.INPUT_ENERGY)
                                .setMinGlobalLimited(1).setMaxGlobalLimited(2))
                        .or(abilities(MultiblockAbility.MAINTENANCE_HATCH)
                                .setExactLimit(1))
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS)
                                .setMinGlobalLimited(1))
                        .or(abilities(MultiblockAbility.EXPORT_ITEMS)
                                .setMinGlobalLimited(1))
                        .or(abilities(MultiblockAbility.IMPORT_FLUIDS)
                                .setMinGlobalLimited(1))
                        .or(abilities(MultiblockAbility.EXPORT_FLUIDS)))
                .where('E', states(getElectrodeState()))
                .where('G', states(getGlassState()))
                .build();
    }

    @Override
    public List<MultiblockShapeInfo> getMatchingShapes() {
        ArrayList<MultiblockShapeInfo> shapeInfo = new ArrayList<>();
        MultiblockShapeInfo.Builder baseBuilder = MultiblockShapeInfo.builder()
                .where('S', SuSyMetaTileEntities.ELECTRIC_DISCHARGE_MACHINE, EnumFacing.SOUTH)
                .where('C', getCasingState())
                .where('E', getElectrodeState())
                .where('G', getGlassState())
                .where('F', MetaTileEntities.FLUID_IMPORT_HATCH[GTValues.LV], EnumFacing.SOUTH)
                .where('I', MetaTileEntities.ITEM_IMPORT_BUS[GTValues.LV], EnumFacing.SOUTH)
                .where('O', MetaTileEntities.ITEM_EXPORT_BUS[GTValues.LV], EnumFacing.SOUTH)
                .where('N', MetaTileEntities.ENERGY_INPUT_HATCH[GTValues.HV], EnumFacing.SOUTH)
                .where('M',
                        () -> ConfigHolder.machines.enableMaintenance ? MetaTileEntities.MAINTENANCE_HATCH :
                                getCasingState(),
                        EnumFacing.SOUTH);
        shapeInfo.add(baseBuilder.shallowCopy()
                .aisle("CCCCC", "CCCCC", "CCCCC", "CCCCC", " CCC ")
                .aisle("CCCCC", "C C C", "C E C", "C C C", " CCC ")
                .aisle("MCCCN", "F   C", "C   C", "C   C", " CCC ")
                .aisle(" ISO ", " GGG ", " GGG ", " CCC ", "     ")
                .build());
        return shapeInfo;
    }

    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        if (sourcePart instanceof IMultiblockAbilityPart<?>) {
            MultiblockAbility<?> ability = ((IMultiblockAbilityPart<?>) sourcePart).getAbility();
        }
        return GCYMTextures.NONCONDUCTING_CASING;
    }

    protected static IBlockState getCasingState() {
        return GCYMMetaBlocks.LARGE_MULTIBLOCK_CASING.getState(BlockLargeMultiblockCasing.CasingType.NONCONDUCTING_CASING);
    }

    protected static IBlockState getElectrodeState() {
        return SuSyBlocks.EDM_ELECTRODE.getState(BlockEDMElectrode.ElectrodeType.COPPER_TUNGSTEN);
    }

    protected static IBlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.LAMINATED_GLASS);
    }


    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @NotNull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.EDM_OVERLAY;
    }
}
