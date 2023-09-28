package supersymmetry.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import supersymmetry.api.SusyLog;
import supersymmetry.asm.visitors.StockLoaderVisitor;

public class SusyTransformer implements IClassTransformer, Opcodes {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {

        if (name.equals("cam72cam.immersiverailroading.registry.DefinitionManager")) {
            SusyLog.logger.info("Transforming {}", name);
            return writeStockLoader(basicClass);
        }

        return basicClass;
    }

    private byte[] writeStockLoader(byte[] basicClass) {
        ClassReader reader = new ClassReader(basicClass);
        ClassNode cls = new StockLoaderVisitor();

        reader.accept(cls, 0);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cls.accept(writer);

        return writer.toByteArray();
    }
}
