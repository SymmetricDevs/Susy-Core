package supersymmetry.common.tileentities;

import static supersymmetry.api.util.SuSyUtility.susyId;

import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import supersymmetry.client.renderer.handler.AnimatablePartRenderer;

public class SuSyTileEntities {

    public static void register() {
        GameRegistry.registerTileEntity(AnimatablePartTileEntity.class, susyId("animatable_part"));
        GameRegistry.registerTileEntity(TileEntityCoverable.class, susyId("coverable"));
    }

    @SideOnly(Side.CLIENT)
    public static void registerRenderers() {
        ClientRegistry.bindTileEntitySpecialRenderer(AnimatablePartTileEntity.class, new AnimatablePartRenderer());
    }
}
