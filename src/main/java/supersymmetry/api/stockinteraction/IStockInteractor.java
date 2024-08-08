package supersymmetry.api.stockinteraction;

import net.minecraft.util.math.AxisAlignedBB;

// Is this really necessary?
public interface IStockInteractor
{
    // Defines the area in which the machine can find and interact with stocks
    AxisAlignedBB getInteractionBoundingBox();
}
