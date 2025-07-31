package supersymmetry.client.renderer.textures;

import gregtech.api.capability.IDataAccessHatch;
import gregtech.api.capability.IObjectHolder;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import gregtech.common.ConfigHolder;
import gregtech.common.blocks.BlockComputerCasing;
import gregtech.common.blocks.BlockSteamCasing;
import gregtech.common.metatileentities.multi.multiblockpart.MetaTileEntityMultiblockPart;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.util.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import supersymmetry.client.renderer.textures.custom.VisualStateRenderer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.function.Function;

import static gregtech.api.util.GTUtility.gregtechId;
import static gregtech.common.blocks.BlockCleanroomCasing.CasingType.PLASCRETE;
import static gregtech.common.blocks.BlockFireboxCasing.FireboxCasingType.*;
import static gregtech.common.blocks.BlockMachineCasing.MachineCasingType.ULV;
import static gregtech.common.blocks.BlockMetalCasing.MetalCasingType.*;
import static gregtech.common.blocks.BlockMultiblockCasing.MultiblockCasingType.GRATE_CASING;
import static gregtech.common.blocks.BlockTurbineCasing.TurbineCasingType.*;
import static gregtech.common.blocks.MetaBlocks.*;
import static supersymmetry.client.renderer.textures.custom.VisualStateRenderer.from;

@ParametersAreNonnullByDefault
public class ConnectedTextures {

    public static final Map<ResourceLocation, Function<@Nullable IMultiblockPart, @Nullable ICubeRenderer>> replacements = new Object2ObjectArrayMap<>();

    public static final VisualStateRenderer PRIMITIVE_BRICKS_CTM = from(METAL_CASING.getState(PRIMITIVE_BRICKS), Textures.PRIMITIVE_BRICKS);
    public static final VisualStateRenderer HEAT_PROOF_CASING_CTM = from(METAL_CASING.getState(INVAR_HEATPROOF), Textures.HEAT_PROOF_CASING);
    public static final VisualStateRenderer FROST_PROOF_CASING_CTM = from(METAL_CASING.getState(ALUMINIUM_FROSTPROOF), Textures.FROST_PROOF_CASING);
    public static final VisualStateRenderer SOLID_STEEL_CASING_CTM = from(METAL_CASING.getState(STEEL_SOLID), Textures.SOLID_STEEL_CASING);
    public static final VisualStateRenderer VOLTAGE_CASING_ULV_CTM = from(MACHINE_CASING.getState(ULV), Textures.VOLTAGE_CASINGS[0]);
    public static final VisualStateRenderer CLEAN_STAINLESS_STEEL_CASING_CTM = from(METAL_CASING.getState(STAINLESS_CLEAN), Textures.CLEAN_STAINLESS_STEEL_CASING);
    public static final VisualStateRenderer STABLE_TITANIUM_CASING_CTM = from(METAL_CASING.getState(TITANIUM_STABLE), Textures.STABLE_TITANIUM_CASING);
    public static final VisualStateRenderer ROBUST_TUNGSTENSTEEL_CASING_CTM = from(METAL_CASING.getState(TUNGSTENSTEEL_ROBUST), Textures.ROBUST_TUNGSTENSTEEL_CASING);
    public static final VisualStateRenderer STEEL_TURBINE_CASING_CTM = from(TURBINE_CASING.getState(STEEL_TURBINE_CASING), SusyTextures.STEEL_TURBINE_CASING);
    public static final VisualStateRenderer STAINLESS_TURBINE_CASING_CTM = from(TURBINE_CASING.getState(STAINLESS_TURBINE_CASING), SusyTextures.STAINLESS_TURBINE_CASING);
    public static final VisualStateRenderer TITANIUM_TURBINE_CASING_CTM = from(TURBINE_CASING.getState(TITANIUM_TURBINE_CASING), SusyTextures.TITANIUM_TURBINE_CASING); // Unused for now
    public static final VisualStateRenderer TUNGSTENSTEEL_TURBINE_CASING_CTM = from(TURBINE_CASING.getState(TUNGSTENSTEEL_TURBINE_CASING), SusyTextures.TUNGSTENSTEEL_TURBINE_CASING);
    public static final VisualStateRenderer BRONZE_PLATED_BRICKS_CTM = from(METAL_CASING.getState(BRONZE_BRICKS), Textures.BRONZE_PLATED_BRICKS);
    public static final VisualStateRenderer BRONZE_FIREBOX_CTM = from(BOILER_FIREBOX_CASING.getState(BRONZE_FIREBOX), Textures.BRONZE_FIREBOX);
    public static final VisualStateRenderer BRONZE_FIREBOX_ACTIVE_CTM = from(BOILER_FIREBOX_CASING.getState(BRONZE_FIREBOX), Textures.BRONZE_FIREBOX_ACTIVE, true);
    public static final VisualStateRenderer STEEL_FIREBOX_CTM = from(BOILER_FIREBOX_CASING.getState(STEEL_FIREBOX), Textures.STEEL_FIREBOX);
    public static final VisualStateRenderer STEEL_FIREBOX_ACTIVE_CTM = from(BOILER_FIREBOX_CASING.getState(STEEL_FIREBOX), Textures.STEEL_FIREBOX_ACTIVE, true);
    public static final VisualStateRenderer TITANIUM_FIREBOX_CTM = from(BOILER_FIREBOX_CASING.getState(TITANIUM_FIREBOX), Textures.TITANIUM_FIREBOX);
    public static final VisualStateRenderer TITANIUM_FIREBOX_ACTIVE_CTM = from(BOILER_FIREBOX_CASING.getState(TITANIUM_FIREBOX), Textures.TITANIUM_FIREBOX_ACTIVE, true);
    public static final VisualStateRenderer TUNGSTENSTEEL_FIREBOX_CTM = from(BOILER_FIREBOX_CASING.getState(TUNGSTENSTEEL_FIREBOX), Textures.TUNGSTENSTEEL_FIREBOX);
    public static final VisualStateRenderer TUNGSTENSTEEL_FIREBOX_ACTIVE_CTM = from(BOILER_FIREBOX_CASING.getState(TUNGSTENSTEEL_FIREBOX), Textures.TUNGSTENSTEEL_FIREBOX_ACTIVE, true);
    public static final VisualStateRenderer COKE_BRICKS_CTM = from(METAL_CASING.getState(COKE_BRICKS), Textures.COKE_BRICKS);
    public static final VisualStateRenderer GRATE_CASING_STEEL_FRONT_CTM = from(MULTIBLOCK_CASING.getState(GRATE_CASING), Textures.GRATE_CASING_STEEL_FRONT);
    public static final VisualStateRenderer INERT_PTFE_CASING_CTM = from(METAL_CASING.getState(PTFE_INERT_CASING), Textures.INERT_PTFE_CASING);
    public static final VisualStateRenderer STURDY_HSSE_CASING_CTM = from(METAL_CASING.getState(HSSE_STURDY), Textures.STURDY_HSSE_CASING);
    public static final VisualStateRenderer PLASCRETE_CTM = from(CLEANROOM_CASING.getState(PLASCRETE), Textures.PLASCRETE);
    public static final VisualStateRenderer COMPUTER_CASING_CTM = from(COMPUTER_CASING.getState(BlockComputerCasing.CasingType.COMPUTER_CASING), Textures.COMPUTER_CASING);
    public static final VisualStateRenderer HIGH_POWER_CASING_CTM = from(COMPUTER_CASING.getState(BlockComputerCasing.CasingType.HIGH_POWER_CASING), Textures.HIGH_POWER_CASING);
    public static final VisualStateRenderer ADVANCED_COMPUTER_CASING_CTM = from(COMPUTER_CASING.getState(BlockComputerCasing.CasingType.ADVANCED_COMPUTER_CASING), Textures.ADVANCED_COMPUTER_CASING);
    public static final VisualStateRenderer PALLADIUM_SUBSTATION_CASING_CTM = from(METAL_CASING.getState(PALLADIUM_SUBSTATION), Textures.PALLADIUM_SUBSTATION_CASING);
    public static final VisualStateRenderer WOOD_WALL_CTM = from(STEAM_CASING.getState(BlockSteamCasing.SteamCasingType.WOOD_WALL), Textures.WOOD_WALL);
    public static final VisualStateRenderer PRIMITIVE_PUMP_CTM = from(STEAM_CASING.getState(BlockSteamCasing.SteamCasingType.PUMP_DECK), Textures.PRIMITIVE_PUMP);

    @Nullable
    public static ICubeRenderer get(ResourceLocation id, @Nullable IMultiblockPart part) {
        var function = replacements.get(id);
        if (function == null) return null;
        return function.apply(part);
    }

    public static void register(ResourceLocation id, Function<@Nullable IMultiblockPart, @Nullable ICubeRenderer> function) {
        replacements.put(id, function);
    }

    private static ICubeRenderer fromBoilerPart(@Nullable IMultiblockPart part,
                                                ICubeRenderer casing,
                                                ICubeRenderer firebox,
                                                ICubeRenderer fireboxActive) {

        if (part instanceof IMultiblockAbilityPart<?> ability && ability.getAbility() == MultiblockAbility.EXPORT_FLUIDS) {
            return casing;
        } else if (part instanceof MetaTileEntityMultiblockPart actualPart) {
            return actualPart.getController().isActive() ? fireboxActive : firebox;
        }
        return casing;
    }

    public static void init() {

        // GTCEu
        PRIMITIVE_BRICKS_CTM.replace(gregtechId("primitive_blast_furnace.bronze"));
        HEAT_PROOF_CASING_CTM.replace(gregtechId("electric_blast_furnace"),
                gregtechId("multi_furnace"));
        FROST_PROOF_CASING_CTM.replace(gregtechId("vacuum_freezer"));
        SOLID_STEEL_CASING_CTM.replace(gregtechId("implosion_compressor"),
                gregtechId("large_miner.ev"),
                gregtechId("central_monitor"),
                gregtechId("fluid_drilling_rig.mv"),
                gregtechId("tank.steel"));
        VOLTAGE_CASING_ULV_CTM.replace(gregtechId("pyrolyse_oven")); // Who would give connected textures for this :clueless:
        CLEAN_STAINLESS_STEEL_CASING_CTM.replace(gregtechId("distillation_tower"),
                gregtechId("cracker"));
        STABLE_TITANIUM_CASING_CTM.replace(gregtechId("large_combustion_engine"),
                gregtechId("large_miner.iv"),
                gregtechId("fluid_drilling_rig.hv"));
        ROBUST_TUNGSTENSTEEL_CASING_CTM.replace(gregtechId("extreme_combustion_engine"),
                gregtechId("large_miner.luv"),
                gregtechId("processing_array"),
                gregtechId("fluid_drilling_rig.ev"));
        STEEL_TURBINE_CASING_CTM.replace(gregtechId("large_turbine.steam"));
        STAINLESS_TURBINE_CASING_CTM.replace(gregtechId("large_turbine.gas"));
        TUNGSTENSTEEL_TURBINE_CASING_CTM.replace(gregtechId("large_turbine.plasma"));

        register(gregtechId("large_boiler.bronze"), part -> fromBoilerPart(
                part, BRONZE_PLATED_BRICKS_CTM, BRONZE_FIREBOX_CTM, BRONZE_FIREBOX_ACTIVE_CTM));

        register(gregtechId("large_boiler.steel"), part -> fromBoilerPart(
                part, SOLID_STEEL_CASING_CTM, STEEL_FIREBOX_CTM, STEEL_FIREBOX_ACTIVE_CTM));

        register(gregtechId("large_boiler.titanium"), part -> fromBoilerPart(
                part, STABLE_TITANIUM_CASING_CTM, TITANIUM_FIREBOX_CTM, TITANIUM_FIREBOX_ACTIVE_CTM));

        register(gregtechId("large_boiler.tungstensteel"), part -> fromBoilerPart(
                part, ROBUST_TUNGSTENSTEEL_CASING_CTM, TUNGSTENSTEEL_FIREBOX_CTM, TUNGSTENSTEEL_FIREBOX_ACTIVE_CTM));

        COKE_BRICKS_CTM.replace(gregtechId("coke_oven"));

        register(gregtechId("assembly_line"), part ->
                part == null || part instanceof IDataAccessHatch ? GRATE_CASING_STEEL_FRONT_CTM : SOLID_STEEL_CASING_CTM);

        // Skipping Fusion Reactors, it doesn't look like they should have connected textures

        INERT_PTFE_CASING_CTM.replace(gregtechId("large_chemical_reactor"));

        final boolean useSteel = ConfigHolder.machines.steelSteamMultiblocks;

        register(gregtechId("steam_oven"), part -> {
            if (part instanceof IMultiblockAbilityPart<?> abilityPart && abilityPart.getAbility() == MultiblockAbility.STEAM) {
                if (part instanceof MetaTileEntityMultiblockPart actualPart) {
                    boolean active = actualPart.getController().isActive();
                    if (useSteel) {
                        return active ? STEEL_FIREBOX_ACTIVE_CTM : STEEL_FIREBOX_CTM;
                    } else {
                        return active ? BRONZE_FIREBOX_ACTIVE_CTM : BRONZE_FIREBOX_CTM;
                    }
                }
            }
            return useSteel ? SOLID_STEEL_CASING_CTM : BRONZE_PLATED_BRICKS_CTM;
        });

        register(gregtechId("steam_grinder"), part -> useSteel ? SOLID_STEEL_CASING_CTM : BRONZE_FIREBOX_CTM);

        STURDY_HSSE_CASING_CTM.replace(gregtechId("advanced_processing_array"));
        PLASCRETE_CTM.replace(gregtechId("cleanroom"));
        BRONZE_PLATED_BRICKS_CTM.replace(gregtechId("charcoal_pile"));  // Why would one use this...

        register(gregtechId("data_bank"), part ->
                part instanceof IDataAccessHatch ? COMPUTER_CASING_CTM : HIGH_POWER_CASING_CTM);

        register(gregtechId("research_station"), part ->
                part == null || part instanceof IObjectHolder ? ADVANCED_COMPUTER_CASING_CTM : COMPUTER_CASING_CTM);

        register(gregtechId("high_performance_computing_array"), part ->
                part == null ? ADVANCED_COMPUTER_CASING_CTM : COMPUTER_CASING_CTM);

        COMPUTER_CASING_CTM.replace(gregtechId("network_switch"));
        PALLADIUM_SUBSTATION_CASING_CTM.replace(gregtechId("power_substation"));
        HIGH_POWER_CASING_CTM.replace(gregtechId("active_transformer"));
        WOOD_WALL_CTM.replace(gregtechId("tank.wood"));
        PRIMITIVE_PUMP_CTM.replace(gregtechId("primitive_water_pump"));
    }
}
