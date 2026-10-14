package supersymmetry.api.capability.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.init.Bootstrap;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import gregtech.api.capability.impl.FluidTankList;

class InductionFurnaceRecipeLogicTest {

    private static Fluid metal;
    private static Fluid coolant;

    @BeforeAll
    static void bootstrap() {
        Bootstrap.register();
        metal = new Fluid("furnace_test_metal", new ResourceLocation("susy:test"), new ResourceLocation("susy:test"));
        coolant = new Fluid("furnace_test_coolant", new ResourceLocation("susy:test"),
                new ResourceLocation("susy:test"));
        FluidRegistry.registerFluid(metal);
        FluidRegistry.registerFluid(coolant);
    }

    @Test
    void partialActualFillRetainsExactTaggedRemainderAndRetryDoesNotDuplicate() {
        FluidTank destination = new FluidTank(16000) {

            @Override
            public int fill(FluidStack stack, boolean execute) {
                FluidStack limited = stack.copy();
                if (execute) limited.amount = Math.min(500, limited.amount);
                return super.fill(limited, execute);
            }
        };
        FluidStack product = new FluidStack(metal, 1152);
        product.tag = new NBTTagCompound();
        product.tag.setString("Batch", "one");
        List<FluidStack> pending = new ArrayList<>(Collections.singletonList(product));
        assertEquals(1152, destination.fill(product.copy(), false));
        assertTrue(InductionFurnaceRecipeLogic.deliverFluidOutputs(pending, destination, false));
        assertEquals(652, pending.get(0).amount);
        assertEquals("one", pending.get(0).tag.getString("Batch"));
        InductionFurnaceRecipeLogic.deliverFluidOutputs(pending, destination, false);
        assertEquals(152, pending.get(0).amount);
        InductionFurnaceRecipeLogic.deliverFluidOutputs(pending, destination, false);
        assertTrue(pending.isEmpty());
        assertEquals(1152, destination.getFluidAmount());
        assertFalse(InductionFurnaceRecipeLogic.deliverFluidOutputs(pending, destination, false));
        assertEquals(1152, destination.getFluidAmount());
    }

    @Test
    void blockedFillRequiresExplicitPermissionToVoid() {
        FluidTank destination = new FluidTank(new FluidStack(coolant, 16000), 16000);
        List<FluidStack> pending = new ArrayList<>(Collections.singletonList(new FluidStack(metal, 1152)));
        assertFalse(InductionFurnaceRecipeLogic.deliverFluidOutputs(pending, destination, false));
        assertEquals(1152, pending.get(0).amount);
        assertTrue(InductionFurnaceRecipeLogic.deliverFluidOutputs(pending, destination, true));
        assertTrue(pending.isEmpty());
        assertEquals(16000, destination.getFluidAmount());
    }

    @Test
    void coolantCannotOccupyTheOnlyProductTankButNormalCoolingStillWorks() {
        FluidTank first = new FluidTank(new FluidStack(coolant, 15990), 16000);
        FluidTank second = new FluidTank(16000);
        FluidTankList exports = new FluidTankList(true, first, second);
        List<FluidStack> outputs = Collections.singletonList(new FluidStack(metal, 1152));
        assertFalse(
                InductionFurnaceRecipeLogic.canOutputCoolant(exports, new FluidStack(coolant, 100), outputs, false));
        assertEquals(15990, first.getFluidAmount());
        assertEquals(0, second.getFluidAmount());
        assertTrue(InductionFurnaceRecipeLogic.canOutputCoolant(exports, new FluidStack(coolant, 100), outputs, true));
        first.drain(15000, true);
        assertTrue(InductionFurnaceRecipeLogic.canOutputCoolant(exports, new FluidStack(coolant, 100), outputs, false));
    }
}
