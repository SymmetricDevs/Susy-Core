package supersymmetry.api.util;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.metatileentity.multiblock.MultiblockControllerBase;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.util.BlockInfo;
import gregtech.api.util.RelativeDirection;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;

import gregtech.api.GTValues;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;

import supersymmetry.Supersymmetry;
import supersymmetry.common.materials.SusyMaterials;

import java.util.HashMap;
import java.util.function.Function;
import java.util.Map;
import java.util.function.Supplier;

public class SuSyUtility {

    /*
     * if we assume a solar boiler to have an approximately 1m^2 "collecting" surface, and take the hp to be less
     * inefficient,
     * one can approximate the J to EU conversion via 18L/t -> 9EU/t -> 180EU/s -> 1000J/180EU ~= 6J in one EU.
     * Assuming solar actually gets slightly less sunlight than 1m^2 conversion may be ~= 5, but potential
     * inefficiencies
     * make it unclear the specific amount. Just going to round to one sig fig and leave it at 10J -> 1EU
     */
    public static final int JOULES_PER_EU = 10;

    public static final Function<Integer, Integer> reactorTankSizeFunction = tier -> {
        if (tier <= GTValues.LV)
            return 12000;
        if (tier == GTValues.MV)
            return 16000;
        if (tier == GTValues.HV)
            return 20000;
        if (tier == GTValues.EV)
            return 36000;
        // IV+
        return 64000;
    };

    public static final Function<Integer, Integer> collectorTankSizeFunction = tier -> {
        if (tier <= GTValues.LV)
            return 16000;
        if (tier == GTValues.MV)
            return 24000;
        if (tier == GTValues.HV)
            return 32000;
        // EV+
        return 64000;
    };

    public static final Function<Integer, Integer> bulkTankSizeFunction = tier -> {
        if (tier <= GTValues.LV)
            return 12000;
        if (tier == GTValues.MV)
            return 18000;
        if (tier == GTValues.HV)
            return 24000;
        if (tier == GTValues.EV)
            return 48000;
        // IV+
        return 64000;
    };

    public static class Lubricant {
        public String name;
        public int amount_required;
        public double boost;

        public Lubricant(String name, int amount_required, double boost) {
            this.name = name;
            this.amount_required = amount_required;
            this.boost = boost;
        }
    }

    public static final Map<String, Lubricant> lubricants;
    static {
        lubricants = new HashMap<>();
        lubricants.put("lubricating_oil", new Lubricant("LubricatingOil", 16, 1.0));
        lubricants.put("lubricant", new Lubricant("Lubricant", 8, 1.0));
        lubricants.put("midgrade_lubricant", new Lubricant("MidgradeLubricant", 4, 1.5));
        lubricants.put("premium_lubricant", new Lubricant("PremiumLubricant", 2, 1.5));
        lubricants.put("supreme_lubricant", new Lubricant("SupremeLubricant", 1, 2.0));
    }

    public static class Coolant {
        public String name;
        public int amount_required;

        public Coolant(String name, int amount_required) {
            this.name = name;
            this.amount_required = amount_required;
        }
    }

    public static final Map<String, Coolant> coolants;
    static {
        coolants = new HashMap<>();
        coolants.put("water", new Coolant("Water", 16));
        coolants.put("distilled_water", new Coolant("DistilledWater", 8));
        coolants.put("coolant", new Coolant("Coolant", 4));
        coolants.put("advanced_coolant", new Coolant("AdvancedCoolant", 1));
    }

    public static ResourceLocation susyId(String path) {
        return new ResourceLocation(Supersymmetry.MODID, path);
    }

    public static String getRLPrefix(Material material) {
        return material.getModid().equals(GTValues.MODID) ? "" : material.getModid() + ":";
    }
    protected static EnumFacing getRelativeFacing(MultiblockControllerBase mte, RelativeDirection dir) {
        return dir.getRelativeFacing(mte.getFrontFacing(), mte.getUpwardsFacing(), mte.isFlipped());
    }
    public static TraceabilityPredicate orientation(MultiblockControllerBase mte, IBlockState state, RelativeDirection direction,
                                                IProperty<EnumFacing> facingProperty) {
        EnumFacing facing = getRelativeFacing(mte, direction);

        Supplier<BlockInfo[]> supplier = () -> new BlockInfo[] {
                new BlockInfo(state.withProperty(facingProperty, facing)) };
        return new TraceabilityPredicate(blockWorldState -> {
            if (blockWorldState.getBlockState() != state.withProperty(facingProperty, facing)) {
                if (blockWorldState.getBlockState().getBlock() != state.getBlock()) return false;
                mte.getWorld().setBlockState(blockWorldState.getPos(), state.withProperty(facingProperty, facing));
            }
            return true;
        }, supplier);
    }

    public static TraceabilityPredicate axisOrientation(MultiblockControllerBase mte, IBlockState state, RelativeDirection direction,
                                                    IProperty<EnumFacing.Axis> facingProperty) {
        EnumFacing facing = getRelativeFacing(mte, direction);
        EnumFacing.Axis axis = facing.getAxis();

        Supplier<BlockInfo[]> supplier = () -> new BlockInfo[] {
                new BlockInfo(state.withProperty(facingProperty, axis)) };
        return new TraceabilityPredicate(blockWorldState -> {
            if (blockWorldState.getBlockState() != state.withProperty(facingProperty, axis)) {
                if (blockWorldState.getBlockState().getBlock() != state.getBlock()) return false;
                mte.getWorld().setBlockState(blockWorldState.getPos(), state.withProperty(facingProperty, axis));
            }
            return true;
        }, supplier);
    }
    public static TraceabilityPredicate horizontalOrientation(MultiblockControllerBase mte, IBlockState state, RelativeDirection direction,
                                                    IProperty<EnumFacing> facingProperty) {
        EnumFacing facing = getRelativeFacing(mte, direction);
        // converting the left facing to positive x or z axis direction
        // this is needed for the following update which converts this rotatable block from horizontal directional into
        // axial directional.
        EnumFacing axialFacing = facing.getIndex() < 4 ? EnumFacing.SOUTH : EnumFacing.WEST;

        Supplier<BlockInfo[]> supplier = () -> new BlockInfo[] {
                new BlockInfo(state.withProperty(facingProperty, axialFacing)) };
        return new TraceabilityPredicate(blockWorldState -> {
            if (blockWorldState.getBlockState() != state.withProperty(facingProperty, axialFacing)) {
                if (blockWorldState.getBlockState().getBlock() != state.getBlock()) return false;
                mte.getWorld().setBlockState(blockWorldState.getPos(), state.withProperty(facingProperty, axialFacing));
            }
            return true;
        }, supplier);
    }
}
