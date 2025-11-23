package supersymmetry.common.blocks.rocketry;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import supersymmetry.api.rocketry.components.Instrument;
import supersymmetry.common.entities.EntityAbstractRocket;
import supersymmetry.common.rocketry.instruments.InstrumentLander;
import supersymmetry.common.rocketry.instruments.InstrumentRobotArm;

public class BlockSpacecraftInstrument extends VariantBlock<BlockSpacecraftInstrument.Type> {

    public BlockSpacecraftInstrument() {
        super(Material.IRON);
        setTranslationKey("spacecraft_instrument");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(Type.FLIGHT_COMPUTER));
        setHarvestLevel("wrench", 4);
    }

    public enum Type implements IStringSerializable, IStateHarvestLevel {

        SENSOR_ARRAY("sensors", 4),
        COLLECTOR("collector", 4),
        CAMERA("position", 4),
        FLIGHT_COMPUTER("computer", 4),
        ENGINE("engine", 4),
        SOLAR_PANEL("solar_panel", 4),
        BATTERY("battery", 4),
        ARM("arm", 4, new InstrumentRobotArm()),
        LANDER("lander", 4, new InstrumentLander()); // will have variable purposes

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
            if (instrument != null) instrument.act(count, rocket);
        }
    }
}
