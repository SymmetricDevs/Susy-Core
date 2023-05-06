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
import javax.xml.soap.Text;
import java.util.ArrayList;
import java.util.List;
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
        List<String> tooltips = new ArrayList<>();


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
                if (group.getCatalystInfos().getMap().containsKey(is)){
                    String localisedCatalystGroupName = I18n.format("gregtech.catalyst_group." + group.getName() + ".name");
                    CatalystInfo catalystInfo = group.getCatalystInfos().getMap().get(is);
                    tooltips.add(TextFormatting.UNDERLINE + (TextFormatting.DARK_BLUE + I18n.format("gregtech.universal.catalysts.tooltip.title", localisedCatalystGroupName)));
                    tooltips.add((I18n.format("gregtech.universal.catalysts.tooltip.tier", GTValues.V[catalystInfo.getTier()], GTValues.VNF[catalystInfo.getTier()])));
                    if (catalystInfo.getYieldEfficiency() != 1) tooltips.add((I18n.format("gregtech.universal.catalysts.tooltip.yield", catalystInfo.getYieldEfficiency())));
                    if (catalystInfo.getEnergyEfficiency() != 1) tooltips.add((I18n.format("gregtech.universal.catalysts.tooltip.energy", catalystInfo.getEnergyEfficiency())));
                    if (catalystInfo.getSpeedEfficiency() != 1) tooltips.add((I18n.format("gregtech.universal.catalysts.tooltip.speed", catalystInfo.getSpeedEfficiency())));
                }
            }
            
        } 

        if (tooltips != null) {
            for (String s : tooltips) {
                if (s == null || s.isEmpty()) continue;
                event.getToolTip().add(s);
            }
        }
    }


    @SubscribeEvent
    public static void registerModels(@NotNull ModelRegistryEvent event) {
        SuSyBlocks.registerItemModels();
    }
}
