package supersymmetry.common.pipelike.tanklessfluid.net;

import java.util.*;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import gregtech.api.pipenet.Node;
import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import supersymmetry.api.unification.material.properties.TanklessFluidPipeProperties;

public class TanklessFluidPipeNet extends PipeNet<TanklessFluidPipeProperties> {

    private final Map<BlockPos, List<FluidRoutePath>> NET_DATA = new HashMap<>();

    public TanklessFluidPipeNet(WorldPipeNet<TanklessFluidPipeProperties, ? extends PipeNet<TanklessFluidPipeProperties>> world) {
        super(world);
    }

    public List<FluidRoutePath> getNetData(BlockPos pipePos, EnumFacing facing) {
        var data = NET_DATA.get(pipePos);
        if (data == null) {
            data = TanklessFluidNetWalker.createNetData(getWorldData(), pipePos, facing);
            if (data == null) {
                // walker failed, don't cache so it tries again on next insertion
                return Collections.emptyList();
            }
            data.sort(Comparator.comparingInt(path -> path.getProperties().getResistance()));
            NET_DATA.put(pipePos, data);
        }
        return data;
    }

    @Override
    public void onNeighbourUpdate(BlockPos fromPos) {
        NET_DATA.clear();
    }

    @Override
    public void onPipeConnectionsUpdate() {
        NET_DATA.clear();
    }

    @Override
    public void onChunkUnload() {
        NET_DATA.clear();
    }

    @Override
    protected void transferNodeData(Map<BlockPos, Node<TanklessFluidPipeProperties>> transferredNodes,
                                    PipeNet<TanklessFluidPipeProperties> parentNet) {
        super.transferNodeData(transferredNodes, parentNet);
        NET_DATA.clear();
        ((TanklessFluidPipeNet) parentNet).NET_DATA.clear();
    }

    @Override
    protected void writeNodeData(TanklessFluidPipeProperties nodeData, NBTTagCompound tagCompound) {
        tagCompound.setInteger("max_temperature", nodeData.getMaxFluidTemperature());
        tagCompound.setInteger("throughput", nodeData.getThroughput());
        tagCompound.setBoolean("gas_proof", nodeData.isGasProof());
        tagCompound.setBoolean("acid_proof", nodeData.isAcidProof());
        tagCompound.setBoolean("cryo_proof", nodeData.isCryoProof());
        tagCompound.setBoolean("plasma_proof", nodeData.isPlasmaProof());
        tagCompound.setInteger("resistance", nodeData.getResistance());
    }

    @Override
    protected TanklessFluidPipeProperties readNodeData(NBTTagCompound tagCompound) {
        int maxTemperature = tagCompound.getInteger("max_temperature");
        int throughput = tagCompound.getInteger("throughput");
        boolean gasProof = tagCompound.getBoolean("gas_proof");
        boolean acidProof = tagCompound.getBoolean("acid_proof");
        boolean cryoProof = tagCompound.getBoolean("cryo_proof");
        boolean plasmaProof = tagCompound.getBoolean("plasma_proof");
        int resistance = tagCompound.getInteger("resistance");
        return new TanklessFluidPipeProperties(maxTemperature, throughput, gasProof, acidProof, cryoProof,
                plasmaProof, resistance);
    }
}
