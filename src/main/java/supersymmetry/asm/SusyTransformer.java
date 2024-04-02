package supersymmetry.asm;

import net.minecraft.client.Minecraft;
import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import supersymmetry.api.SusyLog;
import supersymmetry.asm.visitors.DefinitionManagerVisitor;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collections;

public class SusyTransformer implements IClassTransformer, Opcodes {

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {

        if (name.equals("cam72cam.immersiverailroading.registry.DefinitionManager")) {
            SusyLog.logger.info("Transforming {}", name);
            return writeDefinitionManager(basicClass);
        }
        if (name.equals("cam72cam.immersiverailroading.registry.DefinitionManager$StockLoader")) {
            SusyLog.logger.info("Transforming {}", name);
            return writeStockLoader(basicClass);
        }

        return basicClass;
    }

    private byte[] writeDefinitionManager(byte[] basicClass) {

        ClassNode cls = new DefinitionManagerVisitor();

        ClassReader reader = new ClassReader(basicClass);
        reader.accept(cls, 0);

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cls.accept(writer);

        writeClass(cls);

        return writer.toByteArray();
    }

    private byte[] writeStockLoader(byte[] basicClass) {

        ClassNode cls = new ClassNode();

        ClassReader reader = new ClassReader(basicClass);
        reader.accept(cls, 0);

        cls.interfaces = Collections.singletonList("supersymmetry/loaders/SuSyIRLoader$StockLoader");

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS);
        cls.accept(writer);

        return writer.toByteArray();
    }

    public static void writeClass(ClassNode cls) {
        FileOutputStream stream;
        File file = new File(Minecraft.getMinecraft().gameDir, "classOutputs/" + cls.name + ".class");
        file.getParentFile().mkdirs();

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        cls.accept(writer);

        try {
            stream = new FileOutputStream(file);
            stream.write(writer.toByteArray());
            stream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
