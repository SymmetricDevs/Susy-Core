package supersymmetry.common.item;

import gregtech.api.items.toolitem.ToolClasses;
import gregtech.api.items.toolitem.ToolDefinitionBuilder;
import supersymmetry.common.item.behavior.PipeNetWalkerBehavior;

public class ToolBehaviorExtender {

    public static void addFirst(ToolDefinitionBuilder b, String... toolClasses) {
        for (String toolClass : toolClasses) {
            switch (toolClass) {
                case ToolClasses.WRENCH, ToolClasses.WIRE_CUTTER -> b.behaviors(PipeNetWalkerBehavior.INSTANCE);
                default -> {
                }
            }
        }
    }
}
