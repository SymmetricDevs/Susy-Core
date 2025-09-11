package supersymmetry.common.item;

import gregtech.api.items.armor.ArmorMetaItem;
import gregtech.api.items.metaitem.stats.IItemComponent;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import supersymmetry.api.items.IBreathingArmorLogic;
import supersymmetry.api.items.IStandardArmorLogic;
import supersymmetry.api.items.IBreathingItem;
import supersymmetry.client.renderer.handler.ITextureRegistrar;


public class SuSyArmorItem extends ArmorMetaItem<SuSyArmorItem.SuSyArmorMetaValueItem> implements IBreathingItem {


    @Override
    public boolean isValid(ItemStack stack, EntityPlayer player) {
        return getItem(stack).armorLogic.mayBreatheWith(stack, player);
    }

    @Override
    public double getDamageAbsorbed(ItemStack stack, EntityPlayer player) {
        return getItem(stack).armorLogic.getDamageAbsorbed(stack, player);
    }

    public class SuSyArmorMetaValueItem extends ArmorMetaItem<SuSyArmorMetaValueItem>.ArmorMetaValueItem {
        private IBreathingArmorLogic armorLogic = null;
        private IStandardArmorLogic standardArmorLogic = null;
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

        public SuSyArmorMetaValueItem setArmorLogic(IStandardArmorLogic standardArmorLogic) {
            super.setArmorLogic(standardArmorLogic);
            this.standardArmorLogic = standardArmorLogic;
            if (standardArmorLogic instanceof IItemComponent)
                this.addComponents((IItemComponent) standardArmorLogic);
            return this;
        }

    }

    protected SuSyArmorMetaValueItem constructMetaValueItem(short metaValue, String unlocalizedName) {
        return new SuSyArmorMetaValueItem(metaValue, unlocalizedName);
    }

    public void registerIngameModels(TextureMap map) {
        for (SuSyArmorMetaValueItem item : this.getAllItems()) {
            if (item.getArmorLogic() instanceof ITextureRegistrar registrar) {
                for (ResourceLocation model : registrar.getTextureLocations()) {
                    map.registerSprite(model);
                }
            }
        }
    }
}
