package supersymmetry.asm;

import gregtech.asm.util.ObfMapping;
import org.objectweb.asm.*;


public class DefinitionManagerVisitor extends ClassVisitor implements Opcodes {
    public static final String TARGET_CLASS_NAME = "cam72cam/immersiverailroading/registry/DefinitionManager";
    private static final ObfMapping TARGET_METHOD = new ObfMapping("cam72cam/immersiverailroading/registry/DefinitionManager", "lambda$initModels$4", "(Ljava/util/Map;Lcam72cam/mod/gui/Progress$Bar;Ljava/util/Map$Entry;)Lorg/apache/commons/lang3/tuple/Pair;");
    private static String TARGET_TYPE = "cam72cam/immersiverailroading/registry/DefinitionManager$StockLoader";
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
                    // what the actual fuck am I writing here -- MTBO
                    if (opcode == Opcodes.CHECKCAST && type.equals(TARGET_TYPE)) {
                        // Do nothing to remove the cast instruction (I think?)
                    } else {
                        super.visitTypeInsn(opcode, type);
                    }
                }
            };
        }
        return mv;
    }
}
