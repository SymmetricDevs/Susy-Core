package supersymmetry.common.metatileentities.multi.electric;

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
import gregtech.common.blocks.BlockMachineCasing.MachineCasingType;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.util.ResourceLocation;
import supersymmetry.api.recipes.SuSyRecipeMaps;

import javax.annotation.Nonnull;

public class MetaTileEntityContactCooler extends RecipeMapMultiblockController {

    public MetaTileEntityContactCooler(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.CONTACT_COOLER);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityContactCooler(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("F F", "AAA", "FBF", "FBF", "FBF", "CCC")
                .aisle("   ", "AAA", "BBB", "BBB", "BBB", "CCC")
                .aisle("F F", "ASA", "FBF", "FBF", "FBF", "CCC")
                .where('S', selfPredicate())
                .where('A', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.ALUMINIUM_FROSTPROOF))
                        .or(autoAbilities(false, false, false, false, false, true, false).setExactLimit(1))
                        .or(autoAbilities(false, false, false, false, true, false, false).setExactLimit(1))
                        .or(autoAbilities(false, true, false, false, false, false, false).setExactLimit(1))
                        .or(autoAbilities(true, false, false, false, false, false, false).setMinGlobalLimited(1).setMaxGlobalLimited(3)))
                .where('B', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where('F', states(MetaBlocks.FRAMES.get(Materials.Aluminium).getBlock(Materials.Aluminium)))
                .where('C', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.ALUMINIUM_FROSTPROOF))
                        .or(autoAbilities(false, false, false, false, false, true, false).setExactLimit(1))
                        .or(autoAbilities(false, false, false, false, true, false, false).setExactLimit(1)))
                .where(' ', any())
                .where('#', air())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.FROST_PROOF_CASING;
    }

    @Nonnull
    @Override
    protected ICubeRenderer getFrontOverlay() {
        return Textures.PYROLYSE_OVEN_OVERLAY;
    }
}
