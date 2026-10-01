package supersymmetry.common.item.behavior;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;

import gregtech.api.items.metaitem.stats.IItemBehaviour;
import gregtech.api.items.metaitem.stats.ISubItemHandler;
import gregtech.api.util.GTUtility;
import supersymmetry.api.rocketry.components.AbstractComponent;

public abstract class AbstractCardBehavior implements IItemBehaviour, ISubItemHandler {

    private final Consumer<List<String>> lines;
    private final List<String> keys;

    protected AbstractCardBehavior(@NotNull Consumer<List<String>> lines, List<String> keys) {
        this.lines = lines;
        this.keys = keys;
    }

    @Override
    public String getItemSubType(ItemStack itemStack) {
        return GTUtility.getOrCreateNbtCompound(itemStack).getString("name");
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        this.lines.accept(lines);
        NBTTagCompound tag = itemStack.getTagCompound();
        if (tag == null)
            return;

        for (String key : this.keys) {
            if (tag.hasKey(key, Constants.NBT.TAG_STRING)) {
                String line = I18n
                        .format(String.format("%s.tag.%s", itemStack.getTranslationKey(), tag.getString(key)));
                lines.add(showCardId(tag) ? String.format("%s ID: %s", line, getID(tag)) : line);
            }
        }

        AbstractComponent<?> component = AbstractComponent.getComponentFromName(tag.getString("name"));
        if (component != null) {
            lines.addAll(component.getTooltipLines(tag));
        }
    }

    protected boolean showCardId(NBTTagCompound tag) {
        return true;
    }

    private String getID(NBTTagCompound tag) {
        return String.format("%08x", tag.hashCode()).toUpperCase();
    }
}
