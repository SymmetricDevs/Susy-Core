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
import gregtech.common.blocks.BlockBoilerCasing;
import gregtech.common.blocks.BlockMetalCasing;
import gregtech.common.blocks.BlockTurbineCasing;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.Strand;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.client.renderer.textures.SusyTextures;

import java.util.List;

public class MetaTileEntityClusterMill extends MetaTileEntityRollingMill {
    public MetaTileEntityClusterMill(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityClusterMill(metaTileEntityId);
    }

    @Override
    public long getVoltage() {
        return 96;
    }


    @Override
    protected boolean consumeInputsAndSetupRecipe() {
        Strand orig = this.input.take();
        if (orig == null) return false;
        maxProgress = (int) Math.ceil(1 / (8.0 * orig.thickness));
        return true;
    }

    @Override
    protected Strand resultingStrand() {
        if (this.input.getStrand() == null || this.input.getStrand().isCut) return null;
        Strand str = new Strand(this.input.getStrand());
        // t / (2 - e^(-8t)) is a pretty good function for balancing
        double scaling = 2 - Math.pow(Math.E, -8 * str.thickness);
        str.thickness /= scaling;
        str.width *= scaling;
        return str;
    }

    @Override
    protected @NotNull BlockPattern createStructurePattern() {
        return FactoryBlockPattern.start()
                .aisle("  P P  ", "  P P  ", "  P P  ", "CCGCGCC", "F P P F", "  P P  ", "  P P  ",  "  P P  ", "  P P  ")
                .aisle("  P P  ", "  h h  ", "  R R  ", "RRRRRRR", "F  A  F", "   R   ", "  R R  ",  "  H H  ", "  P P  ")
                .aisle("  P P  ", "  h h  ", "  R R  ", "RRRRRRR", "I  A  O", "   R   ", "  R R  ",  "  H H  ", "  P P  ")
                .aisle("  P P  ", "  h h  ", "  R R  ", "RRRRRRR", "F  A  F", "   R   ", "  R R  ",  "  H H  ", "  P P  ")
                .aisle("  P P  ", "  P P  ", "  P P  ", "CCGSGCC", "F P P F", "  P P  ", "  P P  ",  "  P P  ", "  P P  ")
                .where('R', rollOrientation(RelativeDirection.FRONT))
                .where('H', hydraulicOrientation(RelativeDirection.UP))
                .where('h', hydraulicOrientation(RelativeDirection.DOWN))
                .where('F', frames(Materials.Steel))
                .where('S', selfPredicate())
                .where('I', abilities(SuSyMultiblockAbilities.STRAND_IMPORT))
                .where('O', abilities(SuSyMultiblockAbilities.STRAND_EXPORT))
                .where('C', autoAbilities().or(states(MetaBlocks.METAL_CASING.getState(BlockMetalCasing.MetalCasingType.STEEL_SOLID))))
                .where('G', states(MetaBlocks.TURBINE_CASING.getState(BlockTurbineCasing.TurbineCasingType.STEEL_GEARBOX)))
                .where('P', states(MetaBlocks.BOILER_CASING.getState(BlockBoilerCasing.BoilerCasingType.STEEL_PIPE)))
                .where(' ', any())
                .where('A', air())
                .build();
    }

    @Override
    protected void addDisplayText(List<ITextComponent> textList) {
        super.addDisplayText(textList);
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.SOLID_STEEL_CASING;
    }

    @Override
    protected @NotNull ICubeRenderer getFrontOverlay() {
        return SusyTextures.CLUSTER_MILL_OVERLAY;
    }
}
