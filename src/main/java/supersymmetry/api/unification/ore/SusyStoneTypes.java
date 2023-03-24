package supersymmetry.api.unification.ore;

import gregtech.api.unification.ore.OrePrefix;
import gregtech.api.unification.ore.StoneType;
import net.minecraft.block.SoundType;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.api.block.SusyBlockStoneSmooth;
import supersymmetry.common.materials.SusyMaterials;

public class SusyStoneTypes {
    public static final StoneType GABBRO = new StoneType(12, "gabbro", SoundType.STONE, OrePrefix.ore, SusyMaterials.Gabbro,
                () -> SuSyBlocks.SUSY_STONE_SMOOTH.getState(SusyBlockStoneSmooth.BlockType.GABBRO),
                state -> state.getBlock() instanceof SusyBlockStoneSmooth && ((SusyBlockStoneSmooth) state.getBlock()).getVariant(state) == SusyBlockStoneSmooth.BlockType.GABBRO, false);

}
