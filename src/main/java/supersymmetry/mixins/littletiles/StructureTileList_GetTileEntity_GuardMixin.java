package supersymmetry.mixins.littletiles;

import com.creativemd.littletiles.common.tile.parent.StructureTileList;
import com.creativemd.littletiles.common.tileentity.TileEntityLittleTiles;
import com.creativemd.littletiles.common.structure.exception.NotYetConnectedException;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Prevent NPEs during explosions by guarding the call to getTe() inside getTileEntity().
 *
 * Original code in StructureTileList#getTileEntity() does: World world = this.getTe().getWorld();
 *  If the parent was nulled by removal (explosion), getTe() NPEs.
 *  We redirect that invocation and:
 *  if this list is detached (isRemoved()), throw NotYetConnectedException (the method already declares this),
 *  otherwise, call the original getTe() via an @Invoker and validate it isn't null/invalid.
 *  Hopefully this doesn't cause problems :troll:
 */
@Mixin(value = StructureTileList.class, remap = false)
public abstract class StructureTileList_GetTileEntity_GuardMixin {

    @Invoker("getTe")
    protected abstract TileEntityLittleTiles susy$invokeGetTe();

    @Redirect( //probably not the best injector, counter argument: its past midnight
            method = "getTileEntity",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/creativemd/littletiles/common/tile/parent/StructureTileList;getTe()Lcom/creativemd/littletiles/common/tileentity/TileEntityLittleTiles;"
            ),
            remap = false
    )
    private TileEntityLittleTiles susy$guardGetTeForGetTileEntity(StructureTileList self) throws NotYetConnectedException {
        if (((StructureTileList) (Object) this).isRemoved()) {
            // Parent was nulled (e.g., exploded out). Signal "not yet connected" instead of NPE.
            throw new NotYetConnectedException(); //this spams console with errors when a LT is exploded but it won't crash anymore.
        }

        TileEntityLittleTiles te = this.susy$invokeGetTe();
        if (te == null || te.isInvalid()) {
            throw new NotYetConnectedException();
        }

        return te;
    }
}
