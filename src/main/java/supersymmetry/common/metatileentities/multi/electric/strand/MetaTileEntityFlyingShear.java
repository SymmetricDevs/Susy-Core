package supersymmetry.common.metatileentities.multi.electric.strand;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.pattern.BlockPattern;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.capability.Strand;

public class MetaTileEntityFlyingShear extends MetaTileEntityStrandShaper {
    public MetaTileEntityFlyingShear(ResourceLocation metaTileEntityId) {
        super(metaTileEntityId);
    }

    @Override
    public long getVoltage() {
        return 64;
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
        return null;
    }

    @Override
    public ICubeRenderer getBaseTexture(IMultiblockPart iMultiblockPart) {
        return Textures.HEAT_PROOF_CASING;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity iGregTechTileEntity) {
        return new MetaTileEntityFlyingShear(metaTileEntityId);
    }
}
