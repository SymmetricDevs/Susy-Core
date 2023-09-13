package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
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
import gregtech.common.blocks.BlockMachineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import org.jetbrains.annotations.NotNull;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockSinteringBrick;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.recipes.properties.SinterProperty;
import supersymmetry.common.blocks.BlockSuSyMultiblockCasing;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import java.util.List;

public class MetaTileEntitySinteringOven extends RecipeMapMultiblockController {

    private boolean canUsePlasma;

    public MetaTileEntitySinteringOven(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.SINTERING_RECIPES);
        this.recipeMapWorkable = new MultiblockRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntitySinteringOven(metaTileEntityId);
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        if (isStructureFormed()) {
            textList.add(new TextComponentTranslation("gregtech.multiblock.sintering_oven.can_use_plasma",
                    new TextComponentTranslation(canUsePlasma ? "gregtech.multiblocks.sintering_oven.use_plasma.affirmative" : "gregtech.multiblocks.sintering_oven.use_plasma.negative")
                            .setStyle(new Style().setColor(TextFormatting.LIGHT_PURPLE))));
        }
        super.addDisplayText(textList);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart multiblockPart) {
        return SusyTextures.ULV_STRUCTURAL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.SINTERING_OVERLAY;
    }

    protected IBlockState getCasingState() {
        return SuSyBlocks.MULTIBLOCK_CASING.getState(BlockSuSyMultiblockCasing.CasingType.ULV_STRUCTURAL_CASING);
    }

    protected IBlockState getFrameState() {
        return MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel);
    }

    @NotNull
    @Override
    public BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCCC", "CCCCC", "CCCCC", "CCCCC", "CCCCC")
                .aisle("     ", " BBB ", " B#B ", " BBB ", "     ")
                .aisle("FFFFF", "FBBBF", "FB#BF", " BBB ", "     ")
                .aisle("     ", " BBB ", " B#B ", " BBB ", "     ")
                .aisle("FFFFF", "FBBBF", "FB#BF", " BBB ", "     ")
                .aisle("     ", " BBB ", " B#B ", " BBB ", "     ")
                .aisle("FFFFF", "FBBBF", "FB#BF", " BBB ", "     ")
                .aisle("     ", " BBB ", " B#B ", " BBB ", "     ")
                .aisle("FFFFF", "FBBBF", "FB#BF", " BBB ", "     ")
                .aisle("     ", " BBB ", " B#B ", " BBB ", "     ")
                .aisle("DDDDD", "DDSDD", "DDDDD", "DDDDD", "DDDDD")
                .where('S', selfPredicate())
                .where('D', states(getCasingState()).or(autoAbilities(true, true, false, true, true, false, false)))
                .where('C', states(getCasingState()).or(autoAbilities(false, false, true, false, false, true, false)))
                .where('F', states(getFrameState()))
                .where('B', SuSyPredicates.sinteringBricks())
                .where('#', air())
                .where(' ', any())
                .build();
    }

    @Override
    protected void formStructure(PatternMatchContext context) {
        super.formStructure(context);
        Object type = context.get("SinteringBrickType");
        if (type instanceof BlockSinteringBrick.SinteringBrickType) {
            this.canUsePlasma = ((BlockSinteringBrick.SinteringBrickType) type).canResistPlasma;
        } else {
            this.canUsePlasma = false;
        }
    }

    @Override
    public void invalidateStructure() {
        super.invalidateStructure();
        this.canUsePlasma = false;
    }

    @Override
    public boolean checkRecipe(@NotNull Recipe recipe, boolean consumeIfSuccess) {
        return this.canUsePlasma || !(recipe.getProperty(SinterProperty.getInstance(), false));
    }

}
