package supersymmetry.common.blocks.rocketry;

import java.util.List;
import java.util.stream.Stream;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import supersymmetry.api.rocketry.WeightedBlock;
import supersymmetry.api.rocketry.components.Instrument;
import supersymmetry.common.blocks.SuSyBlocks;
import supersymmetry.common.entities.EntityAbstractRocket;
import supersymmetry.common.rocketry.instruments.InstrumentLander;
import supersymmetry.common.rocketry.instruments.InstrumentLanderOneWay;
import supersymmetry.common.rocketry.instruments.InstrumentRobotArm;

public class BlockSpacecraftInstrument extends VariantBlock<BlockSpacecraftInstrument.Type>
                                       implements
                                       WeightedBlock<BlockSpacecraftInstrument.Type> {

    public BlockSpacecraftInstrument() {
        super(Material.IRON);
        setTranslationKey("spacecraft_instrument");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(Type.SENSOR_ARRAY));
        setHarvestLevel("wrench", 4);
    }

    @Override
    public double getMass(Type type) {
        return switch (type) {
            case SENSOR_ARRAY -> 70;
            case COLLECTOR -> 200;
            case CAMERA -> 40;
            case SOLAR_PANEL -> 100;
            case BATTERY -> 100;
            case ARM -> 400;
            case LANDER -> 15200; // Apollo lander mass from wikipedia
            case LANDER_ONE_WAY -> 7600;
            case NUCLEAR_REACTOR -> 1500;
            case CHEMICAL_THRUSTER -> 120;
            case FUEL_CELL -> 270; // Apollo: 111 - fuel cell itself, 13 - liquid hydrogen, 148 - liquid oxygen
            case EARTH_LANDING_SYSTEM -> 1500; // Apollo heat shield was 1400 + the parachutes
            case MAIN_ENGINE -> 150;
            case OXIDIZER_TANK -> 1430; // Apollo service module had 11433 kg of oxidizer, divided among 8 tanks
            case FUEL_TANK -> 890; // 7151 kg Apollo fuel / 8 tanks
        };
    }

    public static boolean allowedOnHull(Type type) {
        if (type == null) {
            return false;
        }
        return switch (type) {
            case NUCLEAR_REACTOR -> false;
            case BATTERY -> false;
            case FUEL_CELL -> false;
            case OXIDIZER_TANK -> false;
            case FUEL_TANK -> false;
            default -> true;
        };
    }

    public static int getPowerProduced(Type type) {
        if (type == null) {
            return 0;
        }
        return switch (type) {
            case SOLAR_PANEL -> 500;
            case NUCLEAR_REACTOR -> 5000;
            case FUEL_CELL -> 1500;
            default -> 0;
        };
    }

    public static int getPowerConsumed(Type type) {
        if (type == null) {
            return 0;
        }
        return switch (type) {
            case SENSOR_ARRAY -> 250;
            case ARM -> 1000;
            case LANDER -> 1500;
            case LANDER_ONE_WAY -> 1000;
            default -> 0;
        }; // life support is 1500, guidance is 250
    }

    public static int getRequiredBatteries(Type type) {
        if (type == null) {
            return 0;
        }
        return switch (type) {
            case SOLAR_PANEL -> 1;
            case NUCLEAR_REACTOR -> 2; // used as backup power and to equalize loads
            case FUEL_CELL -> 1; // same here
            default -> 0;
        };
    }

    public static Type getTypeFromBlockstate(IBlockState state) {
        for (Type value : Type.values()) {
            if (state.equals(SuSyBlocks.SPACE_INSTRUMENT.getState(value))) {
                return value;
            }
        }
        return null;
    }

    public enum Type implements IStringSerializable, IStateHarvestLevel {

        SENSOR_ARRAY("sensors", 4),
        COLLECTOR("collector", 4),
        CAMERA("camera", 4),
        SOLAR_PANEL("solar_panel", 4),
        BATTERY("battery", 4),
        ARM("arm", 4, new InstrumentRobotArm()),
        LANDER("lander", 4, new InstrumentLander()), // will have variable purposes
        NUCLEAR_REACTOR("nuclear_reactor", 4), // https://en.wikipedia.org/wiki/Kilopower
        CHEMICAL_THRUSTER("chemical_thruster", 4),
        FUEL_CELL("fuel_cell", 4),
        EARTH_LANDING_SYSTEM("earth_landing_system", 4),
        LANDER_ONE_WAY("lander_one_way", 4, new InstrumentLanderOneWay()),
        MAIN_ENGINE("main_engine", 4),
        OXIDIZER_TANK("oxidizer_tank", 4),
        FUEL_TANK("fuel_tank", 4);

        public String name;
        public int h;
        public Instrument instrument;

        Type(String name, int h) {
            this(name, h, null);
        }

        Type(String name, int h, Instrument instrument) {
            this.name = name;
            this.h = h;
            this.instrument = instrument;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return h;
        }

        @Override
        public String getHarvestTool(IBlockState state) {
            return "wrench";
        }

        public void act(int count, EntityAbstractRocket rocket) {
            if (instrument != null)
                instrument.act(count, rocket);
        }

        public static Type getInstrument(String name) {
            return Stream.of(values()).filter(type -> type.name.equals(name)).findFirst().orElse(null);
        }
    }

    @SideOnly(Side.CLIENT)
    public void addInformation(@NotNull ItemStack stack, @Nullable World player, @NotNull List<String> tooltip,
                               @NotNull ITooltipFlag advanced) {
        tooltip.add(I18n.format("susy.tooltip.mass", getMass(stack)));
    }
}
