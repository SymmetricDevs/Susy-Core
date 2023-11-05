package supersymmetry.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.texture.Textures;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class AirVentCover extends CoverBehavior implements ITickable {
    private final int airPerSecond;
    public AirVentCover(ICoverable coverHolder, EnumFacing attachedSide, int airPerSecond) {
        super(coverHolder, attachedSide);
        this.airPerSecond = airPerSecond;
    }

    public boolean canAttach() {
        return this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.attachedSide) != null;
    }

    public void renderCover(CCRenderState ccRenderState, Matrix4 matrix4, IVertexOperation[] iVertexOperations, Cuboid6 cuboid6, BlockRenderLayer blockRenderLayer) {
        Textures.AIR_VENT_OVERLAY.renderSided(this.attachedSide, cuboid6, ccRenderState, iVertexOperations, matrix4);
    }

    public void update() {
        if (!this.coverHolder.getWorld().isRemote && this.coverHolder.getOffsetTimer() % 20L == 0L) {
            IFluidHandler fluidHandler = this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.attachedSide);
            if (fluidHandler != null) {
                fluidHandler.fill(Materials.Air.getFluid(airPerSecond), true);
            }
        }

    }

}
