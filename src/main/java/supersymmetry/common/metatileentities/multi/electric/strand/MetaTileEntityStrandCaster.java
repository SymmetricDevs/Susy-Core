package supersymmetry.common.metatileentities.multi.electric.strand;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.*;
import net.minecraft.util.ResourceLocation;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockSheetedFrame;
import supersymmetry.common.blocks.SuSyMetaBlocks;

import javax.annotation.Nonnull;

public class MetaTileEntityStrandCaster extends RecipeMapMultiblockController {

    public MetaTileEntityStrandCaster(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.STRAND_CASTER);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityStrandCaster(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("F   ++", "F   ++", "FCCCF", "FCCCF ", " CCC  ")
                .aisle("      ", "      ", "CCCCP", "CC#CC ", "C###C ")
                .aisle("      ", "  CCCC", "CC##I", "C###C ", "C###C ")
                .aisle("      ", "      ", "CCCCP", "CC#CC ", "C###C ")
                .aisle("F   FF", "F   FF", "FCXCF", "FCCCF ", " CCC  ")
                .where('X', selfPredicate())
                //.where('H', states(UniqueCasingType.HEAT_VENT.get()))
                .where('C', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF)))
                .where('P', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('G', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('s', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID)))
                .where('S', states(SuSyMetaBlocks.SHEETED_FRAMES.get(Materials.Steel).getBlock(Materials.Steel).withProperty(BlockSheetedFrame.SHEETED_FRAME_AXIS, BlockSheetedFrame.FrameEnumAxis.fromFacingAxis(RelativeDirection.FRONT.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped()).getAxis()))))
                .where('R', states(SuSyMetaBlocks.SHEETED_FRAMES.get(Materials.Invar).getBlock(Materials.Invar).withProperty(BlockSheetedFrame.SHEETED_FRAME_AXIS, BlockSheetedFrame.FrameEnumAxis.fromFacingAxis(RelativeDirection.FRONT.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), isFlipped()).getAxis()))))
                .where('F', frames(Materials.Steel))
                .where('I', frames(Materials.Invar))
                .where('#', air())
                .where(' ', any())
                .where('O', frames(Materials.Steel)
                        .or(autoAbilities(false, false, false, true, false, false, false)))
                .where('o', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities(false, false, false, false, false, true, false)))
                .where('+',frames(Materials.Steel)
                        .or(autoAbilities(true, true, false, false, true, false, false)))

                .build();
    }
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return  Textures.HEAT_PROOF_CASING;
        // iMultiblockPart instanceof IMultiblockAbilityPart<?> && getAbilities() = MultiblockAbility.IMPORT_FLUIDS ? Textures.SOLID_STEEL_CASING:
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.PYROLYSE_OVEN_OVERLAY;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return false;
    }
    @Override
    public boolean hasMaintenanceMechanics(){
        return false;
    }

}
