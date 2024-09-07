package supersymmetry.common.item;

import com.google.common.base.Preconditions;
import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.armor.IArmorLogic;
import gregtech.api.items.metaitem.stats.IItemComponent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.NotNull;
import supersymmetry.api.items.IBreathingArmorLogic;
import supersymmetry.api.items.IBreathingItem;


public class SuSyArmorItem extends ArmorMetaItem<SuSyArmorItem.SuSyArmorMetaValueItem> implements IBreathingItem {


    @Override
    public boolean isValid(ItemStack stack, int dimension) {
        return getItem(stack).armorLogic.mayBreatheWith(stack, dimension);
    }

    @Override
    public double tryTick(ItemStack stack, EntityPlayer player, int dimension) {
        return getItem(stack).armorLogic.tryTick(stack, player, dimension);
    }

    public class SuSyArmorMetaValueItem extends ArmorMetaItem<SuSyArmorItem.SuSyArmorMetaValueItem>.ArmorMetaValueItem {
        private IBreathingArmorLogic armorLogic = null;
        protected SuSyArmorMetaValueItem(int metaValue, String unlocalizedName) {
            super(metaValue, unlocalizedName);
            this.setMaxStackSize(1);
        }

        public SuSyArmorMetaValueItem setArmorLogic(IBreathingArmorLogic armorLogic) {
            super.setArmorLogic(armorLogic);
            this.armorLogic = armorLogic;
            if (armorLogic instanceof IItemComponent)
                this.addComponents((IItemComponent) armorLogic);
            return this;
        }

    }

    protected SuSyArmorMetaValueItem constructMetaValueItem(short metaValue, String unlocalizedName) {
        return new SuSyArmorMetaValueItem(metaValue, unlocalizedName);
    }

}
