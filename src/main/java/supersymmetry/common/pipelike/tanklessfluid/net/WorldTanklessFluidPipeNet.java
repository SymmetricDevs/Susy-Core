package supersymmetry.common.pipelike.tanklessfluid.net;

import net.minecraft.world.World;

import org.jspecify.annotations.NullMarked;

import gregtech.api.pipenet.WorldPipeNet;
import supersymmetry.api.unification.material.properties.TanklessFluidPipeProperties;

@NullMarked
public class WorldTanklessFluidPipeNet extends WorldPipeNet<TanklessFluidPipeProperties, TanklessFluidPipeNet> {

    private static final String DATA_ID = "susy.tankless_fluid_pipe_net";

    public static WorldTanklessFluidPipeNet getWorldPipeNet(World world) {
        var netWorldData = (WorldTanklessFluidPipeNet) world.loadData(WorldTanklessFluidPipeNet.class, DATA_ID);
        if (netWorldData == null) {
            netWorldData = new WorldTanklessFluidPipeNet(DATA_ID);
            world.setData(DATA_ID, netWorldData);
        }
        netWorldData.setWorldAndInit(world);
        return netWorldData;
    }

    public WorldTanklessFluidPipeNet(String name) {
        super(name);
    }

    @Override
    protected TanklessFluidPipeNet createNetInstance() {
        return new TanklessFluidPipeNet(this);
    }
}
