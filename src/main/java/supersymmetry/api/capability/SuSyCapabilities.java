package supersymmetry.api.capability;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.Mod;

import gregtech.api.capability.SimpleCapabilityManager;
import supersymmetry.Supersymmetry;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public class SuSyCapabilities {

    @CapabilityInject(IElytraFlyingProvider.class)
    public static Capability<IElytraFlyingProvider> ELYTRA_FLYING_PROVIDER;

    @CapabilityInject(IStrandProvider.class)
    public static Capability<IStrandProvider> STRAND_PROVIDER;

    public static void init() {
        SimpleCapabilityManager.registerCapabilityWithNoDefault(IElytraFlyingProvider.class);
        SimpleCapabilityManager.registerCapabilityWithNoDefault(IStrandProvider.class);
    }
}
