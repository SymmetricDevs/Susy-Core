package supersymmetry.common.metatileentities.multi.electric;

import gregtech.api.capability.impl.MultiblockRecipeLogic;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.recipes.Recipe;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.client.utils.TooltipHelper;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockTurbineCasing.TurbineCasingType;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.api.recipes.properties.BiomeProperty;
import supersymmetry.client.renderer.textures.SusyTextures;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static gregtech.api.util.RelativeDirection.FRONT;
import static gregtech.api.util.RelativeDirection.RIGHT;

public class MetaTileEntityLargeFluidPump extends RecipeMapMultiblockController {

    public MetaTileEntityLargeFluidPump(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.PUMPING_RECIPES);
        this.recipeMapWorkable = new LargePumpRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLargeFluidPump(this.metaTileEntityId);
    }

    @Override
    public boolean isMultiblockPartWeatherResistant(@Nonnull IMultiblockPart part) {
        return true;
    }

    @Override
    public boolean getIsWeatherOrTerrainResistant() {
        return true;
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @NotNull
    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start(RIGHT, FRONT, RelativeDirection.UP)
                .aisle("       ", "      P", "       ")
                .aisle("       ", "      P", "       ")
                .aisle("FCCCC  ", "CCCCC P", "FCECC  ")
                .aisle("CCSGC  ", "OPPPPPP", "CCEGC  ")
                .aisle("FCCC   ", "CCCCC  ", "FCEC   ")
                .where(' ', any())
                .where('S', selfPredicate())
                .where('P', states(getPipeCasingState()))
                .where('G', states(getGearboxState()))
                .where('F', frames(Materials.Steel))
                .where('C', states(getCasingState())
                        .or(abilities(MultiblockAbility.IMPORT_ITEMS).setMinGlobalLimited(1).setMaxGlobalLimited(1))
                        .or(autoAbilities(true, false)))
                .where('E', states(getCasingState())
                        .or(abilities(MultiblockAbility.INPUT_ENERGY)).setMinGlobalLimited(1).setMaxGlobalLimited(2))
                .where('O', abilities(MultiblockAbility.EXPORT_FLUIDS))
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    protected static IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID);
    }

    protected static IBlockState getPipeCasingState() {
        return MetaBlocks.BOILER_CASING.getState(BoilerCasingType.STEEL_PIPE);
    }

    protected static IBlockState getGearboxState() {
        return MetaBlocks.TURBINE_CASING.getState(TurbineCasingType.STEEL_GEARBOX);
    }

    @Override
    public void addInformation(ItemStack stack, @Nullable World player, @NotNull List<String> tooltip, boolean advanced) {
        tooltip.add(I18n.format("gregtech.machine.large_fluid_pump.tooltip.1"));
        tooltip.add(I18n.format("gregtech.machine.large_fluid_pump.tooltip.2"));
        super.addInformation(stack, player, tooltip, advanced);
        tooltip.add(TooltipHelper.RAINBOW_SLOW + I18n.format("gregtech.machine.perfect_oc", new Object[0]));
    }
    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
        EnumFacing leftSide = RelativeDirection.LEFT
                .getRelativeFacing(
                        getFrontFacing(),
                        getUpwardsFacing(),
                        isFlipped()
                );
        EnumFacing backSide = RelativeDirection.BACK
                .getRelativeFacing(
                        getFrontFacing(),
                        getUpwardsFacing(),
                        isFlipped()
                );
        BlockPos tempPos = getPos().offset(leftSide, 4).offset(backSide);
        int yLevel = getPos().getY();
        String biome = getWorld().getBiome(tempPos).biomeName;
        textList.add(new TextComponentTranslation("susy.large_fluid_pump.y_level", yLevel).setStyle(new Style().setColor(TextFormatting.YELLOW)));
        textList.add(new TextComponentTranslation("susy.large_fluid_pump.biome", biome).setStyle(new Style().setColor(TextFormatting.YELLOW)));
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.OCEANIC_DRILL_OVERLAY;
    }

    /**
     * A custom recipeLogic class, for adding our check for biomes
     * This can be moved out to a stand-alone class.
     * But generally speaking if you do not plan to re-use this, making it an inner class should be fine.
     * CEu itself has many such cases.
     */
    public static class LargePumpRecipeLogic extends MultiblockRecipeLogic {

        public LargePumpRecipeLogic(RecipeMapMultiblockController tileEntity) {
            super(tileEntity, true);
        }

        /**
         * Overriding this to add our own custom checks
         * Don't forget super calls
         */
        @Override
        public boolean checkRecipe(@NotNull Recipe recipe) {
            return checkHeightRequirement() && checkBiomeRequirement(recipe) && super.checkRecipe(recipe);
        }

        public boolean checkHeightRequirement() {
            return getMetaTileEntity().getPos().getY() == 64;
        }

        /**
         * This is a method for biome checking
         */
        public boolean checkBiomeRequirement(@NotNull Recipe recipe) {
            if (!recipe.hasProperty(BiomeProperty.getInstance())) return true;
            EnumFacing leftSide = RelativeDirection.LEFT
                    .getRelativeFacing(
                            getMetaTileEntity().getFrontFacing(),
                            ((MultiblockControllerBase) getMetaTileEntity()).getUpwardsFacing(),
                            ((MultiblockControllerBase) getMetaTileEntity()).isFlipped()
                    );
            EnumFacing backSide = RelativeDirection.BACK
                    .getRelativeFacing(
                            getMetaTileEntity().getFrontFacing(),
                            ((MultiblockControllerBase) getMetaTileEntity()).getUpwardsFacing(),
                            ((MultiblockControllerBase) getMetaTileEntity()).isFlipped()
                    );
            BlockPos tempPos = getMetaTileEntity().getPos().offset(leftSide, 4).offset(backSide);
            return recipe.getProperty(BiomeProperty.getInstance(), BiomeProperty.BiomePropertyList.EMPTY_LIST)
                    .checkBiome(getMetaTileEntity().getWorld().getBiome(tempPos));
        }

        @Override
        public int getParallelLimit() {
            return 256;
        }
    }

}
