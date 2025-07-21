package supersymmetry.common.rocketry.components;

import supersymmetry.api.rocketry.components.AbstractComponent;

//        Predicate<BlockPos> spacecraftDetect = bp -> getWorld().getBlockState(bp).getBlock()
//                .equals(SuSyBlocks.SPACECRAFT_HULL);

public class componentSpacecraft extends AbstractComponent<componentSpacecraft> {
    public componentSpacecraft() {
        super("spacecraft", "spacecraft", (blocks) -> {    });
    }
}
