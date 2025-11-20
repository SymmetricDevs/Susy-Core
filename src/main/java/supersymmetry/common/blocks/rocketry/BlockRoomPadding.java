package supersymmetry.common.blocks.rocketry;

import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;

import supersymmetry.api.blocks.VariantDirectionalCoverableBlock;
import supersymmetry.common.item.SuSyMetaItems;

import static supersymmetry.common.materials.SusyMaterials.MetallizedBoPET;

public class BlockRoomPadding extends VariantDirectionalCoverableBlock<BlockRoomPadding.CoveringType> {

    public BlockRoomPadding() {
        super(Material.IRON);
        setTranslationKey("spacecraft_room_padding");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.CLOTH);
        setDefaultState(getState(CoveringType.PADDING));
        setHarvestLevel("wrench", 3);
        validCover = (ItemStack i) -> SuSyMetaItems.getItem("padding_cloth").isItemEqual(i);
    }

    public enum CoveringType implements IStringSerializable {

        PADDING("padding");

        public String name;

        CoveringType(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
