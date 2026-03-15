package supersymmetry.mixins.gregtech;

import java.util.List;

import net.minecraft.world.World;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import gregtech.api.pipenet.PipeNet;
import gregtech.api.pipenet.WorldPipeNet;
import gregtech.common.pipelike.itempipe.net.ItemPipeNet;

@Mixin(value = WorldPipeNet.class, remap = false)
public interface WorldPipeNetAccessor {

    @Invoker
    void callSetWorldAndInit(World world);

    @Accessor
    List<ItemPipeNet> getPipeNets();

    @Invoker("addPipeNetSilently")
    void callAddPipeNet(PipeNet pipeNet);
}
