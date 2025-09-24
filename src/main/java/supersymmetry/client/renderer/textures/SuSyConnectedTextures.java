package supersymmetry.client.renderer.textures;

import static dev.tianmi.sussypatches.client.renderer.textures.ConnectedTextures.*;
import static dev.tianmi.sussypatches.client.renderer.textures.GCyMConnectedTextures.STRESS_PROOF_CTM;
import static dev.tianmi.sussypatches.client.renderer.textures.cube.VisualStateRenderer.from;
import static supersymmetry.api.util.SuSyUtility.susyId;
import static supersymmetry.common.blocks.BlockCoagulationTankWall.CoagulationTankWallType.WOODEN_COAGULATION_TANK_WALL;
import static supersymmetry.common.blocks.BlockSuSyMultiblockCasing.CasingType.*;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import dev.tianmi.sussypatches.client.renderer.textures.cube.VisualStateRenderer;
import gregtech.api.metatileentity.multiblock.IMultiblockAbilityPart;
import gregtech.api.metatileentity.multiblock.IMultiblockPart;
import gregtech.api.metatileentity.multiblock.MultiblockAbility;
import gregtech.client.renderer.ICubeRenderer;
import gregtech.client.renderer.texture.Textures;
import supersymmetry.api.metatileentity.multiblock.SuSyMultiblockAbilities;
import supersymmetry.common.blocks.SuSyBlocks;

public class SuSyConnectedTextures {

    // SuSy
    public static final VisualStateRenderer WOODEN_COAGULATION_TANK_WALL_CTM;
    public static final VisualStateRenderer SILICON_CARBIDE_CASING_CTM;
    public static final VisualStateRenderer MONEL_500_CASING_CTM;
    public static final VisualStateRenderer CONDUCTIVE_COPPER_PIPE_CTM;
    public static final VisualStateRenderer ULV_STRUCTURAL_CASING_CTM;

    static {
        WOODEN_COAGULATION_TANK_WALL_CTM = from(
                SuSyBlocks.COAGULATION_TANK_WALL.getState(WOODEN_COAGULATION_TANK_WALL));
        SILICON_CARBIDE_CASING_CTM = from(SuSyBlocks.MULTIBLOCK_CASING.getState(SILICON_CARBIDE_CASING));
        MONEL_500_CASING_CTM = from(SuSyBlocks.MULTIBLOCK_CASING.getState(MONEL_500_CASING));
        CONDUCTIVE_COPPER_PIPE_CTM = from(SuSyBlocks.MULTIBLOCK_CASING.getState(COPPER_PIPE));
        ULV_STRUCTURAL_CASING_CTM = from(SuSyBlocks.MULTIBLOCK_CASING.getState(ULV_STRUCTURAL_CASING));
    }

    public static void init() {
        SOLID_STEEL_CASING_CTM.override(susyId("cluster_mill"),
                susyId("flying_shear"),
                susyId("rolling_mill"),
                susyId("slab_mold"),
                susyId("billet_mold"),
                susyId("turning_zone"),
                susyId("arc_furnace_complex"),
                susyId("clarifier"),
                susyId("coking_tower"),
                susyId("drone_pad"),
                susyId("dumper"),
                susyId("electrolytic_cell"),
                susyId("evaporation_pool"),
                susyId("flare_stack"),
                susyId("gas_atomizer"),
                susyId("gravity_separator"),
                susyId("heat_exchanger"),
                susyId("heat_radiator"),
                susyId("injection_molder"),
                susyId("large_fluid_pump"),
                susyId("large_weapons_factory"),
                susyId("metallurgical_converter"),
                susyId("mining_drill"),
                susyId("natural_draft_cooling_tower"),
                susyId("ore_sorter"),
                susyId("polymerization_tank"),
                susyId("quarry"),
                susyId("railroad_engineering_station"),
                susyId("rocket_assembler"),
                susyId("rocket_programmer"),
                susyId("rotary_kiln"),
                susyId("smoke_stack"),
                susyId("vacuum_distillation_tower"),
                susyId("landing_pad"),
                susyId("advanced_arc_furnace"));
        FROST_PROOF_CASING_CTM.override(susyId("condenser"),
                susyId("high_pressure_cryogenic_distillation_plant"),
                susyId("low_pressure_cryogenic_distillation_plant"),
                susyId("single_column_cryogenic_distillation_plant"),
                susyId("magnetic_refrigerator"),
                susyId("pressure_swing_adsorber"));
        CLEAN_STAINLESS_STEEL_CASING_CTM.override(susyId("catalytic_reformer"),
                susyId("curtain_coater"),
                susyId("froth_flotation_tank"),
                susyId("mixer_settler"),
                susyId("quencher"),
                susyId("sieve_distillation_tower"));
        STEEL_TURBINE_CASING_CTM.override(susyId("basic_steam_turbine"),
                susyId("basic_gas_turbine"));
        TITANIUM_TURBINE_CASING_CTM.override(susyId("low_pressure_advanced_steam_turbine"),
                susyId("high_pressure_advanced_steam_turbine"));
        INERT_PTFE_CASING_CTM.override(susyId("fluidized_bed_reactor"),
                susyId("blender"));
        SILICON_CARBIDE_CASING_CTM.override(susyId("high_temperature_distillation_tower"),
                susyId("hot_isostatic_press"));
        WOODEN_COAGULATION_TANK_WALL_CTM.override(susyId("coagulation_tank"));
        MONEL_500_CASING_CTM.override(susyId("strand_cooler"));
        ULV_STRUCTURAL_CASING_CTM.override(susyId("sintering_oven"));
        ROBUST_TUNGSTENSTEEL_CASING_CTM.override(susyId("magnetohydrodynamic_generator"));
        STRESS_PROOF_CTM.override(susyId("arc_furnace_complex"));
        VOLTAGE_CASING_ULV_CTM.override(susyId("fermentation_vat"));
        HEAT_PROOF_CASING_CTM.override(susyId("reaction_furnace"));
        PRIMITIVE_BRICKS_CTM.override(susyId("reverberatory_furnace"));
        STABLE_TITANIUM_CASING_CTM.override(susyId("scrap_recycler"));
        BRONZE_PLATED_BRICKS_CTM.override(susyId("primitive_mud_pump"));

        registerCustomOverride(susyId("slab_mold"), SuSyConnectedTextures::strandMoldHandler);
        registerCustomOverride(susyId("billet_mold"), SuSyConnectedTextures::strandMoldHandler);
        registerCustomOverride(susyId("milling"), SuSyConnectedTextures::millingHandler);
        registerCustomOverride(susyId("multi_stage_flash_distiller"), SuSyConnectedTextures::msfdHandler);
    }

    @NotNull
    private static ICubeRenderer msfdHandler(@Nullable IMultiblockPart part) {
        if (part instanceof IMultiblockAbilityPart<?>abilityPart) {
            var ability = abilityPart.getAbility();
            if (ability == MultiblockAbility.MAINTENANCE_HATCH || ability == MultiblockAbility.INPUT_ENERGY) {
                return Textures.CLEAN_STAINLESS_STEEL_CASING;
            }
        }
        return Textures.SOLID_STEEL_CASING;
    }

    @NotNull
    private static ICubeRenderer millingHandler(@Nullable IMultiblockPart part) {
        if (part instanceof IMultiblockAbilityPart<?>abilityPart) {
            var ability = abilityPart.getAbility();
            if (ability == MultiblockAbility.MAINTENANCE_HATCH || ability == MultiblockAbility.INPUT_ENERGY) {
                return SOLID_STEEL_CASING_CTM;
            }
        }
        return CLEAN_STAINLESS_STEEL_CASING_CTM;
    }

    @NotNull
    private static ICubeRenderer strandMoldHandler(@Nullable IMultiblockPart part) {
        if (part instanceof IMultiblockAbilityPart<?>abilityPart) {
            MultiblockAbility<?> ability = abilityPart.getAbility();
            if (ability == MultiblockAbility.IMPORT_FLUIDS || ability == MultiblockAbility.EXPORT_FLUIDS ||
                    ability == SuSyMultiblockAbilities.STRAND_EXPORT) {
                return CONDUCTIVE_COPPER_PIPE_CTM;
            }
        }
        return SOLID_STEEL_CASING_CTM;
    }
}
