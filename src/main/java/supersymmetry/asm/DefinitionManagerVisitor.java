package supersymmetry.asm;

import org.objectweb.asm.*;


public class DefinitionManagerVisitor extends ClassVisitor implements Opcodes {
    public static final String TARGET_CLASS_NAME = "cam72cam/immersiverailroading/registry/DefinitionManager";
    private static String STOCK_LOADER_NAME = "cam72cam/immersiverailroading/registry/DefinitionManager$StockLoader";
    public DefinitionManagerVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        if (name.equals(STOCK_LOADER_NAME)) {
            super.visitInnerClass(name, outerName, innerName, Opcodes.ACC_PUBLIC + Opcodes.ACC_INTERFACE + Opcodes.ACC_ABSTRACT);
        } else {
            super.visitInnerClass(name, outerName, innerName, access);
        }
    }
}
