package supersymmetry.integration.baubles;

import baubles.api.BaubleType;
import baubles.api.BaublesApi;
import baubles.api.cap.IBaublesItemHandler;
import baubles.api.inv.SlotTypeDefinition;
import gregtech.api.modules.GregTechModule;
import gregtech.common.items.MetaItems;
import gregtech.integration.IntegrationSubmodule;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import supersymmetry.Supersymmetry;
import supersymmetry.api.SusyLog;
import supersymmetry.api.capability.SuSyCapabilities;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.item.behavior.ArmorBaubleBehavior;
import supersymmetry.modules.SuSyModules;

import java.util.Collections;
import java.util.List;

@GregTechModule(
        moduleID = SuSyModules.MODULE_BAUBLES,
        containerID = Supersymmetry.MODID,
        modDependencies = "baubles",
        name = "SuSy Baubles Integration",
        description = "SuSy Baubles Integration Module")
public class BaublesModule extends IntegrationSubmodule {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void registerItems(RegistryEvent.Register<Item> event) {
        MetaItems.SEMIFLUID_JETPACK.addComponents(new ArmorBaubleBehavior(BaubleType.BODY));
        MetaItems.ELECTRIC_JETPACK.addComponents(new ArmorBaubleBehavior(BaubleType.BODY));
        MetaItems.ELECTRIC_JETPACK_ADVANCED.addComponents(new ArmorBaubleBehavior(BaubleType.BODY));
        SuSyMetaItems.JET_WINGPACK.addComponents(new ArmorBaubleBehavior(BaubleType.BODY));
        MetaItems.NIGHTVISION_GOGGLES.addComponents(new ArmorBaubleBehavior(BaubleType.HEAD));

    }

    @NotNull
    @Override
    public List<Class<?>> getEventBusSubscribers() {
        return Collections.singletonList(BaublesModule.class);
    }

    @Override
    public void init(FMLInitializationEvent event) {
        SusyLog.logger.info("Baubles found. Enabling integration...");
    }

    public static ItemStack getElytraBauble(@NotNull EntityLivingBase entity) {
        if (entity instanceof EntityPlayer player) {
            // The body slot is 5
            return BaublesApi.getBaublesHandler(player).getStackInSlot(5);
        }
        return ItemStack.EMPTY;
    }
}
