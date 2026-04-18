package supersymmetry.mixins.projectred;

import java.lang.reflect.Field;

import net.minecraft.block.BlockRedstoneWire;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import mrtjp.projectred.core.WirePropagator$;

@Mixin(value = WirePropagator$.class, remap = false)
public class WirePropagatorMixin {

    /**
     * @author bruberu
     * @reason This was not getting the right field by reflection.
     */
    @Overwrite
    private final Field liftedTree1$1() {
        try {
            // Can provide power
            Field c = ObfuscationReflectionHelper.findField(BlockRedstoneWire.class, "field_150181_a");
            c.setAccessible(true);
            return c;
        } catch (Exception var3) {
            throw new RuntimeException(var3);
        }
    }
}
