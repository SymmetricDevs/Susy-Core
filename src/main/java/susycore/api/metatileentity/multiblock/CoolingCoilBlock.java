package susycore.api.metatileentity.multiblock;

import gregtech.api.GregTechAPI;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.pattern.PatternStringError;
import gregtech.api.pattern.TraceabilityPredicate;
import gregtech.api.recipes.recipeproperties.TemperatureProperty;
import gregtech.api.worldgen.bedrockFluids.BedrockFluidVeinHandler;
import gregtech.common.blocks.MetaBlocks;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import susycore.common.blocks.BlockCoolingCoil;

import java.util.LinkedList;
import java.util.Map;
import java.util.function.Supplier;

import static gregtech.GregTechMod.proxy;

public class CoolingCoilBlock {

    /* Init cooling coils exist */

    public static final Object2ObjectOpenHashMap<IBlockState, IHeatingCoilBlockStats> COOLING_COILS = new Object2ObjectOpenHashMap<>();

    BlockCoolingCoil.CoolingCoilType type : BlockCoolingCoil.CoolingCoilType.values()) {
        COOLING_COILS.put(MetaBlocks.COOLING_COIL.getState(type), type);
    }

    @Mod.EventHandler
    public void onPostInit(FMLPostInitializationEvent event) {
        proxy.onPostLoad();
        BedrockFluidVeinHandler.recalculateChances(true);
        // registers coil types for the BlastTemperatureProperty used in Blast Furnace Recipes
        // runs AFTER craftTweaker
        for (Map.Entry<IBlockState, IHeatingCoilBlockStats> entry : GregTechAPI.COOLING_COILS.entrySet()) {
            IHeatingCoilBlockStats value = entry.getValue();
            if (value != null) {
                String name = entry.getKey().getBlock().getTranslationKey();
                if (!name.endsWith(".name")) name = String.format("%s.name", name);
                TemperatureProperty.registerCoilType(value.getCoilTemperature(), value.getMaterial(), name);
            }
        }
    }

    public static Supplier<TraceabilityPredicate> COOLING_COILS = () -> new TraceabilityPredicate(blockWorldState -> {
        IBlockState blockState = blockWorldState.getBlockState();
        if (susycore.COOLING_COILS.containsKey(blockState)) {
            IHeatingCoilBlockStats stats = GregTechAPI.COOLING_COILS.get(blockState);
            Object currentCoil = blockWorldState.getMatchContext().getOrPut("CoolingCoilType", stats);
            if (!currentCoil.equals(stats)) {
                blockWorldState.setError(new PatternStringError("gregtech.multiblock.pattern.error.coils"));
                return false;
            }
            blockWorldState.getMatchContext().getOrPut("VABlock", new LinkedList<>()).add(blockWorldState.getPos());
            return true;
        }
        return false;
    });

    public static TraceabilityPredicate coolingCoils() {
        return TraceabilityPredicate.COOLING_COILS.get();
    }

    /* Init cooling coils exist */

}
