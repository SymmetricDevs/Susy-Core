package supersymmetry.asm;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import supersymmetry.api.SusyLog;

import java.io.FileOutputStream;
import java.io.IOException;

public class SusyTransformer implements IClassTransformer, Opcodes {
    public static final String DEFINITION_MANAGER_CLASS_NAME = "cam72cam/immersiverailroading/registry/DefinitionManager";

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
        if(internalName.equals(DEFINITION_MANAGER_CLASS_NAME)) {
            ClassReader cr = new ClassReader(basicClass);
            ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
            cr.accept(cw, 0);
            cw.visitInnerClass("cam72cam/immersiverailroading/registry/DefinitionManager$StockLoaderBridge", "cam72cam/immersiverailroading/registry/DefinitionManager", "StockLoaderBridge", Opcodes.ACC_PUBLIC);

            byte[] sus = cw.toByteArray();
            SusyLog.logger.info(String.format("Transformed class %s", name));
            return sus;
        }

        return basicClass;
    }
}
