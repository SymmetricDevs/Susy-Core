package supersymmetry.common.blocks.rocketry;

import net.minecraft.block.SoundType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.IStringSerializable;

import gregtech.api.block.IStateHarvestLevel;
import gregtech.api.block.VariantBlock;

public class BlockLifeSupport extends VariantBlock<BlockLifeSupport.Ability> {

    public BlockLifeSupport() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("spacecraft_life_support");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setDefaultState(getState(Ability.OXYGEN_REGEN));
    }

    public enum Ability implements IStringSerializable, IStateHarvestLevel {

        OXYGEN_REGEN("oxygen_regen", 3);

        private String name;
        private int harvest;

        Ability(String name, int h) {
            this.name = name;
            this.harvest = h;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public int getHarvestLevel(IBlockState iBlockState) {
            return harvest;
        }
    }
}
