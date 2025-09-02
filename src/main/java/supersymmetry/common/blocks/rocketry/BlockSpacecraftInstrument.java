package supersymmetry.common.blocks.rocketry;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class BlockSpacecraftInstrument extends VariantBlock<BlockSpacecraftInstrument.Type> {
    public BlockSpacecraftInstrument() {
        super(Material.IRON);
        setTranslationKey("spacecraft_instrument");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(Type.FLIGHT_COMPUTER));
    }

    public enum Type implements IStringSerializable, IStateHarvestLevel {
        SENSOR_ARRAY("sensors", 1),
        COLLECTOR("collector", 2),
        CAMERA("position", 2),
        FLIGHT_COMPUTER("computer", 3),
        ENGINE("engine", 3),
        SOLAR_PANEL("solar_panel", 2),
        BATTERY("battery", 2),
        ARM("arm", 2); // will have variable purposes
        public String name;
        public int h;
        Type(String name, int h) {
            this.name = name;
            this.h = h;
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
    }
}
