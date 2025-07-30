package supersymmetry.mixins.reccomplex;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure.class, remap = false)
public class GenericStructureMixin {
     //Stamp a fresh UUID into the entity NBT before creation
    @Redirect(
            method = "generate(Livorius/reccomplex/world/gen/feature/structure/context/StructureSpawnContext;" +
                    "Livorius/reccomplex/world/gen/feature/structure/generic/GenericStructure$InstanceData;" +
                    "Livorius/reccomplex/world/gen/feature/structure/generic/TransformerMulti;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityList;createEntityFromNBT(Lnet/minecraft/nbt/NBTTagCompound;Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;",
                    remap = false
            )
    )
    private Entity injectUUIDBeforeCreate(NBTTagCompound entityCompound, World world) {
        java.util.UUID newUUID = java.util.UUID.randomUUID();
        entityCompound.setLong("UUIDMost", newUUID.getMostSignificantBits());
        entityCompound.setLong("UUIDLeast", newUUID.getLeastSignificantBits());
        return EntityList.createEntityFromNBT(entityCompound, world);
    }

     //Prevent RC from overriding our baked-in UUID
    @Redirect(
            method = "generate(Livorius/reccomplex/world/gen/feature/structure/context/StructureSpawnContext;" +
                    "Livorius/reccomplex/world/gen/feature/structure/generic/GenericStructure$InstanceData;" +
                    "Livorius/reccomplex/world/gen/feature/structure/generic/TransformerMulti;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Livorius/reccomplex/utils/accessor/RCAccessorEntity;setEntityUniqueID(Lnet/minecraft/entity/Entity;Ljava/util/UUID;)V",
                    remap = false
            )
    )
    private static void noopOverrideUUID(Entity entity, java.util.UUID uuid) {}
}
