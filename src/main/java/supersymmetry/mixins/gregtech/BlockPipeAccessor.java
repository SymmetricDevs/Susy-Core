package supersymmetry.mixins.gregtech;

import net.minecraft.item.ItemStack;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import gregtech.api.pipenet.block.BlockPipe;

@Mixin(value = BlockPipe.class, remap = false)
public interface BlockPipeAccessor {

    @Invoker("isPipeTool")
    boolean checkPipeTool(@NotNull ItemStack stack);
}
