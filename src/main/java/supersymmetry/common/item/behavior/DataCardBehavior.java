package supersymmetry.common.item.behavior;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.ISubItemHandler;
import gregtech.api.util.GTUtility;
import supersymmetry.api.rocketry.components.AbstractComponent;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.rocketry.SusyRocketComponents;

public class DataCardBehavior implements IItemBehaviour, ISubItemHandler {

    private final Consumer<List<String>> lines;
    private final List<String> keys;

    public DataCardBehavior(@NotNull Consumer<List<String>> lines, List<String> keys) {
        this.lines = lines;
        this.keys = keys;
    }

    @Override
    public String getItemSubType(ItemStack itemStack) {
        var tag = GTUtility.getOrCreateNbtCompound(itemStack);
        return tag.getString("name");
    }

    @Override
    public void getSubItems(ItemStack itemStack, CreativeTabs creativeTab, NonNullList<ItemStack> subItems) {
        subItems.add(itemStack.copy());
        if (itemStack.getMetadata() == SuSyMetaItems.DATA_CARD_ACTIVE.metaValue) {
            for (String name : AbstractComponent.getNameRegistry().keySet()) {
                AbstractComponent<?> component = AbstractComponent.getComponentFromName(name);
                if (component == null) continue;
                if (!component.configureDefaults()) continue;
                ItemStack configured = itemStack.copy();
                NBTTagCompound tag = new NBTTagCompound();
                component.writeToNBT(tag);
                configured.setTagCompound(tag);
                subItems.add(configured);
            }
        }
        if (itemStack.getMetadata() == SuSyMetaItems.DATA_CARD_MASTER_BLUEPRINT.metaValue && SusyRocketComponents.ROCKET_SOYUZ_BLUEPRINT_DEFAULT != null) {
            ItemStack configured = itemStack.copy();
            NBTTagCompound tag = SusyRocketComponents.ROCKET_SOYUZ_BLUEPRINT_DEFAULT.writeToNBT();
            tag.setString("rocketType", "soyuz");
            configured.setTagCompound(tag);
            subItems.add(configured);
        }
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        this.lines.accept(lines);
        NBTTagCompound tag = itemStack.getTagCompound();
        if (tag == null) return;

        for (String key : this.keys) {
            if (tag.hasKey(key, Constants.NBT.TAG_STRING)) {
                lines.add(
                        I18n.format(itemStack.getTranslationKey() + ".tag." + tag.getString(key)) + " ID: " +
                                getID(tag));
            }
        }

        if (tag.hasKey("name", Constants.NBT.TAG_STRING)) {
            String targetName = tag.getString("name");
            for (AbstractComponent<?> component : AbstractComponent.getRegistry()) {
                if (component.getName().equals(targetName)) {
                    lines.addAll(component.getTooltipLines(tag));
                    break;
                }
            }
        }
    }

    private String getID(NBTTagCompound key) {
        // Left pad
        String fullID = String.format("%08x", key.hashCode());
        return fullID.toUpperCase();
    }
}
