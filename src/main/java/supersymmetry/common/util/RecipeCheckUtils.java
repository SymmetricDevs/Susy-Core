package supersymmetry.common.util;

import static supersymmetry.common.world.atmosphere.AtmosphereUtils.isPosOxygenated;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.jetbrains.annotations.NotNull;

import gregtech.api.metatileentity.MetaTileEntity;
import gregtech.api.recipes.Recipe;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import supersymmetry.api.recipes.properties.AtmosphereProperty;
import supersymmetry.api.recipes.properties.BiomeProperty;
import supersymmetry.api.recipes.properties.DimensionProperty;

public class RecipeCheckUtils {

    public static boolean checkAtmosphere(Recipe recipe, MetaTileEntity controller) {
        if (!recipe.hasProperty(AtmosphereProperty.getInstance())) {
            return true;
        }
        Boolean needsAtmosphere = recipe.getProperty(AtmosphereProperty.getInstance(), null);
        return checkAtmosphere(controller, needsAtmosphere);
    }

    public static boolean checkAtmosphere(MetaTileEntity controller, Boolean needsAtmosphere) {
        if (needsAtmosphere == null) {
            return true;
        }
        boolean isAtmospherePresent = isPosOxygenated(controller.getPos().offset(controller.getFrontFacing()),
                controller.getWorld());
        return isAtmospherePresent == needsAtmosphere;
    }

    public static boolean checkDimension(Recipe recipe, MetaTileEntity controller) {
        IntList dimensionIDs = recipe.getProperty(DimensionProperty.getInstance(), IntLists.EMPTY_LIST);
        if (dimensionIDs.isEmpty() || dimensionIDs.contains(controller.getWorld().provider.getDimension())) {
            return true;
        }
        return false;
    }

    public static boolean checkBiomeRequirement(@NotNull Recipe recipe, MetaTileEntity controller) {
        if (!recipe.hasProperty(BiomeProperty.getInstance())) return true;
        return recipe.getProperty(BiomeProperty.getInstance(), BiomeProperty.BiomePropertyList.EMPTY_LIST)
                .checkBiome(controller.getWorld().getBiome(controller.getPos()));
    }

    public static boolean checkBiomeRequirement(@NotNull Recipe recipe, World world, BlockPos pos) {
        if (!recipe.hasProperty(BiomeProperty.getInstance())) return true;
        return recipe.getProperty(BiomeProperty.getInstance(), BiomeProperty.BiomePropertyList.EMPTY_LIST)
                .checkBiome(world.getBiome(pos));
    }
}
