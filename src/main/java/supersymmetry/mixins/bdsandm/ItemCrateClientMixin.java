package supersymmetry.mixins.bdsandm;

import funwayguy.bdsandm.inventory.capability.BdsmCapabilies;
import funwayguy.bdsandm.inventory.capability.ICrate;
import funwayguy.bdsandm.items.ItemCrate;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DecimalFormat;
import java.util.List;

@Mixin(ItemCrate.class)
public class ItemCrateClientMixin extends ItemBlock {

    /**
     * @author Bruberu
     * @reason It was literally using an assert in client-side logic, causing crashes in servers.
     */
    @Overwrite
    @SideOnly(Side.CLIENT)
    public void addInformation(@Nonnull ItemStack stack, @Nullable World worldIn, @Nonnull List<String> tooltip, @Nonnull ITooltipFlag flagIn) {
        super.addInformation(stack, worldIn, tooltip, flagIn);
        ICrate crate = stack.getCapability(BdsmCapabilies.CRATE_CAP, null);

        if (crate == null) {
            return;
        }

        if (!crate.getRefItem().isEmpty()) {
            tooltip.add("Item: " + crate.getRefItem().getDisplayName());
            tooltip.add("Amount: " + formatValue(crate.getCount()));
        } else {
            tooltip.add("[EMPTY]");
        }
    }

    private static final DecimalFormat df = new DecimalFormat("0.##");
    private static final String[] suffixes = new String[]{"", "K", "M", "B", "T"};

    private static String formatValue(long value) {
        String s = "";
        double n = 1.0F;

        for(int i = suffixes.length - 1; i >= 0; --i) {
            n = Math.pow(1000.0F, i);
            if ((double)Math.abs(value) >= n) {
                s = suffixes[i];
                break;
            }
        }

        return df.format((double)value / n) + s;
    }
}
