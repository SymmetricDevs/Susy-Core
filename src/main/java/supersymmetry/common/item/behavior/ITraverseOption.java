package supersymmetry.common.item.behavior;

import java.util.List;

import net.minecraft.util.EnumFacing;

import gregtech.api.pipenet.tile.IPipeTile;

public interface ITraverseOption {

    List<EnumFacing> findNext(EnumFacing from, IPipeTile<?, ?> pipe);

    void operate(EnumFacing from, IPipeTile<?, ?> self, IPipeTile<?, ?> other, boolean reverse);
}
