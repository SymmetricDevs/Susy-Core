package supersymmetry.asm.visitors;

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public class DefinitionManagerVisitor extends ClassNode implements Opcodes {

    public DefinitionManagerVisitor() {
        super(ASM5);
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if (name.equals("stockLoaders"))
            signature = "Ljava/util/Map<Ljava/lang/String;Lsupersymmetry/loaders/SuSyIRLoader$StockLoader;>;";
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        if (name.equals("lambda$initModels$4")) {
            MethodNode method = new LambdaMethodVisitor(access, name, desc, signature, exceptions);
            methods.add(method);
            return method;
        }
        return super.visitMethod(access, name, desc, signature, exceptions);
    }

    private static class LambdaMethodVisitor extends MethodNode {

        public LambdaMethodVisitor(int access, String name, String desc, String signature, String[] exceptions) {
            super(ASM5, access, name, desc, signature, exceptions);
        }

        @Override
        public void visitTypeInsn(int opcode, String type) {
            if (type.equals("cam72cam/immersiverailroading/registry/DefinitionManager$StockLoader"))
                type = "supersymmetry/loaders/SuSyIRLoader$StockLoader";
            super.visitTypeInsn(opcode, type);
        }

        @Override
        public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
            if (owner.equals("cam72cam/immersiverailroading/registry/DefinitionManager$StockLoader"))
                owner = "supersymmetry/loaders/SuSyIRLoader$StockLoader";
            super.visitMethodInsn(opcode, owner, name, desc, itf);
        }
    }
}
