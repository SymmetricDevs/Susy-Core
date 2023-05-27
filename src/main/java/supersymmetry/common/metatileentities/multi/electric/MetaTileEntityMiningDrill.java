package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.capability.impl.NotifiableItemStackHandler;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.pattern.PatternMatchContext;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.Recipe;
import gregtech.api.util.GTUtility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.*;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.StoneVariantBlock;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.IItemHandlerModifiable;
import gregtech.api.unification.material.Materials;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.common.blocks.BlockDrillHead;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;

public class MetaTileEntityMiningDrill extends RecipeMapMultiblockController {
    protected BlockPos targetBlock = null;
    public MetaTileEntityMiningDrill(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.MINING_DRILL_RECIPES);
        this.recipeMapWorkable = new IndustrialDrillWorkableHandler(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityMiningDrill(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("               ", "     DDDDD     ", "     DDDDD     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("               ", "   DDDDDDDDD   ", "   DDDDDDDDD   ", "    BB   BB    ", "    BB   BB    ", "    BB   BB    ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("               ", "  DDDDDDDDDDD  ", "  DDDDDDDDDDD  ", "    BB   BB    ", "    BB   BB    ", "    BB   BB    ", "     BB BB     ", "     BB BB     ", "     BB BB     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("               ", " DDDDDDDDDDDDD ", " DDDDDDDDDDDDD ", "               ", "               ", "               ", "     BB BB     ", "     BB BB     ", "     BB BB     ", "     BB BB     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("               ", " DDDDDDDDDDDDD ", " DDDDDDDDDDDDD ", " BB         BB ", " BB         BB ", " BB         BB ", "               ", "       E       ", "     BBEBB     ", "     AAEAA     ", "       E       ", "       E       ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("               ", "DDDDDD   DDDDDD", "DDDDDD   DDDDDD", " BB         BB ", " BB         BB ", " BB         BB ", "  BB       BB  ", "  BB   E   BB  ", "  BBBAAAAABBB  ", "   BAAACAAAB   ", "     GGCGG     ", "     BGEGB     ", "     BGAGB     ", "     B   B     ", "     B   B     ", "     BB  B     ", "     B B B     ", "     B  BB     ", "     B   B     ")
                .aisle("               ", "DDDDD     DDDDD", "DDDDD     DDDDD", "      AAA      ", "      BAB      ", "      BAB      ", "  BB  BAB  BB  ", "  BB  BAB  BB  ", "  BBBAAAAABBB  ", "   BAACCCAAB   ", "     GCCCG     ", "     GCCCG     ", "     GGCGG     ", "               ", "               ", "         B     ", "      ACA      ", "     BAAA      ", "               ")
                .aisle("       H       ", "DDDDD  F  DDDDD", "DDDDD  C  DDDDD", "      ACA      ", "      ACA      ", "      ACA      ", "      ACA      ", "    EEACAEE    ", "    EAACAAE    ", "    ECCCCCE    ", "    ECCCCCE    ", "    EECCCEE    ", "     AGGGA     ", "       C       ", "       C       ", "       C       ", "     BCCCB     ", "      AAA      ", "               ")
                .aisle("               ", "DDDDD     DDDDD", "DDDDD     DDDDD", "      AAA      ", "      BSB      ", "      BAB      ", "  BB  BAB  BB  ", "  BB  BAB  BB  ", "  BBBAAAAABBB  ", "   BAACCCAAB   ", "     GCCCG     ", "     GCCCG     ", "     GGCGG     ", "               ", "               ", "     B         ", "      ACA      ", "      AAAB     ", "               ")
                .aisle("               ", "DDDDDD   DDDDDD", "DDDDDD   DDDDDD", " BB         BB ", " BB         BB ", " BB         BB ", "  BB       BB  ", "  BB   E   BB  ", "  BBBAAAAABBB  ", "   BAAACAAAB   ", "     GGCGG     ", "     BGEGB     ", "     BGAGB     ", "     B   B     ", "     B   B     ", "     B  BB     ", "     B B B     ", "     BB  B     ", "     B   B     ")
                .aisle("               ", " DDDDDDDDDDDDD ", " DDDDDDDDDDDDD ", " BB         BB ", " BB         BB ", " BB         BB ", "               ", "       E       ", "     BBEBB     ", "     AAEAA     ", "       E       ", "       E       ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("               ", " DDDDDDDDDDDDD ", " DDDDDDDDDDDDD ", "               ", "               ", "               ", "     BB BB     ", "     BB BB     ", "     BB BB     ", "     BB BB     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("               ", "  DDDDDDDDDDD  ", "  DDDDDDDDDDD  ", "    BB   BB    ", "    BB   BB    ", "    BB   BB    ", "     BB BB     ", "     BB BB     ", "     BB BB     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("               ", "   DDDDDDDDD   ", "   DDDDDDDDD   ", "    BB   BB    ", "    BB   BB    ", "    BB   BB    ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .aisle("               ", "     DDDDD     ", "     DDDDD     ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ", "               ")
                .where('S', selfPredicate())
                .where('A', states(new IBlockState[]{MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID)}))
                .where('B', states(new IBlockState[]{MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel)})
                        .or(autoAbilities(true, true, true, true, true, true, false)))
                .where('C', states(new IBlockState[]{MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)}))
                .where('D', states(MetaBlocks.STONE_BLOCKS.get(StoneVariantBlock.StoneType.CONCRETE_LIGHT).getState(StoneVariantBlock.StoneType.CONCRETE_LIGHT)))
                .where('E', states(new IBlockState[]{MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)}))
                .where('F', states(new IBlockState[]{SuSyBlocks.DRILL_HEAD.getState(BlockDrillHead.DrillHeadType.STEEL)}))
                .where('G', states(new IBlockState[]{MetaBlocks.MULTIBLOCK_CASING.getState(BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING)}))
                .where('H', depositPredicate())
                .where(' ', any())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.BLAST_FURNACE_OVERLAY;
    }

    @Nonnull
    protected TraceabilityPredicate depositPredicate() {
        return new TraceabilityPredicate(blockWorldState -> {
            this.targetBlock = blockWorldState.getPos();
            if (this.isStructureFormed()) {
                this.inputInventory.setStackInSlot(0, GTUtility.toItem(getWorld().getBlockState(targetBlock)));
            }
            return true;
        });
    }

    @Override
    protected void initializeAbilities() {
        super.initializeAbilities();
        this.inputInventory = new NotifiableItemStackHandler(1, this, false);
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        if (this.targetBlock != null) {
            this.inputInventory.setStackInSlot(0, GTUtility.toItem(getWorld().getBlockState(targetBlock)));
        }
    }

    @Override
    public void invalidateStructure() {
        this.inputInventory.setStackInSlot(0, ItemStack.EMPTY);
        this.targetBlock = null;
        super.invalidateStructure();
    }

    @Override
    public boolean canBeDistinct() {
        return false;
    }

    protected static class IndustrialDrillWorkableHandler extends MultiblockRecipeLogic {

        public IndustrialDrillWorkableHandler(RecipeMapMultiblockController tileEntity) {
            super(tileEntity);
        }

        @Override
        public MetaTileEntityMiningDrill getMetaTileEntity() {
            return (MetaTileEntityMiningDrill) super.getMetaTileEntity();
        }

        @Override
        protected boolean setupAndConsumeRecipeInputs(Recipe recipe, IItemHandlerModifiable importInventory) {
            boolean result = super.setupAndConsumeRecipeInputs(recipe, importInventory);

            // break the block in world if it is consumable
            if (result && !recipe.getInputs().get(0).isNonConsumable()) {
                MetaTileEntityMiningDrill drill = getMetaTileEntity();
                if (drill != null) {
                    drill.getWorld().destroyBlock(drill.targetBlock, false);
                }
            }

            return result;
        }
    }
}
