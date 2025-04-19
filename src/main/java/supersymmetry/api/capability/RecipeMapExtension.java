package supersymmetry.api.capability;

import net.minecraftforge.fml.common.Mod;
import supersymmetry.Supersymmetry;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID)
public interface RecipeMapExtension {
    void modifyMaxOutputs(int maxOutputs);
}
