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
import gregtech.common.blocks.BlockMetalCasing.MetalCasingType;
import gregtech.common.blocks.BlockBoilerCasing.BoilerCasingType;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import gregtech.api.recipes.RecipeMaps;

import javax.annotation.Nonnull;

public class MetaTileEntityGasTurbine extends RecipeMapMultiblockController {

    public MetaTileEntityGasTurbine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, RecipeMaps.GAS_TURBINE_FUELS);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityGasTurbine(metaTileEntityId);
    }

    @Override
    protected BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("AEA", "ABA", "   ")
                .aisle("AAA", "DBD", "   ")
                .aisle("AAA", "DBD", "   ")
                .aisle("AAA", "DBD", "   ")
                .aisle("AAA", "DBD", "   ")
                .aisle("AAA", "DBD", "   ")
                .aisle("AAA", "DBD", "   ")
                .aisle("AAA", "DBD", "   ")
                .aisle("AAA", "DBD", "   ")
                .aisle("AAA", "DBD", "AAA")
                .aisle("AAA", "ASA", "AAA")
                .where('S', selfPredicate())
                .where('A', states(new IBlockState[]{MetaBlocks.METAL_CASING.getState(MetalCasingType.STEEL_SOLID)}))
                .where('B', states(new IBlockState[]{MetaBlocks.BOILER_CASING.getState(BoilerCasingType.STEEL_PIPE)}))
                .where('C', states(new IBlockState[]{MetaBlocks.FRAMES.get(Materials.Steel).getBlock(Materials.Steel)}))
                .where('D', states(new IBlockState[]{MetaBlocks.BOILER_CASING.getState(BoilerCasingType.STEEL_PIPE)})
                    .or(autoAbilities(false, true, false, false, true, true, false))
                    .or(abilities(MultiblockAbility.OUTPUT_ENERGY)))
                .where('E', abilities(MultiblockAbility.MUFFLER_HATCH))
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

    @Override
    public boolean hasMufflerMechanics() {
        return true;
    }
}
