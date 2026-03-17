package supersymmetry.mixins.gregtech;

import org.spongepowered.asm.mixin.Mixin;

import gregtech.api.pipenet.tile.TileEntityPipeBase;
import gregtech.api.unification.material.properties.ItemPipeProperties;
import gregtech.common.pipelike.itempipe.BlockItemPipe;
import gregtech.common.pipelike.itempipe.ItemPipeType;
import gregtech.common.pipelike.itempipe.net.WorldItemPipeNet;
import gregtech.common.pipelike.itempipe.tile.TileEntityItemPipe;

@Mixin(value = TileEntityItemPipe.class, remap = false)
public abstract class TileEntityItemPipeMixin extends TileEntityPipeBase<ItemPipeType, ItemPipeProperties> {

    @Override
    public void onLoad() {
        super.onLoad();
        TileEntityItemPipeAccessor accessor = ((TileEntityItemPipeAccessor) this);
        BlockItemPipe pipeBlock = (BlockItemPipe) accessor.callGetPipeBlock();
        WorldItemPipeNet worldPipeNet = (WorldItemPipeNet) pipeBlock.getWorldPipeNet(accessor.callGetPipeWorld());
        if (worldPipeNet.getNetFromPos(accessor.callGetPipePos()) == null) {
            worldPipeNet.addNode(accessor.callGetPipePos(),
                    pipeBlock.createProperties((TileEntityItemPipe) (Object) this), 0, accessor.callGetConnections(),
                    accessor.callGetConnections() == 0);
        }
    }
}
