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
/*      This is where any visitors that need to be invoked are ran
        if (internalName.equals(BlockStoneSmoothVisitor.TARGET_CLASS_NAME)) {
            ClassReader classReader = new ClassReader(basicClass);
            ClassWriter classWriter = new ClassWriter(0);
            classReader.accept(new BlockStoneSmoothVisitor(classWriter), 0);
            byte[] sus = classWriter.toByteArray();
            SusyLog.logger.info(String.format("Transformed class %s", name));
            return sus;
        }
*/
        if(internalName.equals(DefinitionManagerVisitor.TARGET_CLASS_NAME)) {
            ClassReader classReader = new ClassReader(basicClass);
            ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
            classReader.accept(new DefinitionManagerVisitor(classWriter), ClassReader.SKIP_FRAMES);
            byte[] sus = classWriter.toByteArray();
            SusyLog.logger.info(String.format("Transformed class %s", name));
            return sus;
        }

        return basicClass;
    }
}
