package supersymmetry.client.renderer.textures;

import gregtech.client.renderer.texture.Textures;
import gregtech.client.renderer.texture.cube.*;
import gregtech.client.renderer.texture.custom.DrumRenderer;
import supersymmetry.client.renderer.textures.custom.ExtenderRender;

public class SusyTextures {

    public SusyTextures() {}

    public static final SimpleSidedCubeRenderer WOODEN_COAGULATION_TANK_WALL = new SimpleSidedCubeRenderer(
            "casings/wooden_coagulation_tank_wall");
    public static final SimpleSidedCubeRenderer PLASTIC_CAN_OVERLAY = new SimpleSidedCubeRenderer(
            "storage/drums/plastic_can_top");
    public static final SimpleSidedCubeRenderer INV_BRIDGE = new SimpleSidedCubeRenderer("logistics/bridges/inv");
    public static final SimpleSidedCubeRenderer TANK_BRIDGE = new SimpleSidedCubeRenderer("logistics/bridges/tank");
    public static final SimpleSidedCubeRenderer INV_TANK_BRIDGE = new SimpleSidedCubeRenderer(
            "logistics/bridges/inv_tank");
    public static final SimpleSidedCubeRenderer UNIVERSAL_BRIDGE = new SimpleSidedCubeRenderer(
            "logistics/bridges/universal");

    public static final ExtenderRender INV_EXTENDER = new ExtenderRender("logistics/extenders/inv");
    public static final ExtenderRender TANK_EXTENDER = new ExtenderRender("logistics/extenders/tank");
    public static final ExtenderRender INV_TANK_EXTENDER = new ExtenderRender("logistics/extenders/inv_tank");
    public static final ExtenderRender UNIVERSAL_EXTENDER = new ExtenderRender("logistics/extenders/universal");

    public static final OrientedOverlayRenderer VULCANIZING_PRESS_OVERLAY = new OrientedOverlayRenderer(
            "machines/vulcanizing_press");
    public static final OrientedOverlayRenderer LATEX_COLLECTOR_OVERLAY = new OrientedOverlayRenderer(
            "machines/latex_collector");
    public static final OrientedOverlayRenderer ROASTER_OVERLAY = new OrientedOverlayRenderer("machines/roaster");
    public static final OrientedOverlayRenderer MIXER_OVERLAY_STEAM = new OrientedOverlayRenderer(
            "machines/mixer_steam");
    public static final OrientedOverlayRenderer CONTINUOUS_STIRRED_TANK_REACTOR_OVERLAY = new OrientedOverlayRenderer(
            "machines/continuous_stirred_tank_reactor");
    public static final OrientedOverlayRenderer FIXED_BED_REACTOR_OVERLAY = new OrientedOverlayRenderer(
            "machines/fixed_bed_reactor");
    public static final OrientedOverlayRenderer TRICKLE_BED_REACTOR_OVERLAY = new OrientedOverlayRenderer(
            "machines/trickle_bed_reactor");
    public static final OrientedOverlayRenderer BUBBLE_COLUMN_REACTOR_OVERLAY = new OrientedOverlayRenderer(
            "machines/bubble_column_reactor");
    public static final OrientedOverlayRenderer BATCH_REACTOR_OVERLAY = new OrientedOverlayRenderer(
            "machines/batch_reactor");

    public static final OrientedOverlayRenderer CRYSTALLIZER_OVERLAY = new OrientedOverlayRenderer(
            "machines/crystallizer");
    public static final OrientedOverlayRenderer DRYER_OVERLAY = new OrientedOverlayRenderer("machines/dryer");
    public static final OrientedOverlayRenderer ION_EXCHANGE_COLUMN_OVERLAY = new OrientedOverlayRenderer(
            "machines/ion_exchange_column");
    public static final OrientedOverlayRenderer ZONE_REFINER_OVERLAY = new OrientedOverlayRenderer(
            "machines/zone_refiner");
    public static final OrientedOverlayRenderer TUBE_FURNACE_OVERLAY = new OrientedOverlayRenderer(
            "machines/tube_furnace");

    public static final OrientedOverlayRenderer UV_LIGHT_BOX_OVERLAY = new OrientedOverlayRenderer(
            "machines/uv_light_box");
    public static final OrientedOverlayRenderer CVD_OVERLAY = new OrientedOverlayRenderer("machines/cvd");
    public static final OrientedOverlayRenderer ION_IMPLANTER_OVERLAY = new OrientedOverlayRenderer(
            "machines/ion_implanter");
    public static final OrientedOverlayRenderer SPIN_COATER_OVERLAY = new OrientedOverlayRenderer(
            "machines/spin_coater");
    public static final OrientedOverlayRenderer SPUTTER_DEPOSITION_OVERLAY = new OrientedOverlayRenderer(
            "machines/sputter_deposition");
    public static final OrientedOverlayRenderer PHASE_SEPARATOR_OVERLAY = new OrientedOverlayRenderer(
            "machines/phase_separator");
    public static final OrientedOverlayRenderer BATH_CONDENSER_OVERLAY = new OrientedOverlayRenderer(
            "machines/bath_condenser");
    public static final OrientedOverlayRenderer CATALYTIC_REFORMER_OVERLAY = new OrientedOverlayRenderer(
            "multiblock/catalytic_reformer");
    public static final OrientedOverlayRenderer INCINERATOR_OVERLAY = new OrientedOverlayRenderer(
            "machines/incinerator");

    public static final OrientedOverlayRenderer FUEL_CELL_OVERLAY = new OrientedOverlayRenderer("machines/fuel_cell");

    public static final OrientedOverlayRenderer FLUID_COMPRESSOR_OVERLAY = new OrientedOverlayRenderer(
            "machines/fluid_compressor");
    public static final OrientedOverlayRenderer FLUID_DECOMPRESSOR_OVERLAY = new OrientedOverlayRenderer(
            "machines/fluid_decompressor");
    public static final OrientedOverlayRenderer ELECTROSTATIC_SEPARATOR_OVERLAY = new OrientedOverlayRenderer(
            "machines/electrostatic_separator");
    public static final OrientedOverlayRenderer TEXTILE_SPINNER_OVERLAY = new OrientedOverlayRenderer(
            "machines/textile_spinner");
    public static final OrientedOverlayRenderer POLISHING_MACHINE_OVERLAY = new OrientedOverlayRenderer(
            "machines/polishing_machine");
    public static final OrientedOverlayRenderer ARC_FURNACE_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/arc_furnace");
    public static final OrientedOverlayRenderer CLARIFIER_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/clarifier");
    public static final OrientedOverlayRenderer CONDENSER_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/condenser");
    public static final OrientedOverlayRenderer NATURAL_DRAFT_COOLING_TOWER_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/natural_draft_cooling_tower");
    public static final OrientedOverlayRenderer HPCDT_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/distillation_towers/high_pressure_cryogenic_distilation_tower");
    public static final OrientedOverlayRenderer HTDT_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/distillation_towers/high_temperature_distilation_tower");
    public static final OrientedOverlayRenderer LPCDT_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/distillation_towers/low_pressure_cryogenic_distilation_tower");
    public static final OrientedOverlayRenderer VDT_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/distillation_towers/vacuum_temperature_distilation_tower");
    public static final OrientedOverlayRenderer DUMPER_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/dumper");
    public static final OrientedOverlayRenderer ELECTROLYTIC_CELL_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/electrolytic_cell");
    public static final OrientedOverlayRenderer FLARE_STACK_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/flare_stack");
    public static final OrientedOverlayRenderer FLUIDIZED_BED_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/fluidized_bed");
    public static final OrientedOverlayRenderer FROTH_FLOTATION_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/froth_flotation");
    public static final OrientedOverlayRenderer HEAT_EXCHANGER_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/heat_exchanger");
    public static final OrientedOverlayRenderer LARGE_GAS_TURBINE_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/large_gas_turbine");
    public static final OrientedOverlayRenderer LARGE_STEAM_TURBINE_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/large_steam_turbine");
    public static final OrientedOverlayRenderer ADVANCED_STEAM_TURBINE_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/advanced_steam_turbine");

    public static final OrientedOverlayRenderer LARGE_WEAPONS_FACTORY_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/large_weapons_factory");
    public static final OrientedOverlayRenderer MINING_DRILL_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/mining_drill");
    public static final OrientedOverlayRenderer LARGE_FLUID_PUMP_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/large_fluid_pump");
    public static final OrientedOverlayRenderer ORE_SORTER_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/ore_sorter");
    public static final OrientedOverlayRenderer PRESSURE_SWING_ABSORBER_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/pressure_swing_absorber");
    public static final OrientedOverlayRenderer QUENCHER_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/quencher");
    public static final OrientedOverlayRenderer RADIATOR_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/radiator");
    public static final OrientedOverlayRenderer RAILROAD_ENGINEERING_STATION_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/railroad_engineering_station");
    public static final OrientedOverlayRenderer ROTARY_KILN_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/rotary_kiln");
    public static final OrientedOverlayRenderer SINTERING_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/sintering");
    public static final OrientedOverlayRenderer SMOKE_STACK_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/smoke_stack");
    public static final OrientedOverlayRenderer PRIMITIVE_SMELTER_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/primitive_smelter");
    public static final OrientedOverlayRenderer TURNING_ZONE_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/turning_zone");
    public static final OrientedOverlayRenderer MILLING_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/milling");
    public static final OrientedOverlayRenderer CLUSTER_MILL_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/cluster_mill");
    public static final OrientedOverlayRenderer ROLLING_MILL_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/rolling_mill");
    public static final OrientedOverlayRenderer FLYING_SHEAR_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/flying_shear");
    public static final OrientedOverlayRenderer BILLET_MOLD_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/billet_mold");
    public static final OrientedOverlayRenderer SLAB_MOLD_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/slab_mold");
    public static final OrientedOverlayRenderer STRAND_COOLER_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/strand_cooler");
    public static final OrientedOverlayRenderer GAS_ATOMIZER_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/gas_atomizer");
    public static final OrientedOverlayRenderer METALLURGICAL_CONVERTER_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/metallurgical_converter");
    public static final OrientedOverlayRenderer INJECTION_MOLDER_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/injection_molder");
    public static final OrientedOverlayRenderer POLYMERIZATION_TANK_OVERLAY = new OrientedOverlayRenderer(
            "machines/multiblocks/polymerization_tank");

    public static final SimpleOverlayRenderer SLAG_HOT = new SimpleOverlayRenderer("resource/slag_hot");
    public static final SimpleOverlayRenderer RESTRICTIVE_FILTER_FILTER_OVERLAY = new SimpleOverlayRenderer(
            "cover/overlay_restrictive_filter");
    public static final SimpleOverlayRenderer STRAND_BUS_OVERLAY = new SimpleOverlayRenderer(
            "logistics/overlay_strand_bus");
    public static final SimpleOverlayRenderer CODE_BREACHER_OVERLAY = new SimpleOverlayRenderer(
            "storage/crates/code_breacher");
    public static final SimpleOverlayRenderer REDSTONE_CONTROLLER_OVERLAY = new SimpleOverlayRenderer(
            "machines/overlay/overlay_redstone");

    public static final SimpleCubeRenderer MASONRY_BRICK = new SimpleCubeRenderer(
            "gregtech:blocks/multiblock_casing/masonry_brick");
    public static final SimpleCubeRenderer SILICON_CARBIDE_CASING = new SimpleCubeRenderer(
            "gregtech:blocks/multiblock_casing/silicon_carbide_casing");
    public static final SimpleCubeRenderer MONEL_500_CASING = new SimpleCubeRenderer(
            "gregtech:blocks/multiblock_casing/monel_500_casing");
    public static final SimpleCubeRenderer CONDUCTIVE_COPPER_PIPE = new SimpleCubeRenderer(
            "gregtech:blocks/multiblock_casing/copper_casing_pipe");
    public static final SimpleCubeRenderer ULV_STRUCTURAL_CASING = new SimpleCubeRenderer(
            "gregtech:blocks/multiblock_casing/ulv_structural_casing");
    public static final SimpleCubeRenderer STEEL_TURBINE_CASING = new SimpleCubeRenderer(
            "gregtech:blocks/casings/mechanic/machine_casing_turbine_steel");
    public static final SimpleCubeRenderer TITANIUM_TURBINE_CASING = new SimpleCubeRenderer(
            "gregtech:blocks/casings/mechanic/machine_casing_turbine_titanium");

    public static final DrumRenderer PLASTIC_CAN = new DrumRenderer("storage/drums/plastic_can");

    // Stock stuff
    // public static final SimpleCubeRenderer STOCK_MACHINE_CASING = new
    // SimpleCubeRenderer("rail_interfaces/stock_machine_casing/stock_machine_casing");
    public static final SimpleSidedCubeRenderer STOCK_MACHINE_CASING = Textures.VOLTAGE_CASINGS[1];

    public static final OrientedOverlayRenderer STOCK_FLUID_EXCHANGER = new OrientedOverlayRenderer(
            "rail_interfaces/fluid_exchanger");
    public static final OrientedOverlayRenderer STOCK_ITEM_EXCHANGER = new OrientedOverlayRenderer(
            "rail_interfaces/item_exchanger");

    public static final SimpleOrientedCubeRenderer STOCK_DETECTOR_NEITHER = new SimpleOrientedCubeRenderer(
            "rail_interfaces/stock_detector/stock_detector_neither");
    public static final SimpleOrientedCubeRenderer STOCK_DETECTOR_DETECTING = new SimpleOrientedCubeRenderer(
            "rail_interfaces/stock_detector/stock_detector_detecting");
    public static final SimpleOrientedCubeRenderer STOCK_DETECTOR_FILTER = new SimpleOrientedCubeRenderer(
            "rail_interfaces/stock_detector/stock_detector_filter");
    public static final SimpleOrientedCubeRenderer STOCK_DETECTOR_BOTH = new SimpleOrientedCubeRenderer(
            "rail_interfaces/stock_detector/stock_detector_both");

    public static final SimpleOrientedCubeRenderer STOCK_READER_ITEM = new SimpleOrientedCubeRenderer(
            "rail_interfaces/content_reader/content_reader_item");
    public static final SimpleOrientedCubeRenderer STOCK_READER_FLUID = new SimpleOrientedCubeRenderer(
            "rail_interfaces/content_reader/content_reader_fluid");

    public static final OrientedOverlayRenderer STOCK_CONTROLLER = new OrientedOverlayRenderer(
            "rail_interfaces/stock_controller");
}
