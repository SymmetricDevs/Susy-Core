package supersymmetry.asm;

import gregtech.asm.util.ObfMapping;
import gregtech.common.blocks.MetaBlocks;
import net.minecraft.item.Item;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class BlockStoneSmoothVisitor extends ClassVisitor implements Opcodes {
    public static final String TARGET_CLASS_NAME = "gregtech/common/blocks/BlockStoneSmooth";
    public static final ObfMapping TARGET_METHOD = new ObfMapping("net/minecraft/block/Block", "getItemDropped", "(Lnet/minecraft/block/state/IBlockState;Ljava/util/Random;I)Lnet/minecraft/item/Item;");
    public static final ObfMapping HOOK = new ObfMapping("supersymmetry/asm/BlockStoneSmoothVisitor", "getItemHook", "()Lnet/minecraft/item/Item;");

    public BlockStoneSmoothVisitor(ClassVisitor cv) {
        super(Opcodes.ASM5, cv);
        ObfMapping methodKey = TARGET_METHOD.toRuntime();
        {
            MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, methodKey.s_name, methodKey.s_desc, null, null);
            mv.visitCode();
            Label label0 = new Label();
            mv.visitLabel(label0);
            HOOK.visitMethodInsn(mv, INVOKESTATIC);
            mv.visitInsn(ARETURN);
            Label label1 = new Label();
            mv.visitLabel(label1);
            mv.visitLocalVariable("this", "Lgregtech/common/blocks/BlockStoneSmooth;", null, label0, label1, 0);
            mv.visitLocalVariable("state", "Lnet/minecraft/block/state/IBlockState;", null, label0, label1, 1);
            mv.visitLocalVariable("rand", "Ljava/util/Random;", null, label0, label1, 2);
            mv.visitLocalVariable("fortune", "I", null, label0, label1, 3);
            mv.visitMaxs(1, 4);
            mv.visitEnd();
        }
    }

    @SuppressWarnings("unused")
    public static Item getItemHook() {
        return Item.getItemFromBlock(MetaBlocks.STONE_COBBLE);
    }
}
