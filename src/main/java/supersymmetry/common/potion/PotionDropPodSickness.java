package supersymmetry.common.potion;

import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;
import supersymmetry.Supersymmetry;

public class PotionDropPodSickness extends Potion {

    public static final PotionDropPodSickness INSTANCE = new PotionDropPodSickness();

    private PotionDropPodSickness() {
        super(false, 0xB7B865);
        this.setRegistryName(new ResourceLocation(Supersymmetry.MODID, "droppod_sickness"));
        this.setPotionName("effect.droppod_sickness");
    }

    public static int getId() {
        return Potion.getIdFromPotion(INSTANCE);
    }
}
