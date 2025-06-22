package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.recipes.Recipe;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import gregtech.common.blocks.StoneVariantBlock;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.recipes.properties.DimensionProperty;

import javax.annotation.Nonnull;

/** @author h3tR / RMI
 */

public class MetaTileEntityQuarry extends RecipeMapMultiblockController {

    public MetaTileEntityQuarry(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.QUARRY_RECIPES);
    }
    @Override

    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityQuarry(this.metaTileEntityId);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {

        return FactoryBlockPattern.start()
                .aisle("CCCCCFFFFFCCCCC", " CC    F    CC ", "       F       ", "       F       ", "       F       ")
                .aisle("CFS         SFC", "CFS         SFC", " FS         SF ", " FS         SF ", " FSFFFFFFFFFSF ")
                .aisle("CS           SC", "CS           SC", " S           S ", " S           S ", " S           S ")
                .aisle("C             C", "               ", "               ", "               ", " F           F ")
                .aisle("C             C", "               ", "               ", "               ", " F           F ")
                .aisle("F             F", "               ", "               ", "               ", " F           F ")
                .aisle("AG           GA", "SS           SS", "               ", "               ", " F           F ")
                .aisle("FG           GG", " G           G ", " F           F ", " F           F ", " F           F ")
                .aisle("AG           GA", "SS           SS", "               ", "               ", " F           F ")
                .aisle("F             F", "               ", "               ", "               ", " F           F ")
                .aisle("C             C", "               ", "               ", "               ", " F           F ")
                .aisle("C             C", "               ", "               ", "               ", " F           F ")
                .aisle("CS           SC", "CS           SC", " S           S ", " S           S ", " S           S ")
                .aisle("CFS         SFC", "CFS         SFC", " FS         SF ", " FS         SF ", " FSFFFFFFFFFSF ")
                .aisle("CCCCCFAAAFCCCCC", " CC   AMA   CC ", "               ", "               ", "               ")
                .where('M', selfPredicate())
                .where('A', states(getCasingState())
                        .or(autoAbilities(true, true, true, true, false, false, false)))
                .where('S', states(getCasingState()))
                .where('G', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID)))
                .where('C', states(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneVariant.SMOOTH).getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT)))
                .where('F', frames(Materials.Steel))
                .where('G', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .build();
    }


    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.CHUNK_MINER_OVERLAY; //TODO: custom texture
    }

    @Override
    public boolean isMultiblockPartWeatherResistant(@Nonnull IMultiblockPart part) {
        return true;
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
    }


    @Override
    public boolean checkRecipe(@NotNull Recipe recipe, boolean consumeIfSuccess) {
        for (int dimension : recipe.getProperty(DimensionProperty.getInstance(), IntLists.EMPTY_LIST))
            if (dimension == this.getWorld().provider.getDimension())
                return super.checkRecipe(recipe, consumeIfSuccess);
        return false;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }
}

