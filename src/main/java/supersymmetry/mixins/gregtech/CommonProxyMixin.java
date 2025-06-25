package supersymmetry.mixins.gregtech;


import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import gregtech.common.CommonProxy;
import gregtech.common.pipelike.cable.BlockCable;
import gregtech.common.pipelike.cable.ItemBlockCable;
import gregtech.common.pipelike.fluidpipe.BlockFluidPipe;
import gregtech.common.pipelike.fluidpipe.ItemBlockFluidPipe;
import gregtech.common.pipelike.itempipe.BlockItemPipe;
import gregtech.common.pipelike.itempipe.ItemBlockItemPipe;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

/// Yeah, it's a bunch of dirty mixins
@Mixin(value = CommonProxy.class, remap = false)
public class CommonProxyMixin {

    @WrapWithCondition(method = "registerBlocks",
            at = @At(target = "Lnet/minecraftforge/registries/IForgeRegistry;register(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V",
                    value = "INVOKE",
                    ordinal = 1))
    private static boolean registerCablesOnlyWhenNeeded(IForgeRegistry<?> registry, IForgeRegistryEntry<?> entry) {
        if (entry instanceof BlockCable pipe) {
            return !pipe.getEnabledMaterials().isEmpty();
        } else
            throw new AssertionError("BlockCable mixin mixed-in to a wrong target registration: \"" + entry.getClass() + "\"!");
    }

    @WrapWithCondition(method = "registerBlocks",
            at = @At(target = "Lnet/minecraftforge/registries/IForgeRegistry;register(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V",
                    value = "INVOKE",
                    ordinal = 2))
    private static boolean registerFluidPipesOnlyWhenNeeded(IForgeRegistry<?> registry, IForgeRegistryEntry<?> entry) {
        if (entry instanceof BlockFluidPipe pipe) {
            return !pipe.getEnabledMaterials().isEmpty();
        } else
            throw new AssertionError("BlockFluidPipe mixin mixed-in to a wrong target registration: \"" + entry.getClass() + "\"!");
    }

    @WrapWithCondition(method = "registerBlocks",
            at = @At(target = "Lnet/minecraftforge/registries/IForgeRegistry;register(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V",
                    value = "INVOKE",
                    ordinal = 3))
    private static boolean registerItemPipesOnlyWhenNeeded(IForgeRegistry<?> registry, IForgeRegistryEntry<?> entry) {
        if (entry instanceof BlockItemPipe pipe) {
            return !pipe.getEnabledMaterials().isEmpty();
        } else
            throw new AssertionError("BlockItemPipe mixin mixed-in to a wrong target registration: \"" + entry.getClass() + "\"!");
    }

    @WrapWithCondition(method = "registerItems",
            at = @At(target = "Lnet/minecraftforge/registries/IForgeRegistry;register(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V",
                    value = "INVOKE",
                    ordinal = 3))
    private static boolean registerCableItemsOnlyWhenNeeded(IForgeRegistry<?> registry, IForgeRegistryEntry<?> entry, @Local BlockCable block) {
        if (entry instanceof ItemBlockCable) {
            return !block.getEnabledMaterials().isEmpty();
        } else
            throw new AssertionError("ItemBlockCable mixin mixed-in to a wrong target registration: \"" + entry.getClass() + "\"!");
    }

    @WrapWithCondition(method = "registerItems",
            at = @At(target = "Lnet/minecraftforge/registries/IForgeRegistry;register(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V",
                    value = "INVOKE",
                    ordinal = 4))
    private static boolean registerFluidPipeItemsOnlyWhenNeeded(IForgeRegistry<?> registry, IForgeRegistryEntry<?> entry, @Local BlockFluidPipe block) {
        if (entry instanceof ItemBlockFluidPipe) {
            return !block.getEnabledMaterials().isEmpty();
        } else
            throw new AssertionError("ItemBlockFluidPipe mixin mixed-in to a wrong target registration: \"" + entry.getClass() + "\"!");
    }

    @WrapWithCondition(method = "registerItems",
            at = @At(target = "Lnet/minecraftforge/registries/IForgeRegistry;register(Lnet/minecraftforge/registries/IForgeRegistryEntry;)V",
                    value = "INVOKE",
                    ordinal = 5))
    private static boolean registerItemPipeItemsOnlyWhenNeeded(IForgeRegistry<?> registry, IForgeRegistryEntry<?> entry, @Local BlockItemPipe block) {
        if (entry instanceof ItemBlockItemPipe) {
            return !block.getEnabledMaterials().isEmpty();
        } else
            throw new AssertionError("ItemBlockItemPipe mixin mixed-in to a wrong target registration: \"" + entry.getClass() + "\"!");
    }
}
