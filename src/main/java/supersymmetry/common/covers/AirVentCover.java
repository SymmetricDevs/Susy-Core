package supersymmetry.common.covers;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.CoverBehavior;
import gregtech.api.cover.ICoverable;
import gregtech.api.recipes.Recipe;
import gregtech.api.recipes.RecipeMaps;
import gregtech.api.recipes.builders.GasCollectorRecipeBuilder;
import gregtech.api.recipes.recipeproperties.GasCollectorDimensionProperty;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.texture.Textures;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.Optional;

public class AirVentCover extends CoverBehavior implements ITickable {
    private final int airPerSecond;
    private FluidStack cachedAirType;
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
            if (this.coverHolder.getWorld().getBlockState(this.coverHolder.getPos().offset(this.attachedSide)).isFullBlock()) {
                return;
            }
            IFluidHandler fluidHandler = this.coverHolder.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, this.attachedSide);
            if (fluidHandler != null) {
                if (cachedAirType == null) {
                    RecipeMaps.GAS_COLLECTOR_RECIPES.getRecipeList().stream().filter(recipe -> {
                        for (int dimension : recipe.getProperty(GasCollectorDimensionProperty.getInstance(), IntLists.EMPTY_LIST)) {
                            if (dimension == this.coverHolder.getWorld().provider.getDimension()) {
                                return true;
                            }
                        }
                        return false;
                    }).findFirst().ifPresent(recipe -> this.cachedAirType = new FluidStack(recipe.getFluidOutputs().get(0).getFluid(), airPerSecond));
                }
                if (cachedAirType != null)
                    fluidHandler.fill(cachedAirType.copy(), true);
            }
        }

    }

}
