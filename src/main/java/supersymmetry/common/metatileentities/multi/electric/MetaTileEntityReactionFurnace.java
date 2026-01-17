package supersymmetry.common.metatileentities.multi.electric;

import javax.annotation.Nonnull;

import net.minecraft.util.ResourceLocation;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockFireboxCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.recipes.SuSyRecipeMaps;

public class MetaTileEntityReactionFurnace extends RecipeMapMultiblockController {

    public MetaTileEntityReactionFurnace(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.REACTION_FURNACE_RECIPES);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityReactionFurnace(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("     ", "     ", " P P ", " P P ", " P P ")
                .aisle("F   F", "FBBBF", "XPXPX", "XXXXX", " P P ")
                .aisle("     ", "XBBBX", "XP#PX", "XPMPX", " P P ")
                .aisle("F   F", "FBBBF", "XXSXX", "XXXXX", "     ")
                .where('S', selfPredicate())
                .where('X',
                        states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.INVAR_HEATPROOF))
                                .setMinGlobalLimited(13)
                                .or(autoAbilities(true, true, true, true, true, true, false)))
                .where('P', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('F', frames(Materials.Invar))
                .where('M', abilities(MultiblockAbility.MUFFLER_HATCH))
                .where('B',
                        states(MetaBlocks.BOILER_FIREBOX_CASING
                                .getState(BlockFireboxCasing.FireboxCasingType.STEEL_FIREBOX)))
                .where('#', air())
                .where(' ', any())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.HEAT_PROOF_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.PYROLYSE_OVEN_OVERLAY;
    }

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }
}
