package supersymmetry.common.pipelike.tanklessfluid.tile;

import net.minecraft.util.ITickable;

public class TileEntityTanklessFluidPipeTickable extends TileEntityTanklessFluidPipe implements ITickable {

    @Override
    public boolean supportsTicking() {
        return true;
    }

    @Override
    public void update() {
        getCoverableImplementation().update();
    }
}
