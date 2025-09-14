package supersymmetry.mixins.reccomplex;

import ivorius.reccomplex.utils.accessor.RCAccessorEntity;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.util.UUID;

@Mixin(value = RCAccessorEntity.class, remap = false)
public class RCAccessorEntityMixin {

    /**
     * @author Tian_mi
     * @reason RC WTF are you doing???
     */
    @Overwrite
    public static void setEntityUniqueID(Entity entity, UUID uuid) {
        entity.setUniqueId(uuid);
    }

    /**
     * @author Tian_mi
     * @reason RC WTF are you doing???
     */
    @Overwrite
    public static UUID getEntityUniqueID(Entity entity) {
        return entity.getUniqueID();
    }
}
