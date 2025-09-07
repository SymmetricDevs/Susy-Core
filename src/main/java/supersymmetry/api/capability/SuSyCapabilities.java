package supersymmetry.api.capability;

import gregtech.api.capability.SimpleCapabilityManager;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Mod;
import supersymmetry.Supersymmetry;
import supersymmetry.api.particle.IParticleBeamProvider;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class SuSyCapabilities {

    @CapabilityInject(IElytraFlyingProvider.class)
    public static Capability<IElytraFlyingProvider> ELYTRA_FLYING_PROVIDER;

    @CapabilityInject(IStrandProvider.class)
    public static Capability<IStrandProvider> STRAND_PROVIDER;

    @CapabilityInject(IParticleBeamProvider.class)
    public static Capability<IParticleBeamProvider> PARTICLE_BEAM_PROVIDER;

    public static void init() {
        SimpleCapabilityManager.registerCapabilityWithNoDefault(IElytraFlyingProvider.class);
        SimpleCapabilityManager.registerCapabilityWithNoDefault(IStrandProvider.class);
        SimpleCapabilityManager.registerCapabilityWithNoDefault(IParticleBeamProvider.class);
    }
}
