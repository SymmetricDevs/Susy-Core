package supersymmetry.common.metatileentities.multi.electric;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.capability.impl.NoEnergyMultiblockRecipeLogic;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;

public class MetaTileEntityHeatExchanger extends RecipeMapMultiblockController {

    public MetaTileEntityHeatExchanger(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.HEAT_EXCHANGER_RECIPES);
        this.recipeMapWorkable = new NoEnergyMultiblockRecipeLogic(this);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityHeatExchanger(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCC", "BCB", "ACA")
                .aisle("CCC", "CDC", "ACA")
                .aisle("CCC", "CDC", "ACA")
                .aisle("CCC", "CDC", "ACA")
                .aisle("CCC", "CDC", "ACA")
                .aisle("CCC", "CDC", "ACA")
                .aisle("CCC", "CDC", "ACA")
                .aisle("CCC", "CDC", "ACA")
                .aisle("CCC", "BSB", "ACA")
                .where('S', selfPredicate())
                .where('A', frames(Materials.Steel))
                .where('B', autoAbilities(false, false, false, false, false, true, false).setMinGlobalLimited(2)
                        .or(autoAbilities(false, false, false, false, true, false, false).setMinGlobalLimited(2)))
                .where('C', states(MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities(false, true, false, false, false, false, false))
                        .or(autoAbilities(false, false, true, false, false, false, false).setMaxGlobalLimited(1)))
                .where('D', states(MetaBlocks.BOILER_CASING.getState(BoilerCasingType.STEEL_PIPE)))
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return SusyTextures.HEAT_EXCHANGER_OVERLAY;
    }
}
