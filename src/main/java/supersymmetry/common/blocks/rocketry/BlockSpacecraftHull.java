package supersymmetry.common.blocks.rocketry;

import static supersymmetry.common.materials.SusyMaterials.MetallizedBoPET;

import net.minecraft.block.SoundType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import supersymmetry.api.blocks.VariantDirectionalCoverableBlock;
import supersymmetry.api.rocketry.WeightedBlock;

public class BlockSpacecraftHull extends VariantDirectionalCoverableBlock<BlockSpacecraftHull.HullType> implements
                                 WeightedBlock<BlockSpacecraftHull.HullType> {

    public BlockSpacecraftHull() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("spacecraft_hull");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench", 4);
        setDefaultState(getState(HullType.AL_LI));
        validCover = (ItemStack i) -> i
                .isItemEqualIgnoreDurability(OreDictUnifier.get(OrePrefix.foil, MetallizedBoPET));
    }

    @Override
    public double getMass(HullType type) {
        return switch (type) {
            case AL_LI -> 15;
            default -> 50;
        };
    }

    public enum HullType implements IStringSerializable {

        AL_LI("al_li");

        public String name;

        HullType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
