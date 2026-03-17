package supersymmetry.mixins.gregtech;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import gregtech.api.pipenet.block.BlockPipe;
import gregtech.api.pipenet.tile.IPipeTile;

@Mixin(value = IPipeTile.class, remap = false)
public interface TileEntityItemPipeAccessor {

    @Invoker
    public BlockPipe callGetPipeBlock();

    @Invoker
    public int callGetConnections();

    @Invoker
    public World callGetPipeWorld();

    @Invoker
    public BlockPos callGetPipePos();
}
