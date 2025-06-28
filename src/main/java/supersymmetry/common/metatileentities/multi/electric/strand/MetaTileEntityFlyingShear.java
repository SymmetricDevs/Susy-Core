package supersymmetry.common.metatileentities.multi.electric.strand;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.BlockPattern;
import gregtech.api.pattern.FactoryBlockPattern;
import gregtech.api.unification.material.Materials;
import gregtech.api.util.RelativeDirection;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.blocks.VariantHorizontalRotatableBlock;
import supersymmetry.api.capability.Strand;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.blocks.BlockMetallurgy2;
import supersymmetry.common.blocks.SuSyBlocks;

public class MetaTileEntityFlyingShear extends MetaTileEntityStrandShaper {
    public MetaTileEntityFlyingShear(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    protected boolean consumeInputsAndSetupRecipe() {
        Strand orig = this.input.take();
        if (orig == null) return false;
        this.progress = 10;
        return true;
    }

    @Override
    protected Strand resultingStrand() {
        if (this.input.getStrand() == null) return null;
        Strand str = new Strand(this.input.getStrand());
        str.isCut = true;
        return str;
    }


    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("CCCCCCCCC", "FFFFGFFFF", "    F    ", "    G    ")
                .aisle("CRRRRRRRC", "F   A   F", "    X    ", "    F    ")
                .aisle("CRRRRRRRC", "I   A   O", "    X    ", "    F    ")
                .aisle("CRRRRRRRC", "F   A   F", "    X    ", "    F    ")
                .aisle("CCCCSCCCC", "FFFFGFFFF", "    F    ", "    G    ")
                .where('S', this.selfPredicate())
                .where('A', air())
                .where('C', states(getCasingState()).or(autoAbilities()))
                .where('I', abilities(SuSyMultiblockAbilities.STRAND_IMPORT))
                .where('O', abilities(SuSyMultiblockAbilities.STRAND_EXPORT))
                .where('X', orientation(getSawbladeState(), RelativeDirection.RIGHT, VariantHorizontalRotatableBlock.FACING))
                .where('F', frames(Materials.Steel))
                .where('R', rollOrientation(RelativeDirection.FRONT))
                .where('G', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where(' ', any())
                .build();
    }

    private IBlockState getSawbladeState() {
        return SuSyBlocks.METALLURGY_2.getState(BlockMetallurgy2.BlockMetallurgy2Type.FLYING_SHEAR_SAW);
    }

    private IBlockState getCasingState() {
        return MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID);
    }

    @Override
    public boolean allowsExtendedFacing() {
        return false;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityFlyingShear(metaTileEntityId);
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.FLYING_SHEAR_OVERLAY;
    }
}
