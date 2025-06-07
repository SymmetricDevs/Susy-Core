package supersymmetry.common.blocks.rocketry;

import com.cleanroommc.groovyscript.compat.vanilla.OreDict;
import com.cleanroommc.groovyscript.compat.vanilla.OreDictEntry;
import gregtech.api.unification.OreDictUnifier;
import gregtech.api.unification.material.registry.MaterialRegistry;
import gregtech.api.unification.ore.OrePrefix;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IStringSerializable;
import net.minecraftforge.oredict.OreDictionary;
import supersymmetry.api.blocks.VariantDirectionalCoverableBlock;
import supersymmetry.common.materials.SusyMaterials;

import static supersymmetry.common.materials.SusyMaterials.MetallizedBoPET;

public class BlockSpacecraftHull extends VariantDirectionalCoverableBlock<BlockSpacecraftHull.HullType> {
    public BlockSpacecraftHull() {
        super(net.minecraft.block.material.Material.IRON);
        setTranslationKey("spacecraft_hull");
        setHardness(5f);
        setResistance(15f);
        setSoundType(SoundType.METAL);
        setHarvestLevel("wrench",2);
        setDefaultState(getState(HullType.MYLAR));
        validCover = (ItemStack i) -> i.isItemEqualIgnoreDurability(OreDictUnifier.get(OrePrefix.foil, MetallizedBoPET));
    }

    public enum HullType implements IStringSerializable {
        MYLAR("mylar");
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
