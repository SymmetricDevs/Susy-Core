package supersymmetry.common.metatileentities.multi.electric;

import static supersymmetry.api.metatileentity.multiblock.SuSyPredicates.hiddenGearTooth;

import javax.annotation.Nonnull;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntityAttritionScrubber extends RecipeMapMultiblockController {

    public MetaTileEntityAttritionScrubber(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.ATTRITION_SCRUBBER);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this, true);
    }

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityAttritionScrubber(this.metaTileEntityId);
    }

private static IBlockState getHydrostaticCasingState() {
        return SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.HYDROSTATIC_CASING);
    }

private static IBlockState getAluminiumGearboxState() {
        return SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.ALUMINIUM_GEARBOX);
    }


    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" CCC CCC ", " CCCCCCC ", " CCCCCCC ", " CCCCCCC ", " CCC CCC ", " FGF FGF ")
                .aisle("CCCCCCCCC", "W#B###B#S", "C###C###C", "I#B#C#B#O", "C###C###C", " FGF FGF ")
                .aisle("CCCCCCCCC", "WBBB#BBBS", "C#A#C#A#C", "IBABCBSBO", "C#A#C#A#C", " FGF FGF ")
                .aisle("CCCCCCCCC", "W#B###B#S", "C###C###C", "I#B#C#B#O", "C###C###C", " F F F F ")
                .aisle(" CCC CCC ", " CXCCCCC ", " CCCCCCC ", " CCCCCCC ", " CCC CCC ", " F F F F ")
                .where('C', states(getHydrostaticCasingState()).or(autoAbilities(
                        true, true, false,
                        false, false, false, false)))
                .where('I', abilities(MultiblockAbility.IMPORT_ITEMS).or(states(getHydrostaticCasingState())))
                .where('O', abilities(MultiblockAbility.EXPORT_ITEMS).or(states(getHydrostaticCasingState())))
                .where('W', abilities(MultiblockAbility.IMPORT_FLUIDS).or(states(getHydrostaticCasingState())))
                .where('S', abilities(MultiblockAbility.EXPORT_FLUIDS).or(states(getHydrostaticCasingState())))
                .where('G', states(getAluminiumGearboxState()))
                .where('B', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.ALUMINIUM_FROSTPROOF)))
                .where('A', frames(Materials.Aluminium))
                .where('F', frames(Materials.Steel))
                .where('X', selfPredicate())
                .where('#', air())
                .where(' ', any())
                .where('M', hiddenGearTooth(
                        RelativeDirection.UP.getRelativeFacing(getFrontFacing(), getUpwardsFacing(), false)
                                .getAxis()))
                .build();
    }
    
    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart multiblockPart) {
        return SusyTextures.HYDROSTATIC_CASING;
    }

    protected static IBlockState getCasingState() {
        return SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.HYDROSTATIC_CASING);
    }
}
