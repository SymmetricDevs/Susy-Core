package supersymmetry.common.item;

import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.behavior.IToolBehavior;
import supersymmetry.common.item.behavior.PipeNetWalkerBehavior;

import java.util.List;

public class ToolBehaviorExtender {

    public static void registerExtra(List<IToolBehavior> b, String... toolClasses) {
        for (String toolClass : toolClasses) {
            switch (toolClass) {
                case ToolClasses.WRENCH, ToolClasses.WIRE_CUTTER -> b.add(0, PipeNetWalkerBehavior.INSTANCE);
                default -> {
                    /* Do nothing */
                }
            }
        }
    }
}
