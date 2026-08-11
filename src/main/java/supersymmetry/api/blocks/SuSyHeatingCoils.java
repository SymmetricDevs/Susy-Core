package supersymmetry.api.blocks;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.GregTechAPI;
import gregtech.api.block.IHeatingCoilBlockStats;
import gregtech.api.unification.material.Material;
import gregtech.api.unification.material.Materials;
import gregtech.common.blocks.BlockWireCoil.CoilType;
import gregtech.common.blocks.MetaBlocks;
import supersymmetry.common.blocks.BlockHeatingCoil;
import supersymmetry.common.blocks.SuSyBlocks;

public final class SuSyHeatingCoils {

    private SuSyHeatingCoils() {}

    public static void init() {
        GregTechAPI.HEATING_COILS.clear();

        GregTechAPI.HEATING_COILS.put(
                MetaBlocks.WIRE_COIL.getState(CoilType.CUPRONICKEL),
                SuSyCoilType.CUPRONICKEL);

        GregTechAPI.HEATING_COILS.put(
                MetaBlocks.WIRE_COIL.getState(CoilType.NICHROME),
                SuSyCoilType.NICHROME);

        GregTechAPI.HEATING_COILS.put(
                MetaBlocks.WIRE_COIL.getState(CoilType.KANTHAL),
                SuSyCoilType.KANTHAL);

        GregTechAPI.HEATING_COILS.put(
                MetaBlocks.WIRE_COIL.getState(CoilType.RTM_ALLOY),
                SuSyCoilType.RTM_ALLOY);

        GregTechAPI.HEATING_COILS.put(
                MetaBlocks.WIRE_COIL.getState(CoilType.HSS_G),
                SuSyCoilType.HSS_G);

        GregTechAPI.HEATING_COILS.put(
                SuSyBlocks.HEATING_COIL.getState(BlockHeatingCoil.CoilType.MOLYBDENUM_DISILICIDE),
                BlockHeatingCoil.CoilType.MOLYBDENUM_DISILICIDE);

        GregTechAPI.HEATING_COILS.put(
                SuSyBlocks.HEATING_COIL.getState(BlockHeatingCoil.CoilType.TUNGSTEN),
                BlockHeatingCoil.CoilType.TUNGSTEN);
    }

    public enum SuSyCoilType implements IHeatingCoilBlockStats {

        CUPRONICKEL("cupronickel", 800, 1, 1, 0, Materials.Cupronickel),
        NICHROME("nichrome", 1400, 2, 1, 1, Materials.Nichrome),
        KANTHAL("kanthal", 1700, 2, 2, 2, Materials.Kanthal),

        // Deprecated but will still function
        RTM_ALLOY("rtm_alloy", 2100, 4, 2, 3, Materials.RTMAlloy),
        HSS_G("hss_g", 3000, 4, 4, 4, Materials.HSSG);

        private final String name;
        private final int coilTemperature;
        private final int level;
        private final int energyDiscount;
        private final int tier;
        private final Material material;

        SuSyCoilType(String name,
                     int coilTemperature,
                     int level,
                     int energyDiscount,
                     int tier,
                     Material material) {
            this.name = name;
            this.coilTemperature = coilTemperature;
            this.level = level;
            this.energyDiscount = energyDiscount;
            this.tier = tier;
            this.material = material;
        }

        @NotNull @Override
        public String getName() {
            return name;
        }

        @Override
        public int getCoilTemperature() {
            return coilTemperature;
        }

        @Override
        public int getLevel() {
            return level;
        }

        @Override
        public int getEnergyDiscount() {
            return energyDiscount;
        }

        @Override
        public int getTier() {
            return tier;
        }

        @Nullable @Override
        public Material getMaterial() {
            return material;
        }
    }
}
