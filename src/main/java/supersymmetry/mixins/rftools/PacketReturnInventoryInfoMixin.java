package supersymmetry.mixins.rftools;

import io.netty.buffer.ByteBuf;
import mcjty.lib.network.NetworkTools;
import mcjty.rftools.blocks.storagemonitor.PacketReturnInventoryInfo;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import supersymmetry.api.bugfixes.IItemStackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(PacketReturnInventoryInfo.class)
public class PacketReturnInventoryInfoMixin {

    @Shadow(remap = false)
    private List<PacketReturnInventoryInfo.InventoryInfo> inventories;

    /**
     * @author Tian_mi
     * @reason Ask mcjty
     */
    @Overwrite(remap = false)
    public void fromBytes(ByteBuf buf) {
        int size = buf.readInt();
        inventories = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            BlockPos pos = NetworkTools.readPos(buf);
            String name = NetworkTools.readString(buf);
            boolean routable = buf.readBoolean();
            ItemStack stack = ItemStack.EMPTY;
            if (buf.readBoolean()) {
                stack = NetworkTools.readItemStack(buf);
            }
            var info = new PacketReturnInventoryInfo.InventoryInfo(pos, name, routable, Blocks.AIR);
            ((IItemStackInfo) info).setStack(stack);
            inventories.add(info);
        }
    }

    /**
     * @author Tian_mi
     * @reason Ask mcjty
     */
    @Overwrite(remap = false)
    public void toBytes(ByteBuf buf) {
        buf.writeInt(inventories.size());
        for (PacketReturnInventoryInfo.InventoryInfo info : inventories) {
            NetworkTools.writePos(buf, info.getPos());
            NetworkTools.writeString(buf, info.getName());
            buf.writeBoolean(info.isRoutable());
            ItemStack stack = ((IItemStackInfo) info).getStack();
            if (stack.isEmpty()) {
                buf.writeBoolean(false);
            } else {
                buf.writeBoolean(true);
                NetworkTools.writeItemStack(buf, stack);
            }
        }
    }
}
