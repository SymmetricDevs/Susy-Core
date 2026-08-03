package supersymmetry.common.metatileentities.multi.electric;

import net.minecraft.util.ResourceLocation;

import org.jetbrains.annotations.NotNull;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.RecipeMapMultiblockController;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;
import supersymmetry.api.metatileentity.multiblock.SuSyPredicates;
import supersymmetry.api.recipes.SuSyRecipeMaps;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockRobotArmLayup;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntityLayupMachine extends RecipeMapMultiblockController {

    public MetaTileEntityLayupMachine(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId, SuSyRecipeMaps.LAYUP);
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle(" SGS ", "  A  ", "     ")
                .aisle("sssss", "     ", "     ")
                .aisle("sssss", "     ", "     ")
                .aisle("sssss", "     ", "     ")
                .aisle(" HCH ", " HHH ", " HHH ")
                .where('s', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STAINLESS_CLEAN)))
                .where('S', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID)))
                .where('H', states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))
                        .or(autoAbilities()))
                .where('A', SuSyPredicates.orientation(this,
                        SuSyBlocks.ROBOT_ARM_LAYUP.getState(BlockRobotArmLayup.LayupRobotArmType.LAYUP),
                        RelativeDirection.BACK, VariantHorizontalRotatableBlock.FACING))
                .where('G',
                        states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('C', selfPredicate())
                .build();
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart sourcePart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.LAYUP_OVERLAY;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityLayupMachine(this.metaTileEntityId);
    }
}
