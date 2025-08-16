package supersymmetry.common.tileentities;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import supersymmetry.client.renderer.handler.AnimatablePartRenderer;

import static supersymmetry.api.util.SuSyUtility.susyId;

public class SuSyTileEntities {

    public static void register() {
        GameRegistry.registerTileEntity(AnimatablePartTileEntity.class, susyId("animatable_part"));
    }

    @SideOnly(Side.CLIENT)
    public static void registerRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(AnimatablePartTileEntity.class, new AnimatablePartRenderer());
    }
}
