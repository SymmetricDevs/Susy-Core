package supersymmetry.mixins.reccomplex;

import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.world.gen.feature.structure.context.StructureSpawnContext;
import ivorius.reccomplex.world.gen.feature.structure.generic.GenericStructure;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(value = GenericStructure.class, remap = false)
public class GenericStructureMixin {
    @Unique
    private static void log(String msg) {
        RecurrentComplex.logger.info("[GenericStructureMixin] " + msg);
        System.out.println("[GenericStructureMixin] " + msg);
    }

    @Inject(method = "generate", at = @At("HEAD"))
    private void onStartGenerating(StructureSpawnContext context, GenericStructure.InstanceData instanceData, Object foreignTransformer, CallbackInfo ci) {
        System.out.println("Structure generation started at: " + context.boundingBox);
    }

    @Inject(
            method = "generate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityList;createEntityFromNBT(Lnet/minecraft/nbt/NBTTagCompound;Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;"
            )
    )
    private void onStartEntityGeneration(StructureSpawnContext context, GenericStructure.InstanceData instanceData, Object foreignTransformer, CallbackInfo ci) {
        System.out.println("Starting entity generation phase.");
    }

    @Redirect(
            method = "generate",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/EntityList;createEntityFromNBT(Lnet/minecraft/nbt/NBTTagCompound;Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;"
            )
    )
    private Entity onCreateEntityWithPatchedUUID(NBTTagCompound originalTag, World world) {
        NBTTagCompound tag = originalTag.copy();
        UUID newUUID = UUID.randomUUID();
        tag.setLong("UUIDMost", newUUID.getMostSignificantBits());
        tag.setLong("UUIDLeast", newUUID.getLeastSignificantBits());

        String id = tag.getString("id");
        System.out.println("Attempting to spawn entity: " + id + " with UUID: " + newUUID);

        Entity entity = EntityList.createEntityFromNBT(tag, world);
        if (entity == null) {
            System.out.println("Failed to create entity from NBT: " + tag);
        } else {
            System.out.println("Successfully created entity: " + entity.getName() + " (" + entity.getUniqueID() + ")");
        }

        return entity;
    }

    @Redirect(
            method = "generate",
            at = @At(
                    value = "INVOKE",
                    target = "Livorius/reccomplex/utils/accessor/RCAccessorEntity;setEntityUniqueID(Lnet/minecraft/entity/Entity;Ljava/util/UUID;)V"
            )
    )
    private static void skipForcedUUIDOverwrite(Entity entity, UUID ignored) {
        // No-op
    }

    @Inject(method = "generate", at = @At("RETURN"))
    private void onFinishGenerating(StructureSpawnContext context, GenericStructure.InstanceData instanceData, Object foreignTransformer, CallbackInfo ci) {
        System.out.println("Structure generation completed.");
    }
}
