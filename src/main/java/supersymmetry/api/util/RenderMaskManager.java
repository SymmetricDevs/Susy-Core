package supersymmetry.api.util;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockPos.MutableBlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@SideOnly(Side.CLIENT)
public class RenderMaskManager {

    public final static ThreadLocal<Boolean> isBuildingChunk = ThreadLocal.withInitial(()-> Boolean.FALSE);
    protected static Set<BlockPos> modelDisabled = new ObjectOpenHashSet<>();
    protected static Map<BlockPos, Collection<BlockPos>> multiDisabled = new HashMap<>();

    public static void removeDisableModel(BlockPos controllerPos, boolean updateRendering) {
        Collection<BlockPos> poses = multiDisabled.remove(controllerPos);
        if (poses == null) return;
        modelDisabled.clear();
        multiDisabled.values().forEach(modelDisabled::addAll);
        if (updateRendering) updateRenderChunk(poses);
    }

    private static void updateRenderChunk(Collection<BlockPos> poses) {

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;

        for (BlockPos pos : poses) {
            minX = Math.min(minX, pos.getX());
            minY = Math.min(minY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxX = Math.max(maxX, pos.getX());
            maxY = Math.max(maxY, pos.getY());
            maxZ = Math.max(maxZ, pos.getZ());
        }

        Minecraft.getMinecraft().world.markBlockRangeForRenderUpdate(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
    }

    public static void addDisableModel(BlockPos controllerPos, Collection<BlockPos> poses, boolean updateRendering) {
        multiDisabled.put(controllerPos, poses);
        modelDisabled.addAll(poses);
        if (updateRendering) updateRenderChunk(poses);
    }

    public static boolean isModelDisabled(BlockPos pos) {
        if (isBuildingChunk.get()) {
            return isModelDisabledRaw(pos);
        }
        return false;
    }

    public static boolean isModelDisabledRaw(BlockPos pos) {
        return modelDisabled.contains(pos);
    }

    public static boolean isModelDisabled(MutableBlockPos pos) {
        return isModelDisabled(pos.toImmutable());
    }

    public static boolean isModelDisabledRaw(MutableBlockPos pos) {
        return isModelDisabledRaw(pos.toImmutable());
    }

    public static void clearDisabled() {
        modelDisabled.clear();
        multiDisabled.clear();
    }
}
