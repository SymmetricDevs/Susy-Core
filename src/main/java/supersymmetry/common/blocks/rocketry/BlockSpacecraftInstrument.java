package supersymmetry.common.blocks.rocketry;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.IStringSerializable;

public class BlockSpacecraftInstrument extends VariantBlock<BlockSpacecraftInstrument.Type> {
    public BlockSpacecraftInstrument() {
        super(Material.IRON);
        setTranslationKey("spacecraft_instrument");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench",2);
        setDefaultState(getState(Type.FLIGHT_COMPUTER));
    }

    public enum Type implements IStringSerializable {
        CAMERA("position"),
        FLIGHT_COMPUTER("computer"),
        ENGINE("engine"),
        COLLECTOR("collector"),
        SOLAR_PANEL("solar_panel"),
        RAD_SHIELD("radiation_shield");
        public String name;
        Type(String name) {
            this.name = name;
        }
        @Override
        public String getName() {
            return this.name;
        }
    }
}
