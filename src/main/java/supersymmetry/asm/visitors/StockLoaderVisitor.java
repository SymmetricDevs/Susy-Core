package supersymmetry.asm.visitors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public class StockLoaderVisitor extends ClassNode implements Opcodes {

    public StockLoaderVisitor() {
        super(ASM5);
    }

    @Override
    public void visitInnerClass(String name, String outerName, String innerName, int access) {
        if (name.equals("cam72cam/immersiverailroading/registry/DefinitionManager$StockLoader")) {
            super.visitInnerClass(name, outerName, innerName, ACC_PUBLIC | ACC_STATIC | ACC_ABSTRACT | ACC_INTERFACE);
        }
        else super.visitInnerClass(name, outerName, innerName, access);
    }
}
