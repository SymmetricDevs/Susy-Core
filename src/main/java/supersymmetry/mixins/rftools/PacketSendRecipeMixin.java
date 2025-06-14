package supersymmetry.mixins.rftools;

import io.netty.buffer.ByteBuf;
import java.util.function.Supplier;
import mcjty.lib.thirteen.Context;
import mcjty.lib.varia.ItemStackList;
import mcjty.rftools.blocks.storage.ModularStorageItemContainer;
import mcjty.rftools.blocks.storage.ModularStorageSetup;
import mcjty.rftools.blocks.storage.RemoteStorageItemContainer;
import mcjty.rftools.blocks.storagemonitor.StorageScannerContainer;
import mcjty.rftools.compat.jei.JEIRecipeAcceptor;
import mcjty.rftools.compat.jei.PacketSendRecipe;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import supersymmetry.api.SusyLog;

@Mixin(value = PacketSendRecipe.class, remap = false)
public class PacketSendRecipeMixin {
  @Shadow private ItemStackList stacks;
  @Shadow private BlockPos pos;

  @Inject(method = "fromBytes", at = @At("TAIL"))
  public void fromBytes(ByteBuf buf, CallbackInfo ci) {
    SusyLog.logger.warn("frombytes: {} {}", pos, stacks);
  }

  @Inject(method = "toBytes", at = @At("HEAD"))
  public void toBytes(ByteBuf buf, CallbackInfo ci) {

    SusyLog.logger.warn("tobytes: {} {}", pos, stacks);
  }

  @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
  public void handle(Supplier<Context> supplier, CallbackInfo ci) {
    Context ctx = supplier.get();
    ctx.enqueueWork(
        () -> {
          EntityPlayerMP player = ctx.getSender();
          World world = player.getEntityWorld();
          if (pos == null) {
            // Handle tablet version
            ItemStack mainhand = player.getHeldItemMainhand();
            if (!mainhand.isEmpty()
                && mainhand.getItem() == ModularStorageSetup.storageModuleTabletItem) {
              SusyLog.logger.warn("pos == null somehow????");
              if (player.openContainer instanceof ModularStorageItemContainer) {
                ModularStorageItemContainer storageItemContainer =
                    (ModularStorageItemContainer) player.openContainer;
                storageItemContainer.getJEIRecipeAcceptor().setGridContents(stacks);
              } else if (player.openContainer instanceof RemoteStorageItemContainer) {
                RemoteStorageItemContainer storageItemContainer =
                    (RemoteStorageItemContainer) player.openContainer;
                storageItemContainer.getJEIRecipeAcceptor().setGridContents(stacks);
              } else if (player.openContainer instanceof StorageScannerContainer) {
                StorageScannerContainer storageItemContainer =
                    (StorageScannerContainer) player.openContainer;
                storageItemContainer.getStorageScannerTileEntity().setGridContents(stacks);
              }
            }
          } else {
            TileEntity te = world.getTileEntity(pos);
            if (te instanceof JEIRecipeAcceptor) {
              JEIRecipeAcceptor acceptor = (JEIRecipeAcceptor) te;
              acceptor.setGridContents(stacks);
            }
          }
        });
    ctx.setPacketHandled(true);
    ci.cancel();
  }
}
