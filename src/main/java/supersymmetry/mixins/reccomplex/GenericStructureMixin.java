package supersymmetry.mixins.reccomplex;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure.class)
public class GenericStructureMixin {
    /**
     * Stamp a fresh UUID into the NBT just before RC calls EntityList.createEntityFromNBT(...).
     */
    @Redirect(
            method = "generate",  // literal deobf name
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityList;createEntityFromNBT(Lnet/minecraft/nbt/NBTTagCompound;Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;"
            ),
            remap = false
    )
    private Entity injectUUIDBeforeCreate(NBTTagCompound tag, World world) {
        java.util.UUID id = java.util.UUID.randomUUID();
        tag.setLong("UUIDMost", id.getMostSignificantBits());
        tag.setLong("UUIDLeast", id.getLeastSignificantBits());
        return EntityList.createEntityFromNBT(tag, world);
    }

    /**
     * No-op RC’s later call to setEntityUniqueID so we keep our baked-in value.
     */
    @Redirect(
            method = "generate",  // literal deobf name
            at = @At(
                    value = "INVOKE",
                    target = "Livorius/reccomplex/utils/accessor/RCAccessorEntity;setEntityUniqueID(Lnet/minecraft/entity/Entity;Ljava/util/UUID;)V"
            ),
            remap = false
    )
    private static void noopOverrideUUID(Entity e, java.util.UUID uuid) {
        // intentionally empty
    }
}
