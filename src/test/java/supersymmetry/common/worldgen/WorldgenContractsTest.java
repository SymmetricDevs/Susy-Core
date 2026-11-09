package supersymmetry.common.worldgen;

import static org.junit.jupiter.api.Assertions.*;

import java.io.InputStream;

import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

class WorldgenContractsTest {

    private static ClassNode read(String name) throws Exception {
        ClassNode node = new ClassNode();
        try (InputStream in = WorldgenContractsTest.class
                .getResourceAsStream("/" + name.replace('.', '/') + ".class")) {
            assertNotNull(in);
            new ClassReader(in).accept(node, 0);
        }
        return node;
    }

    @Test
    void loadedPeekCannotLoadChunksOrCancelUnloading() throws Exception {
        ClassNode node = read("supersymmetry.mixins.minecraft.LoadedChunksMixin");
        MethodNode peek = node.methods.stream().filter(m -> m.name.equals("susy$peekLoadedChunk")).findFirst().get();
        int calls = 0;
        for (AbstractInsnNode instruction : peek.instructions.toArray()) {
            assertNotEquals(Opcodes.PUTFIELD, instruction.getOpcode());
            if (instruction instanceof MethodInsnNode) {
                MethodInsnNode call = (MethodInsnNode) instruction;
                calls++;
                assertTrue(call.owner.equals("it/unimi/dsi/fastutil/longs/Long2ObjectMap") && call.name.equals("get") ||
                        call.owner.equals("net/minecraft/util/math/ChunkPos") && call.name.equals("asLong"));
            }
        }
        assertEquals(2, calls);
    }

    @Test
    void metadataAndIndicatorWorkersNeverLoadTerrain() throws Exception {
        for (String name : new String[] { "GridMasters", "Indicators" }) {
            ClassNode node = read("supersymmetry.common.worldgen." + name);
            for (MethodNode method : node.methods) for (AbstractInsnNode instruction : method.instructions.toArray()) {
                if (!(instruction instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) instruction;
                for (String forbidden : new String[] { "provideChunk", "getChunkFromChunkCoords",
                        "getChunkFromBlockCoords", "loadChunk", "loadChunk__Async", "getLoadedChunk" })
                    assertNotEquals(forbidden, call.name);
                assertFalse(call.name.equals("<init>") && call.owner.equals("net/minecraft/world/chunk/Chunk"));
            }
        }
    }

    @Test
    void savedQueueKeepsItsExistingIdentity() {
        assertEquals("susy_pending_indicators_v1", IndicatorData.NAME);
    }

    @Test
    void mixinsUseJava8Bytecode() throws Exception {
        for (String name : new String[] { "minecraft.LoadedChunksMixin", "minecraft.AnvilPendingAccessor",
                "gregtech.GridMasterMixin",
                "gregtech.SurfaceRockMixin", "gregtech.SurfaceBlockMixin", "reccomplex.RecurrentPlacerMixin",
                "ivtoolkit.WorldCacheMixin" })
            assertEquals(52, read("supersymmetry.mixins." + name).version);
    }
}
