package supersymmetry.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import supersymmetry.api.SusyLog;


public class SusyTransformer implements IClassTransformer, Opcodes {
    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        String internalName = transformedName.replace('.', '/');
        if (internalName.equals(BlockStoneSmoothVisitor.TARGET_CLASS_NAME)) {
            ClassReader classReader = new ClassReader(basicClass);
            ClassWriter classWriter = new ClassWriter(0);
            classReader.accept(new BlockStoneSmoothVisitor(classWriter), 0);
            byte[] sus = classWriter.toByteArray();
            SusyLog.logger.info(String.format("Transformed class %s", name));
            return sus;
        }
        return basicClass;
    }
}
