package supersymmetry.client.renderer.textures;

import gregtech.client.renderer.texture.cube.SimpleSidedCubeRenderer;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import supersymmetry.Supersymmetry;

@Mod.EventBusSubscriber(modid = Supersymmetry.MODID, value = {Side.CLIENT})
public class SusyTextures {
    public static SimpleSidedCubeRenderer WOODEN_COAGULATION_TANK_WALL;

    public SusyTextures(){
    }

    public static void preInit(){
        WOODEN_COAGULATION_TANK_WALL = new SimpleSidedCubeRenderer("casings/wooden_coagulation_tank_wall");
    }
}
