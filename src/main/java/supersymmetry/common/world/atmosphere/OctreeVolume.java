package supersymmetry.common.world.atmosphere;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

/**
 * An unbounded set of block positions, stored as a sparse grid of fixed-size, grid-aligned {@link Octree}s.
 *
 * <p>
 * Unlike a single octree with a fixed origin, a volume stored this way can grow in any direction and be merged
 * with or split from other volumes without any bounds bookkeeping.
 */
public class OctreeVolume implements Iterable<BlockPos> {

    private static final int SHIFT = 5;
    private static final int TREE_SIZE = 1 << SHIFT;

    private final Long2ObjectOpenHashMap<Octree> trees = new Long2ObjectOpenHashMap<>();
    private int size = 0;

    private static long key(int tx, int ty, int tz) {
        return ((long) (tx & 0x3FFFFFF) << 38) | ((long) (ty & 0xFFF) << 26) | (tz & 0x3FFFFFF);
    }

    private static long key(BlockPos pos) {
        return key(pos.getX() >> SHIFT, pos.getY() >> SHIFT, pos.getZ() >> SHIFT);
    }

    public boolean insert(BlockPos pos) {
        long key = key(pos);
        Octree tree = trees.get(key);
        if (tree == null) {
            tree = new Octree(pos.getX() >> SHIFT << SHIFT, pos.getY() >> SHIFT << SHIFT,
                    pos.getZ() >> SHIFT << SHIFT, TREE_SIZE);
            trees.put(key, tree);
        }
        if (tree.insert(pos)) {
            size++;
            return true;
        }
        return false;
    }

    public boolean remove(BlockPos pos) {
        long key = key(pos);
        Octree tree = trees.get(key);
        if (tree == null || !tree.remove(pos))
            return false;
        size--;
        if (tree.isEmpty())
            trees.remove(key);
        return true;
    }

    public boolean contains(BlockPos pos) {
        Octree tree = trees.get(key(pos));
        return tree != null && tree.contains(pos);
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    @Override
    public Iterator<BlockPos> iterator() {
        Iterator<Octree> treeIt = trees.values().iterator();
        return new Iterator<BlockPos>() {

            private Iterator<BlockPos> current = Collections.emptyIterator();

            @Override
            public boolean hasNext() {
                while (!current.hasNext() && treeIt.hasNext()) {
                    current = treeIt.next().iterator();
                }
                return current.hasNext();
            }

            @Override
            public BlockPos next() {
                if (!hasNext())
                    throw new NoSuchElementException();
                return current.next();
            }
        };
    }

    // ---- NBT serialization ----

    public NBTTagList writeToNBT() {
        NBTTagList list = new NBTTagList();
        for (Octree tree : trees.values()) {
            NBTTagCompound tag = new NBTTagCompound();
            tag.setInteger("x", tree.getOriginX());
            tag.setInteger("y", tree.getOriginY());
            tag.setInteger("z", tree.getOriginZ());
            tag.setByteArray("data", tree.serialize());
            list.appendTag(tag);
        }
        return list;
    }

    public static OctreeVolume readFromNBT(NBTTagList list) {
        OctreeVolume volume = new OctreeVolume();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound tag = list.getCompoundTagAt(i);
            int x = tag.getInteger("x"), y = tag.getInteger("y"), z = tag.getInteger("z");
            Octree tree = Octree.deserialize(x, y, z, TREE_SIZE, tag.getByteArray("data"));
            if (tree.isEmpty())
                continue;
            volume.trees.put(key(x >> SHIFT, y >> SHIFT, z >> SHIFT), tree);
            volume.size += tree.size();
        }
        return volume;
    }
}
