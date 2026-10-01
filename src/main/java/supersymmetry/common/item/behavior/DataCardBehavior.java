package supersymmetry.common.item.behavior;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

import org.jetbrains.annotations.NotNull;

import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.common.item.SuSyMetaItems;

public class DataCardBehavior extends AbstractCardBehavior {

    public DataCardBehavior(@NotNull Consumer<List<String>> lines, List<String> keys) {
        super(lines, keys);
    }

    @Override
    public void getSubItems(ItemStack itemStack, CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        subItems.add(itemStack.copy());
        if (itemStack.getMetadata() == SuSyMetaItems.DATA_CARD_ACTIVE.metaValue) {
            for (String name : AbstractComponent.getRegisteredNames()) {
                AbstractComponent<?> component = AbstractComponent.getComponentFromName(name);
                if (component == null || !component.configureDefaults())
                    continue;
                ItemStack configured = itemStack.copy();
                NBTTagCompound tag = new NBTTagCompound();
                component.writeToNBT(tag);
                configured.setTagCompound(tag);
                subItems.add(configured);
            }
        }
    }
}
