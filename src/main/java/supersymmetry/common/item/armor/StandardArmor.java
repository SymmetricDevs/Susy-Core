package supersymmetry.common.item.armor;

import gregtech.api.items.metaitem.stats.IItemDurabilityManager;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import supersymmetry.api.items.IStandardArmorLogic;
import supersymmetry.client.renderer.handler.ITextureRegistrar;

import java.util.ArrayList;
import java.util.List;

import static net.minecraft.inventory.EntityEquipmentSlot.CHEST;
import static supersymmetry.api.util.SuSyUtility.susyId;

public class StandardArmor implements ITextureRegistrar, IStandardArmorLogic {
    @SideOnly(Side.CLIENT)
    protected ModelBiped Model;
    private final String name;
    private final int tier;
    protected final EntityEquipmentSlot SLOT;

    public StandardArmor(EntityEquipmentSlot slot, String name, int tier) {
        SLOT = slot;
        this.name = name;
        this.tier = tier;
    }

    @Override
    public EntityEquipmentSlot getEquipmentSlot(ItemStack itemstack) {return SLOT;}

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EntityEquipmentSlot slot, String type) {
        return "susy:textures/armor/" + name + "_" + slot.getName() + ".png";
    }

    protected float getAbsorption(ItemStack itemStack) {
        return getAbsorption(getEquipmentSlot(itemStack));
    }

    protected float getAbsorption(EntityEquipmentSlot slot) {
        return switch (SLOT) {
            case HEAD, FEET -> 0.15F;
            case CHEST -> 0.4F;
            case LEGS -> 0.3F;
            default -> 0.0F;
        };
    }

    @Override
    @SideOnly(Side.CLIENT)
    public List<ResourceLocation> getTextureLocations() {
        List<ResourceLocation> models = new ArrayList<>();
        models.add(susyId("armor/" + name + "_" + this.SLOT.toString()));
        return models;
    }

    public void addInformation(ItemStack stack, List<String> tooltips) {
        if (getEquipmentSlot(stack) == CHEST) {
            tooltips.add(I18n.format("supersymmetry.steel_armor"));
        }
    }
}
