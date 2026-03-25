package supersymmetry.common.metatileentities.multiblockpart;

import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;

import gregtech.api.gui.ModularUI;
import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import supersymmetry.api.capability.SuSyCapabilities;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.api.particle.IParticleBeamProvider;
import supersymmetry.api.particle.ParticleBeam;

public class MetaTileEntityBeamLineHatch extends MetaTileEntityMultiblockPart implements IParticleBeamProvider,
                                         IMultiblockAbilityPart<IParticleBeamProvider> {

    private ParticleBeam particleBeam;
    private boolean isExport;

    public MetaTileEntityBeamLineHatch(ResourceLocation metaTileEntityId, boolean isExport) {
        super(metaTileEntityId, 4);
        this.isExport = isExport;
    }

    @Override
    public MetaTileEntity createMetaTileEntity(IGregTechTileEntity tileEntity) {
        return new MetaTileEntityBeamLineHatch(metaTileEntityId, isExport);
    }

    @Override
    protected ModularUI createUI(EntityPlayer entityPlayer) {
        return null;
    }

    @Override
    protected boolean openGUIOnRightClick() {
        return false;
    }

    @Override
    public MultiblockAbility<IParticleBeamProvider> getAbility() {
        return isExport ? SuSyMultiblockAbilities.BEAM_EXPORT : SuSyMultiblockAbilities.BEAM_IMPORT;
    }

    @Override
    public void registerAbilities(List<IParticleBeamProvider> abilityList) {
        abilityList.add(this);
    }

    @Override
    public ParticleBeam getParticleBeam() {
        return this.particleBeam;
    }

    @Override
    public ParticleBeam insertBeam(ParticleBeam beam) {
        if (this.particleBeam != null) {
            return beam;
        }
        this.particleBeam = beam;
        return null;
    }

    @Override
    public <T> T getCapability(Capability<T> capability, EnumFacing side) {
        if (capability == SuSyCapabilities.PARTICLE_BEAM_PROVIDER) {
            return SuSyCapabilities.PARTICLE_BEAM_PROVIDER.cast(this);
        }
        return super.getCapability(capability, side);
    }
}
