package supersymmetry.mixins.rftools;

import com.llamalad7.mixinextras.sugar.Local;
import mcjty.lib.gui.widgets.BlockRender;
import mcjty.rftools.blocks.storagemonitor.GuiStorageScanner;
import mcjty.rftools.blocks.storagemonitor.PacketReturnInventoryInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import supersymmetry.api.bugfixes.IItemStackInfo;

@Mixin(GuiStorageScanner.class)
public class GuiStorageScannerMixin {

    @Redirect(method = "addStorageLine",
            remap = false,
            at = @At(value = "INVOKE",
                    target = "Lmcjty/lib/gui/widgets/BlockRender;setRenderItem(Ljava/lang/Object;)Lmcjty/lib/gui/widgets/BlockRender;",
                    ordinal = 0))
    private BlockRender setRenderItemCorrectly(BlockRender renderer, Object object, @Local(argsOnly = true) PacketReturnInventoryInfo.InventoryInfo info) {
        return renderer.setRenderItem(((IItemStackInfo) info).getStack());
    }
}
