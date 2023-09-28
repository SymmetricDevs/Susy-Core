package supersymmetry.asm;

import org.objectweb.asm.*;

public class StockLoaderBridgeGenerator {
    public static String TARGET_CLASS_NAME = "cam72cam/immersiverailroading/registry/DefinitionManager$StockLoaderBridge";
    public static String TARGET_CLASS_NAME_LOADER = "cam72cam.immersiverailroading.registry.DefinitionManager$StockLoaderBridge";

    // If anyone reads this, stop.
    // Do not read this.
    // Do not go further.
    // I warn you.
    // You cannot recover from the code that follows.
    // I spent the past 5 days trying to access that fucking StockLoader interface.
    // I belong in a mental asylum.
    // java.lang.ClassFormatError, java.lang.IncompatibleClassChangeError, java.lang.ClassCastException
    // Exceptions dreamed up by the utterly deranged (I am utterly deranged)
    public static byte[] generateStockLoaderClass() {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES);

        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC, TARGET_CLASS_NAME, null, "java/lang/Object", new String[]{"cam72cam/immersiverailroading/registry/DefinitionManager$StockLoader"});

        FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, "stockDefinitionClass", "Ljava/lang/Class;", "Ljava/lang/Class<LEntityRollingStockDefinition;>;", null);
        fv.visitEnd();

        // Constructor
        // Takes a class extending EntityRollingStockDefinition and stores it in the field stockDefinitionClass
        MethodVisitor constructor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "(Ljava/lang/Class;)V", null, null);
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        constructor.visitVarInsn(Opcodes.ALOAD, 0);
        constructor.visitVarInsn(Opcodes.ALOAD, 1);
        constructor.visitFieldInsn(Opcodes.PUTFIELD, TARGET_CLASS_NAME, "stockDefinitionClass", "Ljava/lang/Class;");
        constructor.visitInsn(Opcodes.RETURN);
        constructor.visitMaxs(2, 2);
        constructor.visitEnd();

        // Apply
        MethodVisitor applyMethod = cw.visitMethod(Opcodes.ACC_PUBLIC, "apply", "(Ljava/lang/String;LDataBlock;)LEntityRollingStockDefinition;", null, new String[]{"java/lang/Exception"});
        applyMethod.visitCode();
        // Load class to stack
        applyMethod.visitVarInsn(Opcodes.ALOAD, 0);
        applyMethod.visitFieldInsn(Opcodes.GETFIELD, TARGET_CLASS_NAME, "stockDefinitionClass", "Ljava/lang/Class;");
        // Load the arguments onto the stack
        applyMethod.visitVarInsn(Opcodes.ALOAD, 1); // defID
        applyMethod.visitVarInsn(Opcodes.ALOAD, 2); // data
        // Call the constructor of the class with the arguments
        applyMethod.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "newInstance", "()Ljava/lang/Object;", false);
        applyMethod.visitInsn(Opcodes.ARETURN);

        applyMethod.visitMaxs(3, 3); // This should be good?
        applyMethod.visitEnd();

        cw.visitEnd();

        return cw.toByteArray();
    }
}
