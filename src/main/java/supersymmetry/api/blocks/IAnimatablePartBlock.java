package supersymmetry.api.blocks;

import static gregtech.api.util.GTUtility.gregtechId;
import static supersymmetry.api.util.SuSyUtility.susyId;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import supersymmetry.common.tileentities.AnimatablePartTileEntity;

public interface IAnimatablePartBlock extends ITileEntityProvider {

    PropertyBool ACTIVE = PropertyBool.create("active");

    default Block thisObject() {
        return (Block) this;
    }

    default String getGeoName() {
        return thisObject().getTranslationKey().substring(5); // Remove "tile." prefix
    }

    default ResourceLocation modelRL() {
        return susyId("geo/" + getGeoName() + ".geo.json");
    }

    default ResourceLocation textureRL() {
        return gregtechId("textures/blocks/casings/" + getGeoName() + "/all.png");
    }

    default ResourceLocation animationRL() {
        return susyId("animations/" + getGeoName() + ".animation.json");
    }

    @Nullable
    @Override
    default TileEntity createNewTileEntity(@NotNull World worldIn, int meta) {
        return new AnimatablePartTileEntity();
    }

    default AxisAlignedBB getRenderBoundingBox(World world, BlockPos pos, int meta) {
        return new AxisAlignedBB(pos);
    }
}
