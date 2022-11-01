package supersymmetry.client;

import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.jetbrains.annotations.NotNull;
import supersymmetry.Supersymmetry;
import supersymmetry.common.CommonProxy;
import supersymmetry.common.blocks.SuSyBlocks;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = Side.CLIENT)
public class ClientProxy extends CommonProxy {

    @SubscribeEvent
    public static void registerModels(@NotNull ModelRegistryEvent event) {
        SuSyBlocks.registerItemModels();
    }
}
