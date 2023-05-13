package supersymmetry.client;

import gregtech.api.GTValues;
import gregtech.api.items.metaitem.MetaOreDictItem;
import gregtech.api.unification.OreDictUnifier;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.Supersymmetry;
import supersymmetry.api.recipes.catalysts.CatalystGroup;
import supersymmetry.api.recipes.catalysts.CatalystInfo;
import supersymmetry.client.renderer.textures.SusyTextures;
import supersymmetry.common.CommonProxy;
import supersymmetry.common.SusyMetaEntities;
import supersymmetry.common.blocks.SuSyBlocks;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    public void preLoad() {
        super.preLoad();
        SusyTextures.preInit();
        SusyMetaEntities.initRenderers();
    }

    @SubscribeEvent
    public static void addCatalystTooltipHandler(@Nonnull ItemTooltipEvent event) {
        ItemStack itemStack = event.getItemStack();
        // Handles Item tooltips
        Collection<String> tooltips = new ArrayList<>();

        if (itemStack.getItem() instanceof MetaOreDictItem) { // Test for OreDictItems
            MetaOreDictItem oreDictItem = (MetaOreDictItem) itemStack.getItem();
            Optional<String> oreDictName = OreDictUnifier.getOreDictionaryNames(itemStack).stream().findFirst();
            if (oreDictName.isPresent() && oreDictItem.OREDICT_TO_FORMULA.containsKey(oreDictName.get()) && !oreDictItem.OREDICT_TO_FORMULA.get(oreDictName.get()).isEmpty()) {
                tooltips.add(TextFormatting.YELLOW + oreDictItem.OREDICT_TO_FORMULA.get(oreDictName.get()));
            }

            for (CatalystGroup group :
                    CatalystGroup.getCatalystGroups()) {
                ItemStack is = itemStack.copy();
                is.setCount(1);
                CatalystInfo catalystInfo = group.getCatalystInfos().get(is);
                if (catalystInfo != null) {
                    tooltips.add(TextFormatting.UNDERLINE + (TextFormatting.BLUE + I18n.format("gregtech.catalyst_group." + group.getName() + ".name")));
                    if(catalystInfo.getTier() == CatalystInfo.NO_TIER){
                        tooltips.add(TextFormatting.RED + "Disclaimer: Catalyst bonuses for non-tiered catalysts have not yet been implemented.");
                        tooltips.add(I18n.format("gregtech.universal.catalysts.tooltip.yield", catalystInfo.getYieldEfficiency()));
                        tooltips.add(I18n.format("gregtech.universal.catalysts.tooltip.energy", catalystInfo.getEnergyEfficiency()));
                        tooltips.add(I18n.format("gregtech.universal.catalysts.tooltip.speed", catalystInfo.getSpeedEfficiency()));
                    } else {
                        tooltips.add(I18n.format("gregtech.universal.catalysts.tooltip.tier", GTValues.V[catalystInfo.getTier()], GTValues.VNF[catalystInfo.getTier()]));
                        tooltips.add(I18n.format("gregtech.universal.catalysts.tooltip.yield.tiered", catalystInfo.getYieldEfficiency()));
                        tooltips.add(I18n.format("gregtech.universal.catalysts.tooltip.energy.tiered", catalystInfo.getEnergyEfficiency()));
                        tooltips.add(I18n.format("gregtech.universal.catalysts.tooltip.speed.tiered", catalystInfo.getSpeedEfficiency()));
                    }
                }
            }

        }

        event.getToolTip().addAll(tooltips);
    }


    @SubscribeEvent
    public static void registerModels(@NotNull ModelRegistryEvent event) {
        SuSyBlocks.registerItemModels();
    }
}
