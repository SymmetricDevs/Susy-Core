package supersymmetry.common.worldgen;

import ivorius.reccomplex.world.gen.feature.WorldStructureGenerationData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.DecorateBiomeEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SuSyTerrainGenEventHandler {
    public void init() {
        MinecraftForge.TERRAIN_GEN_BUS.register(this);
    }

    @SubscribeEvent
    public void onDecoration(DecorateBiomeEvent.Decorate event) {
        if (event.getType() == DecorateBiomeEvent.Decorate.EventType.TREE) {
            List<WorldStructureGenerationData.Entry> entries = WorldStructureGenerationData.get(event.getWorld()).entriesAt(event.getPlacementPos())
                    .collect(Collectors.toCollection(ArrayList::new));
            if(!entries.isEmpty()) {
                event.setResult(Event.Result.DENY);
            }
        }
    }
}
