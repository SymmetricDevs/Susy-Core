package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import supersymmetry.api.recipes.SuSyRecipeMaps;

public class MetaTileEntityScrapRecycler extends RecipeMapMultiblockController {
    public MetaTileEntityScrapRecycler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.SCRAP_RECYCLER);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this, true);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityScrapRecycler(this.metaTileEntityId);
    }

    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" CCC ", "CCCCC", "COOOC", "CCCCC", " CCC ")
                .aisle(" CCC ", "PAAAP", "PAAAP", "PAAAP", " CDC ")
                .aisle(" CCC ", "PAAAP", "PAAAP", "PAAAP", " CDC ")
                .aisle(" CCC ", "PAAAP", "PAAAP", "PAAAP", " CDC ")
                .aisle(" CCC ", "PAAAP", "PAAAP", "PAAAP", " CDC ")
                .aisle(" CCC ", "PAAAP", "PAAAP", "PAAAP", " CDC ")
                .aisle(" CCC ", "CISIC", "CCCCC", "PAAAP", " CCC ")
                .where(' ', any())
                .where('A', air())
                .where('S', this.selfPredicate())
                .where('P', states(getPipeCasingState()))
                .where('C', states(getCasingState()))
                .where('I', states(getCasingState()).or(this.autoAbilities(false, true, true, false, false, false, false)))
                .where('O', states(getCasingState()).or(this.autoAbilities(false, true, false, true, false, false, false)))
                .where('D', states(getCasingState()).or(this.autoAbilities(true, true, false, false, false, false, false)))
                .build();
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.TITANIUM_STABLE);
    }
    protected static IBlockState getPipeCasingState() {
        return MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.TITANIUM_PIPE);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.STABLE_TITANIUM_CASING;
    }

}
