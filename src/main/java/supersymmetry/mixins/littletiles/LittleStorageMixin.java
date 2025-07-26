package supersymmetry.mixins.littletiles;

import com.creativemd.littletiles.common.structure.type.LittleStorage;
import ivorius.reccomplex.RecurrentComplex;
import ivorius.reccomplex.item.GeneratingItem;
import ivorius.reccomplex.world.storage.loot.LootGenerationHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.WorldServer;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supersymmetry.integration.reccomplex.RCLootGenContext;

@Mixin(value = LittleStorage.class, remap = false)
public class LittleStorageMixin {

    @Inject(
            method = "loadFromNBTExtra(Lnet/minecraft/nbt/NBTTagCompound;)V",
            at     = @At("RETURN"),
            remap  = false
    )
    private void onLoadFromNBT(NBTTagCompound nbt, CallbackInfo ci) {
        LittleStorage storage = (LittleStorage) (Object) this;

        //shitton of checks to make sure this only happens with generation
        if (storage.getWorld().isRemote)
            return;

        // Check if currently generating a structure
        if (!RCLootGenContext.STRUCTURE_GEN_RUNNING.get()) {
            //System.out.println("[SuSy][Mixin] Not in structure gen context, skipping loot gen");
            return;
        }

        if (storage.inventory == null) {
            //System.out.println("[SuSy][Mixin] loadFromNBTExtra: inventory is null, skipping");
            return;
        }

        IItemHandlerModifiable handler = new InvWrapper(storage.inventory);
        boolean foundTag = false;

        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty() &&
                    RecurrentComplex.specialRegistry.itemHidingMode().containedItem(handler.getStackInSlot(i)) instanceof GeneratingItem) {
                foundTag = true;
                //System.out.println("[SuSy][Mixin] Found RC loot tag in slot " + i + " of LittleStorage at " + storage.getPos());
                break;
            }
        }

        if (!foundTag) {
            //System.out.println("[SuSy][Mixin] No RC loot tag in loaded inventory, skipping loot gen");
            return;
        }

        //actual loot generation
        WorldServer server = (WorldServer) storage.getWorld();
        LootGenerationHandler.generateAllTags(
                server,
                handler,
                RecurrentComplex.specialRegistry.itemHidingMode(),
                server.rand
        );
        //System.out.println("[SuSy][Mixin] Injected loot into LittleStorage at " + storage.getPos());
    }
}
