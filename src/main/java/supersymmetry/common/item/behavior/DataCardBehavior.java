package supersymmetry.common.item.behavior;

import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants;

import org.jetbrains.annotations.NotNull;

import gregtech.api.items.metaitem.stats.IItemBehaviour;

public class DataCardBehavior implements IItemBehaviour {

    private final Consumer<List<String>> lines;
    private final List<String> keys;

    public DataCardBehavior(@NotNull Consumer<List<String>> lines, List<String> keys) {
        this.lines = lines;
        this.keys = keys;
    }

    @Override
    public void addInformation(ItemStack itemStack, List<String> lines) {
        this.lines.accept(lines);
        if (itemStack.hasTagCompound()) {
            NBTTagCompound tag = itemStack.getTagCompound();
            for (String key : this.keys) {
                if (tag.hasKey(key, Constants.NBT.TAG_STRING)) {
                    lines.add(I18n.format(itemStack.getTranslationKey() + ".tag." + tag.getString(key)));
                }
            }
        }
    }
}
