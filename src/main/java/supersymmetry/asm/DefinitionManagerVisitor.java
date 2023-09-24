package supersymmetry.asm;

import gregtech.asm.util.ObfMapping;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class DefinitionManagerVisitor extends ClassVisitor implements Opcodes {
    public static final String TARGET_CLASS_NAME = "cam72cam/immersiverailroading/registry/DefinitionManager";
    private static final ObfMapping TARGET_METHOD = new ObfMapping("cam72cam/immersiverailroading/registry/DefinitionManager", "lambda$initModels$3", "(Lcam72cam/mod/gui/Progress$Bar;Ljava/util/Map$Entry;)Lorg/apache/commons/lang3/tuple/Pair;");
    private static String TARGET_TYPE = "cam72cam/immersiverailroading/registry/DefinitionManager$JsonLoader";
    public DefinitionManagerVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }
    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

        if(name.equals(TARGET_METHOD.s_name) && desc.equals(TARGET_METHOD.s_desc)) {
            return new MethodVisitor(Opcodes.ASM5, mv) {
                @Override
                public void visitTypeInsn(int opcode, String type) {
                    if (opcode == Opcodes.CHECKCAST && type.equals(TARGET_TYPE)) {
                        // Do nothing to remove the cast instruction
                    } else {
                        super.visitTypeInsn(opcode, type);
                    }
                }
            };
        }
        return mv;
    }
}
