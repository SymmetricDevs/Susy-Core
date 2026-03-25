package supersymmetry.client.renderer.handler;

import java.util.List;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface ITextureRegistrar {

    @SideOnly(Side.CLIENT)
    List<ResourceLocation> getTextureLocations();
}
