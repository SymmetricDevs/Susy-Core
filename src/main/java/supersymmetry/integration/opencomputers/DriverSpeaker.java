package supersymmetry.integration.opencomputers;

import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import gregtech.api.block.VariantItemBlock;
import li.cil.oc.api.driver.item.HostAware;
import li.cil.oc.api.driver.item.Slot;
import li.cil.oc.api.network.EnvironmentHost;
import li.cil.oc.api.network.ManagedEnvironment;
import supersymmetry.common.blocks.BlockSpeaker;

public class DriverSpeaker implements HostAware {

    @Override
    public boolean worksWith(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (item instanceof VariantItemBlock) {
            Block block = ((VariantItemBlock) item).getBlock();
            if (block instanceof BlockSpeaker) {
                return ((BlockSpeaker) block).getState(stack) == BlockSpeaker.BlockSpeakerType.SINGLE;
            }
        }
        return false;
    }

    @Override
    public boolean worksWith(ItemStack stack, Class<? extends EnvironmentHost> host) {
        return worksWith(stack);
    }

    @Override
    public ManagedEnvironment createEnvironment(ItemStack stack, EnvironmentHost host) {
        if (host.world() == null || host.world().isRemote) return null;

        Block block = ((VariantItemBlock) stack.getItem()).getBlock();
        BlockSpeaker speakerBlock = (BlockSpeaker) block;
        BlockSpeaker.BlockSpeakerType type = speakerBlock.getState(stack);

        if (type != BlockSpeaker.BlockSpeakerType.SINGLE) return null;

        return new ComponentSpeaker(host, "speaker_single", type.getRadius());
    }

    @Override
    public String slot(ItemStack stack) {
        return Slot.Upgrade;
    }

    @Override
    public int tier(ItemStack stack) {
        return 0;
    }

    @Override
    public NBTTagCompound dataTag(ItemStack stack) {
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound nbt = stack.getTagCompound();
        if (!nbt.hasKey("oc:data")) {
            nbt.setTag("oc:data", new NBTTagCompound());
        }
        return nbt.getCompoundTag("oc:data");
    }
}
