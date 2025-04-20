package supersymmetry.common.blocks.rocketry;

import gregtech.api.block.VariantBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.IStringSerializable;

public class BlockLifeSupport extends VariantBlock<BlockLifeSupport.Ability> {
    public BlockLifeSupport() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("spacecraft_life_support");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench",2);
        setDefaultState(getState(Ability.OXYGEN_REGEN));
    }

    public enum Ability implements IStringSerializable {
        OXYGEN_REGEN("oxygen_regen");
        private String name;
        Ability(String name) {
            this.name = name;
        }
        @Override
        public String getName() {
            return name;
        }
    }
}
