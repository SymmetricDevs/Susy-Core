package supersymmetry.common.covers;

import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import org.jetbrains.annotations.NotNull;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.render.pipeline.IVertexOperation;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Matrix4;
import gregtech.api.cover.CoverBase;
import gregtech.api.cover.CoverDefinition;
import gregtech.api.cover.CoverableView;
import gregtech.api.unification.material.Materials;
import gregtech.client.renderer.texture.Textures;
import supersymmetry.common.blocks.BlockBreathingGas;
import supersymmetry.common.blocks.SuSyBlocks;

public class CoverAirDisperser extends CoverBase implements ITickable {

    private final int oxygenPerSecond;

    public CoverAirDisperser(@NotNull CoverDefinition definition, @NotNull CoverableView coverableView,
                             @NotNull EnumFacing attachedSide, int oxygenPerSecond) {
        super(definition, coverableView, attachedSide);
        this.oxygenPerSecond = oxygenPerSecond;
    }

    @Override
    public boolean canAttach(@NotNull CoverableView coverable, @NotNull EnumFacing side) {
        return coverable.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
    }

    @Override
    public void renderCover(@NotNull CCRenderState renderState, @NotNull Matrix4 translation,
                            @NotNull IVertexOperation[] pipeline, @NotNull Cuboid6 plateBox,
                            @NotNull BlockRenderLayer layer) {
        Textures.BLOWER_ACTIVE_OVERLAY.renderSided(getAttachedSide(), plateBox, renderState, pipeline, translation);
    }

    @Override
    public void update() {
        World world = getWorld();
        if (world.isRemote || getOffsetTimer() % 20 != 0) return;

        BlockPos frontPos = getPos().offset(getAttachedSide());
        Block block = world.getBlockState(frontPos).getBlock();
        if (!block.isReplaceable(world, frontPos) && block != SuSyBlocks.BREATHING_GAS) {
            if (block == SuSyBlocks.BREATHING_GAS) {
                world.scheduleUpdate(frontPos, SuSyBlocks.BREATHING_GAS, 10);
            }
            return;
        }

        TileEntity tileEntity = getTileEntityHere();
        if (tileEntity == null) return;

        IFluidHandler fluidHandler = tileEntity.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY,
                getAttachedSide());
        if (fluidHandler == null) return;

        BlockBreathingGas.GasType gasType = null;
        FluidStack oxygenStack = new FluidStack(Materials.Oxygen.getFluid(), oxygenPerSecond);
        FluidStack drained = fluidHandler.drain(oxygenStack, false);
        if (drained == null) {
            drained = fluidHandler.drain(oxygenPerSecond, false);
            if (drained != null && drained.getFluid().getName().equals("pesticide")) {
                gasType = BlockBreathingGas.GasType.PESTICIDE;
            }
        } else {
            gasType = BlockBreathingGas.GasType.OXYGEN;
        }
        if (gasType != null && drained.amount >= oxygenPerSecond) {
            fluidHandler.drain(oxygenStack, true);
            world.setBlockState(frontPos, SuSyBlocks.BREATHING_GAS.getState(gasType));
            world.scheduleUpdate(frontPos, SuSyBlocks.BREATHING_GAS, 10);
        }
    }
}
