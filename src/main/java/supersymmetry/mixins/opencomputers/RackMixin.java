package supersymmetry.mixins.opencomputers;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import li.cil.oc.api.component.RackMountable;
import li.cil.oc.api.network.Component;
import li.cil.oc.api.network.Visibility;
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
    private static final Field supersymmetry$nodeMappingField;

    @Unique
    private static final Method supersymmetry$connectMethod;

    static {
        Field nmField = null;
        Method cMethod = null;
        try {
            nmField = Rack.class.getDeclaredField("nodeMapping");
            nmField.setAccessible(true);
            for (Method m : Rack.class.getDeclaredMethods()) {
                if (m.getName().equals("connect") && m.getParameterCount() == 3) {
                    m.setAccessible(true);
                    cMethod = m;
                    break;
                }
            }
        } catch (Exception e) {}
        supersymmetry$nodeMappingField = nmField;
        supersymmetry$connectMethod = cMethod;
    }

    @Inject(method = "onItemRemoved", at = @At("HEAD"))
    private void supersymmetry$cacheMapping(int slot, ItemStack stack, CallbackInfo ci) {
        Rack rack = (Rack) (Object) this;
        if (rack.getWorld() == null || rack.getWorld().isRemote) return;
        try {
            Object[] nodeMapping = (Object[]) supersymmetry$nodeMappingField.get(rack);
            Object[] slotMapping = (Object[]) nodeMapping[slot];
            System.arraycopy(slotMapping, 0, supersymmetry$cachedMappings[slot], 0, 4);
        } catch (Exception e) {}
    }

    @Inject(method = "onItemAdded", at = @At("RETURN"))
    private void supersymmetry$restoreMapping(int slot, ItemStack stack, CallbackInfo ci) {
        Rack rack = (Rack) (Object) this;
        if (rack.getWorld() == null || rack.getWorld().isRemote) return;
        if (supersymmetry$connectMethod == null) return;

        boolean primaryConnected = false;
        for (int i = 0; i < 4; i++) {
            Object cached = supersymmetry$cachedMappings[slot][i];
            if (cached == null) continue;
            try {
                Method isDefined = cached.getClass().getMethod("isDefined");
                if ((Boolean) isDefined.invoke(cached)) {
                    supersymmetry$connectMethod.invoke(rack, slot, i - 1, cached);
                    if (i == 0) primaryConnected = true;
                }
            } catch (Exception e) {}
        }

        // setVisibility(Network) was called inside DiskDriveMountable.load() while
        // the mountable was still isolated. Re-call it now that the bus connection
        // is live so OC actually fires component.added to all reachable machines.
        if (primaryConnected) {
            supersymmetry$reannounceComponentNodes(rack, slot);
        }
    }

    @Unique
    private static void supersymmetry$reannounceComponentNodes(Rack rack, int slot) {
        try {
            RackMountable mountable = rack.getMountable(slot);
            if (mountable == null) return;
            try {
                Method filesystemNode = mountable.getClass().getMethod("filesystemNode");
                Object option = filesystemNode.invoke(mountable);
                Method isDefined = option.getClass().getMethod("isDefined");
                if ((Boolean) isDefined.invoke(option)) {
                    Method get = option.getClass().getMethod("get");
                    Object node = get.invoke(option);
                    if (node instanceof Component) {
                        ((Component) node).setVisibility(Visibility.Network);
                    }
                }
            } catch (NoSuchMethodException ignored) {}
        } catch (Exception e) {}
    }
}
