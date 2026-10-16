package supersymmetry.mixins.xnet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import net.minecraft.client.resources.I18n;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import mcjty.lib.gui.widgets.TextField;
import mcjty.lib.gui.widgets.WidgetList;
import mcjty.xnet.api.keys.SidedPos;
import mcjty.xnet.blocks.controller.gui.GuiController;
import mcjty.xnet.clientinfo.ConnectedBlockClientInfo;

@Mixin(value = GuiController.class, remap = false)
public abstract class GuiControllerMixin {

    @Shadow
    private TextField searchBar;
    @Shadow
    private WidgetList connectorList;
    @Shadow
    private List<SidedPos> connectorPositions;
    @Shadow
    private SidedPos editingConnector;
    @Shadow
    private int delayedSelectedChannel;
    @Shadow
    private SidedPos delayedSelectedConnector;

    @Unique private List<ConnectedBlockClientInfo> susy$visibleConnectors = Collections.emptyList();
    @Unique private SidedPos susy$selectedConnector;
    @Unique private String susy$previousSearch = "";
    @Unique private boolean susy$searchChanged;

    @Inject(method = "populateList",
            at = @At(value = "INVOKE", target = "Lmcjty/lib/gui/widgets/WidgetList;removeChildren()V"),
            require = 1)
    private void susy$rememberSelection(CallbackInfo ci) {
        int selected = connectorList.getSelected();
        susy$selectedConnector = delayedSelectedChannel != -1 ? delayedSelectedConnector :
                selected >= 0 && selected < connectorPositions.size() ? connectorPositions.get(selected) : null;
        String search = searchBar.getText().trim().toLowerCase(Locale.ROOT);
        susy$searchChanged = !search.equals(susy$previousSearch);
        susy$previousSearch = search;
    }

    @ModifyExpressionValue(method = "populateList",
                           at = @At(value = "FIELD",
                                    target = "Lmcjty/xnet/blocks/controller/gui/GuiController;fromServer_connectedBlocks:Ljava/util/List;",
                                    opcode = Opcodes.GETSTATIC),
                           require = 1)
    private List<ConnectedBlockClientInfo> susy$filterConnectors(List<ConnectedBlockClientInfo> connectors) {
        List<ConnectedBlockClientInfo> visible = new ArrayList<>();
        for (ConnectedBlockClientInfo connector : connectors) {
            String name = connector.getName();
            if (name.isEmpty()) {
                name = I18n.format(connector.getBlockUnlocName()).trim();
            }
            if (name.toLowerCase(Locale.ROOT).contains(susy$previousSearch)) {
                visible.add(connector);
            }
        }
        susy$visibleConnectors = visible;
        return visible;
    }

    @ModifyExpressionValue(method = "hilightSelectedContainer",
                           at = @At(value = "FIELD",
                                    target = "Lmcjty/xnet/blocks/controller/gui/GuiController;fromServer_connectedBlocks:Ljava/util/List;",
                                    opcode = Opcodes.GETSTATIC),
                           require = 1)
    private List<ConnectedBlockClientInfo> susy$highlightVisibleConnector(List<ConnectedBlockClientInfo> connectors) {
        return susy$visibleConnectors;
    }

    @Inject(method = "populateList", at = @At("TAIL"), require = 1)
    private void susy$restoreSelection(CallbackInfo ci) {
        // Row numbers change when filtering; keep selection tied to the actual connector.
        connectorList.setSelected(connectorPositions.indexOf(susy$selectedConnector));
        if (editingConnector != null && !connectorPositions.contains(editingConnector)) {
            editingConnector = null;
        }
        int lastFirst = Math.max(0, connectorPositions.size() - connectorList.getCountSelected());
        connectorList.setFirstSelected(susy$searchChanged ? 0 :
                Math.max(0, Math.min(connectorList.getFirstSelected(), lastFirst)));
    }
}
