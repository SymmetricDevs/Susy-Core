package supersymmetry.client.renderer;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public final class CatwalkCache {

    private static final RenderTileCache<World, TileEntity> CACHE = new RenderTileCache<>();
    private static final Class<?> TYPE = tileType();

    private static Class<?> tileType() {
        try {
            return Class.forName("dmfmm.catwalks.tileentity.CatwalkTile", false, CatwalkCache.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Catwalks cache loaded without Catwalks", e);
        }
    }

    static long chunk(int x, int z) {
        return ((long) x << 32) ^ (z & 0xffffffffL);
    }

    public static TileEntity get(World world, BlockPos pos) {
        if (world == null || !world.isBlockLoaded(pos)) return null;
        return CACHE.get(world, chunk(pos.getX() >> 4, pos.getZ() >> 4), pos.toLong(), () -> {
            TileEntity tile = world.getTileEntity(pos);
            return TYPE.isInstance(tile) ? tile : null;
        }, tile -> !tile.isInvalid());
    }

    public static void remove(World world, BlockPos pos) {
        CACHE.remove(world, chunk(pos.getX() >> 4, pos.getZ() >> 4), pos.toLong());
    }

    @SubscribeEvent
    public void unload(WorldEvent.Unload event) {
        CACHE.unloadWorld(event.getWorld());
    }

    @SubscribeEvent
    public void unload(ChunkEvent.Unload event) {
        CACHE.unloadChunk(event.getWorld(), chunk(event.getChunk().x, event.getChunk().z));
    }
}
