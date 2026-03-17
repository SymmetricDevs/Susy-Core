package supersymmetry.mixins.gregtech;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import gregtech.api.pipenet.WorldPipeNet;

@Mixin(value = WorldPipeNet.class, remap = false)
public interface WorldPipeNetAccessor {

    @Invoker
    void callSetWorldAndInit(World world);
}
