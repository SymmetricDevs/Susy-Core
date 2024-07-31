package supersymmetry.mixins.rftools;

import gregtech.api.metatileentity.interfaces.IGregTechTileEntity;
import gregtech.api.util.GTUtility;
import gregtech.api.util.GregFakePlayer;
import gregtech.integration.jei.multiblock.MultiblockInfoRecipeWrapper;
import mcjty.lib.varia.WorldTools;
import mcjty.rftools.blocks.storage.ModularStorageContainer;
import mcjty.rftools.blocks.storage.ModularStorageTileEntity;
import mcjty.rftools.blocks.storagemonitor.PacketGetInventoryInfo;
import mcjty.rftools.blocks.storagemonitor.PacketReturnInventoryInfo;
import mcjty.rftools.blocks.storagemonitor.StorageScannerTileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import supersymmetry.api.bugfixes.IItemStackInfo;

import java.util.Map;
import java.util.Set;

@Mixin(PacketGetInventoryInfo.class)
public class PacketGetInventoryInfoMixin {

    /**
     * @author Tian_mi
     * @reason Ask mcjty
     */
    @Overwrite(remap = false)
    private static PacketReturnInventoryInfo.InventoryInfo toInventoryInfo(World world, BlockPos pos, StorageScannerTileEntity te) {

        ItemStack stack = ItemStack.EMPTY;
        String displayName;

        if (!WorldTools.chunkLoaded(world, pos)) {
            displayName = "[UNLOADED]";
        } else if (world.isAirBlock(pos)) {
            displayName = "[REMOVED]";
        } else {

            stack = supersymmetry$getItemProperly(world, pos);
            displayName = stack.getDisplayName();

            TileEntity storageTe = world.getTileEntity(pos);
            if (storageTe instanceof ModularStorageTileEntity) {
                ModularStorageTileEntity storageTileEntity = (ModularStorageTileEntity) storageTe;
                ItemStack storageModule = storageTileEntity.getStackInSlot(ModularStorageContainer.SLOT_STORAGE_MODULE);
                if (!storageModule.isEmpty()) {
                    if (storageModule.getTagCompound().hasKey("display")) {
                        displayName = storageModule.getDisplayName();
                    }
                }
            }
        }

        var info = new PacketReturnInventoryInfo.InventoryInfo(pos, displayName, te.isRoutable(pos), Blocks.AIR);
        ((IItemStackInfo) info).setStack(stack);
        return info;
    }

    /**
     * To make XNet happy with ceu machines.
     * Copied From {@link MultiblockInfoRecipeWrapper#gatherStructureBlocks(World, Map, Set)}.
     */
    @Unique
    private static ItemStack supersymmetry$getItemProperly(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos);

        ItemStack stack = ItemStack.EMPTY;

        // first check if the block is a GT machine
        TileEntity tileEntity = world.getTileEntity(pos);
        if (tileEntity instanceof IGregTechTileEntity) {
            stack = ((IGregTechTileEntity) tileEntity).getMetaTileEntity().getStackForm();
        }
        if (stack.isEmpty()) {
            // first, see what the block has to say for itself before forcing it to use a particular meta value
            stack = state.getBlock().getPickBlock(state, new RayTraceResult(Vec3d.ZERO, EnumFacing.UP, pos), world, pos,
                    new GregFakePlayer(world));
        }
        if (stack.isEmpty()) {
            // try the default itemstack constructor if we're not a GT machine
            stack = GTUtility.toItem(state);
        }
        if (stack.isEmpty()) {
            // add the first of the block's drops if the others didn't work
            NonNullList<ItemStack> list = NonNullList.create();
            state.getBlock().getDrops(list, world, pos, state, 0);
            if (!list.isEmpty()) {
                ItemStack is = list.get(0);
                if (!is.isEmpty()) {
                    stack = is;
                }
            }
        }
        return stack;
    }
}
