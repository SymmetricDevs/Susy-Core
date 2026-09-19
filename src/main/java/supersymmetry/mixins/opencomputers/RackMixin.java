package supersymmetry.mixins.opencomputers;

import java.lang.reflect.Field;
import li.cil.oc.common.tileentity.Rack;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Rack.class, remap = false)
public abstract class RackMixin {

    @Unique
    private final Object[][] supersymmetry$cachedMappings = new Object[4][4];

    @Unique
    private Object[][] supersymmetry$getNodeMapping(Rack rack) {
        try {
            Field field = Rack.class.getDeclaredField("nodeMapping");
            field.setAccessible(true);
            return (Object[][]) field.get(rack);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Inject(method = "onItemRemoved", at = @At("HEAD"))
    private void supersymmetry$cacheMapping(int slot, ItemStack stack, CallbackInfo ci) {
        Rack rack = (Rack) (Object) this;
        if (rack.getWorld() != null && !rack.getWorld().isRemote) {
            Object[][] mapping = supersymmetry$getNodeMapping(rack);
            if (mapping != null && mapping[slot] != null) {
                System.arraycopy(mapping[slot], 0, supersymmetry$cachedMappings[slot], 0, 4);
                System.out.println("RACK: Cached mapping for slot " + slot);
            }
        }
    }

    @Inject(method = "onItemAdded", at = @At("RETURN"))
    private void supersymmetry$restoreMapping(int slot, ItemStack stack, CallbackInfo ci) {
        Rack rack = (Rack) (Object) this;
        if (rack.getWorld() != null && !rack.getWorld().isRemote) {
            Object[][] mapping = supersymmetry$getNodeMapping(rack);
            if (mapping != null && mapping[slot] != null) {
                System.arraycopy(supersymmetry$cachedMappings[slot], 0, mapping[slot], 0, 4);
                System.out.println("RACK: Restored mapping for slot " + slot);
            }
        }
    }
}
