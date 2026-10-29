package supersymmetry.mixins.rftools;

import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;
import supersymmetry.common.metatileentities.storage.MetaTileEntityLockedCrate;

import java.util.List;

@Mixin(value = StorageScannerTileEntity.class, remap = false)
public class StorageScannerTileEntityMixin {

    @Unique
    private boolean susy$isLockedCrate(BlockPos pos) {
        TileEntity self = (TileEntity) (Object) this;
        if (self.getWorld() == null || pos == null || pos.getY() < 0) return false;
        // don't force-load chunks
        if (!self.getWorld().isBlockLoaded(pos)) return false;
        TileEntity te = self.getWorld().getTileEntity(pos);
        return te instanceof IGregTechTileEntity gt &&
                gt.getMetaTileEntity() instanceof MetaTileEntityLockedCrate;
    }

    // Covers crafting, inserting, requesting (all of them gate on isRoutable)
    @Inject(method = "isRoutable", at = @At("HEAD"), cancellable = true)
    private void susy$noRoutableLockedCrates(BlockPos p, CallbackInfoReturnable<Boolean> cir) {
        if (susy$isLockedCrate(p)) cir.setReturnValue(false);
    }

    // Covers search, item counting, and hides the crate from the inventory list
    @Inject(method = "isValid", at = @At("HEAD"), cancellable = true)
    private void susy$noValidLockedCrates(BlockPos p, CallbackInfoReturnable<Boolean> cir) {
        if (susy$isLockedCrate(p)) cir.setReturnValue(false);
    }

    // Stops the GUI from listing the contents when the crate's position is selected directly
    @Inject(method = "addItemsFromInventory", at = @At("HEAD"), cancellable = true)
    private void susy$noContentsListing(BlockPos cpos, Set<Item> foundItems, List<ItemStack> stacks,
                                        CallbackInfo ci) {
        if (susy$isLockedCrate(cpos)) ci.cancel();
    }
}
