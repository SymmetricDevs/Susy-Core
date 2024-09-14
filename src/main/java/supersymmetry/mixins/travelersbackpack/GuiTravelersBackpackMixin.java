package supersymmetry.mixins.travelersbackpack;

import com.tiviacz.travelersbackpack.gui.GuiTravelersBackpack;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = GuiTravelersBackpack.class, remap = false)
public class GuiTravelersBackpackMixin {

    @Redirect(method = "mouseClicked",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraftforge/fml/common/network/simpleimpl/SimpleNetworkWrapper;sendToServer(Lnet/minecraftforge/fml/common/network/simpleimpl/IMessage;)V",
                    ordinal = 0))
    protected void noBedsForYa(SimpleNetworkWrapper instance, IMessage nope) {}
}
