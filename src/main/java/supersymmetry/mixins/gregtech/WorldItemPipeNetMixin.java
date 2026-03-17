package supersymmetry.mixins.gregtech;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import gregtech.api.pipenet.WorldPipeNet;
import gregtech.common.pipelike.itempipe.net.WorldItemPipeNet;

@Mixin(value = WorldItemPipeNet.class, remap = false)
public abstract class WorldItemPipeNetMixin {

    /**
     * @author aliu-here
     * @reason couldn't figure out a way to fix the original through a regular mixin; i just copied source w/ added line
     */
    @Overwrite
    public static WorldItemPipeNet getWorldPipeNet(World world) {
        String DATA_ID = WorldPipeNet.getDataID("susy.item_pipe_net", world); // just ignore whatever jank gregtech did
                                                                              // entirely
        WorldItemPipeNet netWorldData = (WorldItemPipeNet) world.loadData(WorldItemPipeNet.class, DATA_ID);
        if (netWorldData == null) {
            netWorldData = new WorldItemPipeNet(DATA_ID);
            world.setData(DATA_ID, netWorldData);
        }
        ((WorldPipeNetAccessor) netWorldData).callSetWorldAndInit(world);
        return netWorldData;
    }
}
