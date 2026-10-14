package supersymmetry.client.gui;

import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import supersymmetry.Supersymmetry;
import supersymmetry.common.item.SuSyMetaItems;
import supersymmetry.common.item.behavior.SkywatchChipBehaviour;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = Side.CLIENT)
public class SkywatchClientRegistry {

    @SubscribeEvent
    public static void onModelRegistry(ModelRegistryEvent event) {
        if (SuSyMetaItems.SKYWATCH_CHIP == null) return;

        Item item = SuSyMetaItems.SKYWATCH_CHIP.getStackForm().getItem();
        item.addPropertyOverride(new ResourceLocation(Supersymmetry.MODID, "skywatch_alert"),
                (stack, world, entity) -> SkywatchChipBehaviour.isAlert(stack) ? 1.0F : 0.0F);
    }
}
