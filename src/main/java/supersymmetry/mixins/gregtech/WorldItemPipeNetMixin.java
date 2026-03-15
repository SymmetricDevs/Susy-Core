package supersymmetry.mixins.gregtech;

import java.util.List;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import gregtech.api.pipenet.WorldPipeNet;
import gregtech.common.pipelike.itempipe.net.ItemPipeNet;
import gregtech.common.pipelike.itempipe.net.WorldItemPipeNet;

class WorldItemPipeNetAccessor extends WorldItemPipeNet {

    WorldItemPipeNetAccessor(String name) { // here because java forces me to put it
        super(name);
    }

    List<ItemPipeNet> getPipeNets() {
        return this.pipeNets;
    }

    void callAddPipeNet(ItemPipeNet net) {
        this.addPipeNet(net);
    }
}

@Mixin(value = WorldItemPipeNet.class, remap = false)
public abstract class WorldItemPipeNetMixin {

    @Overwrite
    /**
     * @author aliu-here
     * @reason couldn't figure out a way to fix the original through a regular mixin; i just copied source w/ added line
     */
    public static WorldItemPipeNet getWorldPipeNet(World world) {
        String DATA_ID = WorldPipeNet.getDataID("gregtech.item_pipe_net", world);
        WorldItemPipeNet netWorldData = (WorldItemPipeNet) world.loadData(WorldItemPipeNet.class, DATA_ID);
        if (netWorldData == null) {
            // try loading from the old one; going to save to the new one later
            netWorldData = (WorldItemPipeNet) world.loadData(WorldItemPipeNet.class, "gregtech.item_pipe_net");

            WorldItemPipeNet newNetWorldData = new WorldItemPipeNet(DATA_ID);

            for (ItemPipeNet net : ((WorldItemPipeNetAccessor) netWorldData).getPipeNets()) {
                ((WorldItemPipeNetAccessor) newNetWorldData).callAddPipeNet(net);
            }

            if (netWorldData == null) {
                netWorldData = new WorldItemPipeNet(DATA_ID);
                world.setData(DATA_ID, netWorldData);
            }
        }
        ((WorldPipeNetAccessor) netWorldData).callSetWorldAndInit(world);
        return netWorldData;
    }
}
