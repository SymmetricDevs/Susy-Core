package supersymmetry.common.item.behavior;

import gregtech.api.pipenet.tile.IPipeTile;
import net.minecraft.util.EnumFacing;

import java.util.List;

public interface ITraverseOption {

    List<EnumFacing> findNext(EnumFacing from, IPipeTile<?, ?> pipe);

    void operate(EnumFacing from, IPipeTile<?, ?> self, IPipeTile<?, ?> other, boolean reverse);
}
