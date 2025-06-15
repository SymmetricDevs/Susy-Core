package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.RecipeMap;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntityMixerSettler extends RecipeMapMultiblockController {
    public MetaTileEntityMixerSettler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.MIXER_SETTLER_RECIPES);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {

        return FactoryBlockPattern.start()
                .aisle("ECCCCCCCE", "GGGGGGGGG", "GGGGGGGGG", "GGGGGGGGG", "ECCCCCCCE")
                .aisle("ECCCCCCCE", "G P G P G", "GFG GFG G", "IFG PFG O", "EMCCCMCCE")
                .aisle("ECCCCCCCE", "G GTG GTG", "G GTG GTG", "G GTG GTG", "ECCCCCCCE")
                .aisle("ECCCCCCCE", "GDG GDG G", "GDG GDG G", "G G G G G", "ECCCCCCCE")
                .aisle("ECCCCCCCE", "G GDG GDG", "G GDG GDG", "G G G G G", "ECCCCCCCE")
                .aisle("ECCCCCCCE", "GTG GTG G", "GTG GTG G", "GTG GTG G", "ECCCCCCCE")
                .aisle("ECCCCCCCE", "O G P G I", "G GFG GFG", "G PFG PFG", "ECCMCCCME")
                .aisle("ECCCSCCCE", "GGGGGGGGG", "GGGGSGGGG", "GGGGGGGGG", "ECCCSCCCE")
                .where('S', selfPredicate())
                .where('I', states(getGlassState()).or(abilities(MultiblockAbility.IMPORT_FLUIDS)))
                .where('O', states(getGlassState()).or(abilities(MultiblockAbility.EXPORT_FLUIDS)))
                .where('T', states(SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.SIEVE_TRAY)))
                .where('P', states(getPipeCasingState()))
                //.where('B', abilities(MultiblockAbility.IMPORT_ITEMS, MultiblockAbility.EXPORT_ITEMS))
                .where('D', states(getCasingState()))
                .where('C', states(getCasingState()).setMinGlobalLimited(110).or(autoAbilities(true, true, true, true, false, false, false)))
                .where('G', states(getGlassState()))
                .where('M', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STAINLESS_STEEL_GEARBOX)))
                .where('F', frames(Materials.StainlessSteel))
                .where(' ', air())
                .build();
    }


    protected static IBlockState getGlassState() {
        return MetaBlocks.TRANSPARENT_CASING.getState(BlockGlassCasing.CasingType.TEMPERED_GLASS);
    }

    protected static IBlockState getPipeCasingState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.POLYTETRAFLUOROETHYLENE_PIPE);
    }

    public IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.CLEAN_STAINLESS_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMixerSettler(metaTileEntityId);
    }
}
